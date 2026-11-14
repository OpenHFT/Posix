/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.jna.WinJNAPosixAPI;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** Executed contract coverage, including every public overload and unavailable operation. */
@RunWith(Parameterized.class)
public class WindowsContractTest {
    private static final Map<String, Set<String>> SEEN = new HashMap<>();
    private static final Set<String> STATIC = new HashSet<>();
    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> providers() {
        List<Object[]> result = new ArrayList<>();
        for (String name : System.getProperty("posix.test.providers", "jnr,jna").split(","))
            result.add(new Object[] {name});
        return result;
    }
    private final String provider;
    private PosixAPI api;
    public WindowsContractTest(String provider) { this.provider = provider; }
    @BeforeClass public static void clearCoverage() { SEEN.clear(); STATIC.clear(); }
    @Before public void createProvider() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        final PosixAPI delegate = create(provider);
        api = (PosixAPI) Proxy.newProxyInstance(PosixAPI.class.getClassLoader(), new Class<?>[] {PosixAPI.class},
                (proxy, method, args) -> {
                    SEEN.computeIfAbsent(provider, key -> new HashSet<>()).add(method.toGenericString());
                    try { return method.invoke(delegate, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                });
    }
    private static PosixAPI create(String name) throws Exception {
        if ("jnr".equals(name)) return new WinJNRPosixAPI();
        if ("jna".equals(name)) return new WinJNAPosixAPI();
        return (PosixAPI) Class.forName("net.openhft.posix.ffm.WinFFMPosixAPI").getDeclaredConstructor().newInstance();
    }
    @AfterClass public static void everySignatureExecuted() {
        if (!System.getProperty("os.name").startsWith("Windows")) return;
        for (Object[] entry : providers()) {
            String name = (String) entry[0];
            Set<String> missing = new TreeSet<>();
            for (Method method : PosixAPI.class.getDeclaredMethods()) {
                if (method.isSynthetic()) continue;
                Set<String> seen = Modifier.isStatic(method.getModifiers()) ? STATIC : SEEN.get(name);
                if (seen == null || !seen.contains(method.toGenericString())) missing.add(method.toGenericString());
            }
            assertTrue(name + " untested signatures: " + missing, missing.isEmpty());
        }
    }
    @Test public void selection() throws Exception {
        PosixAPI previous = PosixAPIHolder.POSIX_API;
        String property = System.getProperty("chronicle.posix.provider");
        try {
            System.setProperty("chronicle.posix.provider", provider);
            PosixAPIHolder.POSIX_API = null;
            PosixAPI selected = PosixAPI.posix();
            assertEquals(create(provider).getClass(), selected.getClass());
            assertSame(selected, PosixAPI.posix());
            PosixAPI.useNoOpPosixApi();
            assertTrue(PosixAPI.posix() instanceof net.openhft.posix.internal.noop.NoOpPosixAPI);
            STATIC.add(PosixAPI.class.getMethod("posix").toGenericString());
            STATIC.add(PosixAPI.class.getMethod("useNoOpPosixApi").toGenericString());
        } finally {
            PosixAPIHolder.POSIX_API = previous;
            if (property == null) System.clearProperty("chronicle.posix.provider");
            else System.setProperty("chronicle.posix.provider", property);
        }
    }
    @Test public void files() throws Exception {
        Path directory = Files.createTempDirectory("posix-contract");
        Path file = directory.resolve("unicode-\u03bb.dat");
        long buffer = api.malloc(16);
        int fd = -1;
        try {
            fd = api.open(file.toString(), 2 | 0x200 | 0x800, 0600);
            assertTrue(api.lastErrorStr(), fd >= 0);
            byte[] bytes = {0, 10, 13, 10, 26, (byte) 255};
            for (int i = 0; i < bytes.length; i++) UNSAFE.putByte(buffer + i, bytes[i]);
            assertEquals(bytes.length, api.write(fd, buffer, bytes.length));
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertEquals(0, api.lseek(fd, 0, WhenceFlag.SEEK_SET));
            UNSAFE.setMemory(buffer, 16, (byte) 0);
            assertEquals(bytes.length, api.read(fd, buffer, 16));
            for (int i = 0; i < bytes.length; i++) assertEquals(bytes[i], UNSAFE.getByte(buffer + i));
            assertEquals(0, api.read(fd, buffer, 1));
            assertEquals(0, api.write(fd, buffer, 0));
            assertEquals((1L << 32) + 7, api.lseek(fd, (1L << 32) + 7, 0));
            assertEquals(0, api.ftruncate(fd, 64));
            assertEquals(64, Files.size(file));
            assertEquals((1L << 32) + 7, api.lseek(fd, 0, 1));
            assertEquals(0, api.close(fd)); fd = -1;
            assertEquals(-1, api.open(file.toString(), 2 | 0x200 | 0x800, 0600));
            fd = api.open(file.toString(), 2 | 0x400, 0600);
            assertTrue(fd >= 0); assertEquals(0, Files.size(file));
            assertEquals(0, api.close(fd)); fd = -1;
            fd = api.open(file.toString(), OpenFlag.O_RDONLY, 0);
            assertTrue(fd >= 0);
            assertEquals(-1, api.write(fd, buffer, 1));
            assertEquals(-1, api.ftruncate(fd, 1));
            assertEquals(-1, api.open(file.toString(), 4, 0600));
            assertEquals(22, api.lastError());
            assertFalse(api.strerror(22).isEmpty());
            assertFalse(api.lastErrorStr().isEmpty());
            assertEquals(-1, api.read(-1, buffer, 1)); assertEquals(9, api.lastError());
            assertEquals(-1, api.write(-1, buffer, -1)); assertEquals(22, api.lastError());
            assertEquals(-1, api.lseek(-1, 0, 0));
            assertEquals(-1, api.close(-1));
        } finally {
            if (fd >= 0) api.close(fd);
            api.free(buffer); api.free(0);
            Files.deleteIfExists(file); Files.deleteIfExists(directory);
        }
    }
    @Test public void clocksAndIdentity() throws Exception {
        long before = System.currentTimeMillis();
        long realtime = api.clock_gettime();
        assertTrue(realtime / 1_000_000 >= before - 1);
        assertTrue(realtime / 1_000_000 <= System.currentTimeMillis() + 1);
        long monotonic = api.clock_gettime(ClockId.CLOCK_MONOTONIC);
        assertTrue(api.clock_gettime(1) >= monotonic);
        for (ClockId id : ClockId.values()) {
            if (id.value() <= 3) assertTrue(api.clock_gettime(id) != 0 || id.value() >= 2);
            else try { api.clock_gettime(id); fail("Unsupported clock accepted: " + id); }
                 catch (IllegalArgumentException expected) { /* expected */ }
        }
        try { api.clock_gettime(-1); fail(); } catch (IllegalArgumentException expected) { /* expected */ }
        long memory = api.malloc(32);
        try {
            UNSAFE.setMemory(memory, 32, (byte) 0x5a);
            assertEquals(0, api.gettimeofday(memory + 8));
            assertTrue(UNSAFE.getLong(memory + 8) > 0);
            assertTrue(UNSAFE.getLong(memory + 16) >= 0 && UNSAFE.getLong(memory + 16) < 1_000_000);
            assertEquals(0x5a5a5a5a5a5a5a5aL, UNSAFE.getLong(memory));
            assertEquals(0x5a5a5a5a5a5a5a5aL, UNSAFE.getLong(memory + 24));
            assertTrue(api.gettimeofday() > 0);
        } finally { api.free(memory); }
        assertTrue(api.get_nprocs() > 0); assertTrue(api.get_nprocs_conf() >= api.get_nprocs());
        int pid = api.getpid(), tid = api.gettid();
        assertTrue(pid > 0); assertEquals(tid, api.gettid());
        FutureTask<int[]> other = new FutureTask<>(() -> {
            PosixAPI threadApi = create(provider);
            threadApi.ftruncate(-1, -1);
            return new int[] {threadApi.getpid(), threadApi.gettid(), threadApi.lastError()};
        });
        api.close(-1);
        Thread thread = new Thread(other); thread.start();
        int[] ids = other.get(10, TimeUnit.SECONDS); thread.join(1000);
        assertEquals(pid, ids[0]); assertNotEquals(tid, ids[1]); assertEquals(22, ids[2]);
        assertEquals(9, api.lastError());
    }
    @Test public void explicitUnavailable() throws Exception {
        assertEquals(-1, api.fallocate(-1, 1, 0, 4096)); assertEquals(40, api.lastError());
        assertEquals(-1, api.lockf(-1, 3, 1));
        assertEquals(-1, api.mmap(0, 4096, 3, 1, -1, 0));
        assertEquals(-1, api.mmap(0, 4096, MMapProt.PROT_READ, MMapFlag.SHARED, -1, 0));
        assertEquals(-1, api.munmap(0, 4096));
        assertEquals(-1, api.msync(0, 4096, 4));
        assertEquals(-1, api.msync(0, 4096, MSyncFlag.MS_SYNC));
        assertEquals(-1, api.madvise(0, 4096, 4));
        for (MAdviseFlag flag : MAdviseFlag.values()) assertEquals(-1, api.madvise(0, 4096, flag));
        assertFalse(api.mlock(0, 4096)); assertFalse(api.mlock2(0, 4096, true));
        api.mlockall(1); assertEquals(40, api.lastError());
        api.mlockall(MclFlag.MclCurrent); assertEquals(40, api.lastError());
        long mask = api.malloc(128);
        try {
            UNSAFE.setMemory(mask, 128, (byte) 0);
            assertEquals(-1, api.sched_getaffinity(-1, 128, mask));
            assertEquals(-1, api.sched_setaffinity(-1, 128, mask));
            assertEquals(-1, api.sched_setaffinity_as(-1, 0));
            assertEquals(-1, api.sched_setaffinity_range(-1, 0, 0));
            assertTrue(api.sched_getaffinity_summary(-1).startsWith("na: "));
        } finally { api.free(mask); }
        try { api.du("missing-file"); fail("du unexpectedly available"); }
        catch (IOException expected) { assertFalse(expected.getMessage().isEmpty()); }
    }

    @Test public void mappingsPersistAndPrivateCopiesDoNot() throws Exception {
        Path file = Files.createTempFile("posix-map", ".dat");
        int fd = api.open(file.toString(), OpenFlag.O_RDWR, 0600);
        long view = -1;
        try {
            assertEquals(0, api.ftruncate(fd, 131072));
            assertEquals(-1, api.mmap(0, 4096, 3, 1, fd, 1));
            assertEquals(22, api.lastError());
            view = api.mmap(0, 4096, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 65536);
            assertTrue(api.lastErrorStr(), view != 0 && view != -1);
            assertEquals(0, api.close(fd)); fd = -1;
            UNSAFE.putByte(view, (byte) 71);
            assertEquals(0, api.msync(view, 4096, MSyncFlag.MS_SYNC));
            assertEquals(71, Files.readAllBytes(file)[65536]);
            assertEquals(-1, api.munmap(view, 2048));
            int advice = api.madvise(view, 4096, MAdviseFlag.MADV_WILLNEED);
            assertTrue(advice == 0 || api.lastError() != 0);
            boolean locked = api.mlock2(view, 4096, false);
            assertTrue(locked || api.lastError() != 0);
            assertEquals(0, api.munmap(view, 4096));
            assertEquals(-1, api.munmap(view, 4096)); view = -1;
            fd = api.open(file.toString(), OpenFlag.O_RDONLY, 0);
            view = api.mmap(0, 4096, 3, 2, fd, 65536);
            assertTrue(api.lastErrorStr(), view != 0 && view != -1);
            UNSAFE.putByte(view, (byte) 99);
            assertEquals(0, api.msync(view, 4096, 4));
            assertEquals(71, Files.readAllBytes(file)[65536]);
        } finally {
            if (view != -1 && view != 0) api.munmap(view, 4096);
            if (fd >= 0) api.close(fd);
            Files.deleteIfExists(file);
        }
    }

    @Test public void allocationLocksAndAffinity() throws Exception {
        Path file = Files.createTempFile("posix-allocation", ".dat");
        int fd = api.open(file.toString(), OpenFlag.O_RDWR, 0600);
        try {
            assertEquals(7, api.lseek(fd, 7, 0));
            assertEquals(0, api.fallocate(fd, 0, 0, 65536));
            assertEquals(65536, Files.size(file));
            assertEquals(7, api.lseek(fd, 0, 1));
            assertTrue(api.du(file.toString()) >= 64);
            assertArrayEquals(new byte[65536], Files.readAllBytes(file));
            assertEquals(-1, api.fallocate(fd, 0, Long.MAX_VALUE, 2));
            assertEquals(0, api.lockf(fd, 2, 32));
            assertEquals(0, api.lockf(fd, 0, 32));
            assertEquals(0, api.lockf(fd, 1, 32));
            assertEquals(0, api.lockf(fd, 0, 32));
            assertEquals(7, api.lseek(fd, 0, 1));
        } finally { api.close(fd); Files.deleteIfExists(file); }
        FutureTask<Boolean> affinityTask = new FutureTask<>(() -> {
            PosixAPI worker = create(provider);
            long original = worker.malloc(16), current = worker.malloc(16);
            boolean captured = false;
            try {
                if (worker.get_nprocs_conf() > 64) {
                    assertEquals(-1, worker.sched_getaffinity(0, 16, original));
                    assertEquals(40, worker.lastError()); return true;
                }
                assertEquals(0, worker.sched_getaffinity(0, 16, original)); captured = true;
                int cpu = Long.numberOfTrailingZeros(UNSAFE.getLong(original));
                assertEquals(0, worker.sched_setaffinity_as(0, cpu));
                assertEquals(0, worker.sched_getaffinity(0, 16, current));
                assertEquals(1L << cpu, UNSAFE.getLong(current));
                return true;
            } finally {
                if (captured) assertEquals(0, worker.sched_setaffinity(0, 16, original));
                worker.free(original); worker.free(current);
            }
        });
        Thread thread = new Thread(affinityTask); thread.start();
        assertTrue(affinityTask.get(10, TimeUnit.SECONDS)); thread.join(1000);
    }

    @Test public void locksContendAcrossProcesses() throws Exception {
        Path file = Files.createTempFile("posix-lock", ".dat");
        int fd = api.open(file.toString(), OpenFlag.O_RDWR, 0600);
        try {
            assertEquals(0, api.ftruncate(fd, 4096));
            assertEquals(0, api.lockf(fd, 2, 32));
            lockChild(file, "blocked");
            assertEquals(0, api.lockf(fd, 0, 32));
            lockChild(file, "available");
        } finally { api.close(fd); Files.deleteIfExists(file); }
    }
    private void lockChild(Path file, String expected) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + "/bin/java");
        if ("ffm".equals(provider)) command.add("--enable-native-access=ALL-UNNAMED");
        command.add("-cp"); command.add(System.getProperty("java.class.path"));
        command.add(WindowsLockChild.class.getName());
        command.add(provider); command.add(file.toString()); command.add(expected);
        Path output = Files.createTempFile("posix-child", ".log");
        Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue("Lock child timed out", child.waitFor(20, TimeUnit.SECONDS));
            assertEquals(new String(Files.readAllBytes(output), java.nio.charset.StandardCharsets.UTF_8), 0, child.exitValue());
        } finally {
            child.destroyForcibly(); child.waitFor(5, TimeUnit.SECONDS); Files.deleteIfExists(output);
        }
    }
}

