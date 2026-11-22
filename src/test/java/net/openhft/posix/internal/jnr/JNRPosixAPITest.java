/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.ffi.Platform;
import jnr.constants.platform.Errno;
import net.openhft.posix.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;

import static net.openhft.posix.internal.core.OS.isMacOSX;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class JNRPosixAPITest {

    private static final PosixAPI jnr = isUnix() ? new JNRPosixAPI() : new WinJNRPosixAPI();

    @Test
    public void open() throws IOException {
        final Path file = Files.createTempFile("open", ".test");
        final int fd = jnr.open(file.toString(), OpenFlag.O_RDWR, 0666);
        assertEquals(0, jnr.lseek(fd, 0, WhenceFlag.SEEK_SET));
        assertEquals(-1, jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA));
        assertEquals(0, jnr.ftruncate(fd, 4096));
        // 'lseek' on Windows and macOS doesn't support following behavior
        if (isUnix() && !isMacOSX()) {
            // ftruncate can create a sparse hole; SEEK_DATA is filesystem-dependent.
            long data = jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA);
            assertTrue(data == -1 || (data >= 16 && data < 4096));
            assertEquals(-1, jnr.lseek(fd, 4096, WhenceFlag.SEEK_DATA));
        }

        int err = jnr.close(fd);
        assertEquals(0, err);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void close() {
        int err = jnr.close(-1);
        assertEquals(-1, err);
    }

    @Test
    public void mmap_sync() throws IOException {
        assumeTrue(!isUnix() || new File("/proc/self/maps").exists());
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        long addr = -1;
        try {
            assertTrue(jnr.lastErrorStr(), fd >= 0);
            assertEquals(0, jnr.ftruncate(fd, length));
            boolean hasProc = new File("/proc/self/maps").exists();
            if (hasProc) assertEquals(0, jnr.du(filename));
            addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
            assertTrue(jnr.lastErrorStr(), addr != 0 && addr != -1);
            if (hasProc) {
                assertEquals(0, jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL));
                List<Mapping> list = ProcMaps.forSelf().findAll(m -> filename.equals(m.path()));
                assertEquals(1, list.size());
                assertEquals(addr, list.get(0).addr());
                assertEquals(length, list.get(0).length());
                assertEquals(0L, list.get(0).offset());
                assertEquals(0, jnr.du(filename));
                assertEquals(0, jnr.fallocate(fd, 0, 0, length));
                assertEquals(length >> 10, jnr.du(filename));
            }
            net.openhft.posix.internal.UnsafeMemory.UNSAFE.putByte(addr, (byte) 31);
            net.openhft.posix.internal.UnsafeMemory.UNSAFE.putByte(addr + length - 1, (byte) 47);
            assertEquals(0, jnr.msync(addr, length, MSyncFlag.MS_SYNC));
            byte[] persisted = Files.readAllBytes(file);
            assertEquals(31, persisted[0]);
            assertEquals(47, persisted[(int) length - 1]);
            if (isUnix()) assertEquals(0, jnr.msync(addr, length, MSyncFlag.MS_ASYNC));
            else {
                assertEquals(-1, jnr.msync(addr, length, MSyncFlag.MS_ASYNC));
                assertEquals(40, jnr.lastError());
            }
        } finally {
            releaseMapping(file, fd, addr, length);
        }
    }

    @Test
    public void mlockall() {
        assumeFalse("macOS doesn't support 'mlockall'", isMacOSX());

        PosixAPI.posix().mlockall(MclFlag.MclCurrent);
    }

    @Test
    public void mlock() throws IOException {
        mappedPageLock(false);
    }

    @Test
    public void mlock2() throws IOException {
        mappedPageLock(true);
    }

    private void mappedPageLock(boolean secondVariant) throws IOException {
        assumeTrue(!isUnix() || new File("/proc/self/maps").exists());
        Path file = Files.createTempFile("mlock", ".test");
        int fd = jnr.open(file.toString(), OpenFlag.O_RDWR, 0666);
        long length = 4096;
        long addr = -1;
        try {
            assertTrue(jnr.lastErrorStr(), fd >= 0);
            assertEquals(0, jnr.ftruncate(fd, length));
            addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
            assertTrue(jnr.lastErrorStr(), addr != 0 && addr != -1);
            if (isUnix()) {
                // Native permission and resource limits apply independently of the JVM vendor.
                boolean locked = secondVariant ? jnr.mlock2(addr, length, true) : jnr.mlock(addr, length);
                if (!locked) {
                    int errno = jnr.lastError();
                    assertTrue("Unexpected locking errno: " + errno,
                            errno == Errno.ENOSYS.intValue() || errno == Errno.ENOMEM.intValue()
                                    || errno == Errno.EPERM.intValue() || errno == Errno.EAGAIN.intValue());
                }
            } else if (!secondVariant) assertTrue(jnr.lastErrorStr(), jnr.mlock(addr, length));
            else {
                // Windows implements eager locking, not Linux's on-fault contract.
                assertFalse(jnr.mlock2(addr, length, true));
                assertEquals(40, jnr.lastError());
                assertTrue(jnr.lastErrorStr(), jnr.mlock2(addr, length, false));
            }
        } finally {
            releaseMapping(file, fd, addr, length);
        }
    }

    private void releaseMapping(Path file, int fd, long addr, long length) throws IOException {
        try {
            try {
                if (addr != 0 && addr != -1) assertEquals(0, jnr.munmap(addr, length));
            } finally {
                if (fd >= 0) assertEquals(0, jnr.close(fd));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void gettimeofday() {
        long firstCallIsSlow1 = jnr.gettimeofday();
        long firstCallIsSlow2 = jnr.clock_gettime();

        long time = jnr.gettimeofday();
        long clockGettime = jnr.clock_gettime();
        assertNotEquals(0, time);
        assertEquals(System.currentTimeMillis() * 1_000L, time, 2_000);
        assertEquals(clockGettime / 1000.0, time, 1_000);
    }

    @Test
    public void get_nprocs() {
        assumeFalse("macOS doesn't support 'get_nprocs'", isMacOSX());

        final int nprocs = jnr.get_nprocs();
        assertTrue(nprocs > 0);
        final int nprocs_conf = jnr.get_nprocs_conf();
        assertTrue(nprocs <= nprocs_conf);
    }

    /**
     * Applies the given supplier `n` times across `n` threads, collecting results into a set.
     *
     * @param n thread count/result count
     * @param r supplier to invoke
     * @return number of distinct results produced
     */
    private int poolIntReduce(int n, Supplier<Integer> r) throws InterruptedException {
        final ConcurrentSkipListSet<Integer> items = new ConcurrentSkipListSet<>();
        final ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < n; ++i) {
            Thread t = new Thread(() -> items.add(r.get()));
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }

        return items.size();
    }

    @Test
    public void getpid() throws InterruptedException {
        assumeFalse("macOS doesn't support 'getpid'", isMacOSX());

        final int N = jnr.get_nprocs();
        assertEquals(1, poolIntReduce(N, jnr::getpid));
    }

    @Test
    public void gettid() throws InterruptedException {
        assumeFalse("macOS doesn't support 'gettid'", isMacOSX());

        final int N = jnr.get_nprocs();
        assertEquals(N, poolIntReduce(N, jnr::gettid));

        if (new File("/proc").isDirectory()) {
            final int gettid = jnr.gettid();
            assertTrue(new File("/proc/self/task/" + gettid).exists());
        }
    }

    @Test
    public void setaffinity() {
        assumeFalse("macOS does not support this affinity facade", isMacOSX());

        int gettid = jnr.gettid();
        int count = jnr.get_nprocs_conf();
        int size = Math.max(8, (count + 63) / 64 * 8);
        long original = jnr.malloc(size);
        boolean captured = false;
        try {
            assertNotEquals(0L, original);
            if (!isUnix() && count > 64) {
                assertEquals(-1, jnr.sched_getaffinity(gettid, size, original));
                assertEquals(40, jnr.lastError());
                return;
            }
            assertEquals(0, jnr.sched_getaffinity(gettid, size, original));
            captured = true;
            int cpu = 0;
            while (cpu < count && (net.openhft.posix.internal.UnsafeMemory.UNSAFE.getByte(original + cpu / 8)
                    & (1 << (cpu & 7))) == 0) cpu++;
            assertTrue(cpu < count);
            assertEquals(0, jnr.sched_setaffinity_as(gettid, cpu));
            assertEquals(cpu + "-" + cpu, jnr.sched_getaffinity_summary(gettid));
            assertEquals(0, jnr.sched_setaffinity_range(gettid, cpu, cpu));
        } finally {
            try {
                if (captured) assertEquals(0, jnr.sched_setaffinity(gettid, size, original));
            } finally {
                jnr.free(original);
            }
        }
    }

    @Test
    public void clocks() {
        for (ClockId value : ClockId.values()) {
            if (!isUnix() && value.value() > 3) {
                assertThrows(IllegalArgumentException.class, () -> jnr.clock_gettime(value));
                continue;
            }
            try {
                long before = System.currentTimeMillis();
                final long gettime = jnr.clock_gettime(value);
                if (value == ClockId.CLOCK_REALTIME) {
                    assertTrue(gettime / 1_000_000 >= before - 1000);
                    assertTrue(gettime / 1_000_000 <= System.currentTimeMillis() + 1000);
                } else if (value == ClockId.CLOCK_MONOTONIC) {
                    assertTrue(jnr.clock_gettime(value) >= gettime);
                } else if (value == ClockId.CLOCK_PROCESS_CPUTIME_ID || value == ClockId.CLOCK_THREAD_CPUTIME_ID) {
                    assertTrue(gettime >= 0);
                }
            } catch (IllegalArgumentException e) {
                assertTrue("Required Windows clock unavailable: " + value, isUnix());
            }
        }
        assertThrows(IllegalArgumentException.class, () -> jnr.clock_gettime(-1));
    }

    private static boolean isUnix() {
        return Platform.getNativePlatform().isUnix();
    }
}
