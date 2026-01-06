/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.ffi.Platform;
import net.openhft.posix.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.openhft.posix.internal.core.OS.isMacOSX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JNRPosixAPITest {

    private static final PosixAPI jnr = isUnix() ? new JNRPosixAPI() : new WinJNRPosixAPI();

    @Test
    public void open() throws IOException {
        final Path file = Files.createTempFile("open", ".test");
        final int fd = jnr.open(file.toString(), OpenFlag.O_RDWR, 0666);
        assertEquals(0L, jnr.lseek(fd, 0, WhenceFlag.SEEK_SET), "lseek should return 0 when seeking to file start");
        assertEquals(-1L, jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA), "lseek SEEK_DATA should return -1 when seeking in empty file");
        assertEquals(0, jnr.ftruncate(fd, 4096), "ftruncate should return 0 when extending file to 4096 bytes");
        // 'lseek' on Windows and macOS doesn't support following behavior
        if (isUnix() && !isMacOSX()) {
            assertEquals(16L, jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA), "lseek SEEK_DATA should return 16 when seeking from offset 16");
            assertEquals(4095L, jnr.lseek(fd, 4095, WhenceFlag.SEEK_DATA), "lseek SEEK_DATA should return 4095 when seeking from offset 4095");
            assertEquals(-1L, jnr.lseek(fd, 4096, WhenceFlag.SEEK_DATA), "lseek SEEK_DATA should return -1 when seeking beyond file end");
        }

        int err = jnr.close(fd);
        assertEquals(0, err, "close should return 0 when closing file descriptor successfully");
        assertTrue(file.toFile().exists(), "temporary file should exist after close operation");
        assertTrue(file.toFile().delete(), "temporary file should be successfully deleted in cleanup");
    }

    @Test
    public void close() {
        int err = jnr.close(-1);
        assertEquals(-1, err, "close should return -1 when given invalid file descriptor");
    }

    @Test
    public void mmap_sync() throws IOException {
        assumeTrue(new File("/proc/self").exists(), "/proc/self must exist");
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err, "ftruncate should return 0 when resizing file successfully");

        assertEquals(0L, jnr.du(filename), "du should report 0 disk usage for sparse file before allocation");

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1L, addr, "mmap should return valid address for memory-mapped file");

        int err4 = jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL);
        assertEquals(0, err4, "madvise should return 0 when setting sequential access hint");

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> filename.equals(m.path()));
        assertEquals(1, list.size(), "proc maps should contain exactly one entry for mapped file");
        final Mapping mapping = list.get(0);
        assertEquals(addr, mapping.addr(), "proc maps mapping address should match mmap returned address");
        assertEquals(length, mapping.length(), "proc maps mapping length should match requested mmap size");
        assertEquals(0L, mapping.offset(), "proc maps mapping offset should be 0 for mapping at file start");

        assertEquals(0L, jnr.du(filename), "du should still report 0 disk usage before fallocate");
        int err3 = jnr.fallocate(fd, 0, 0, length);
        assertEquals(0, err3, "fallocate should return 0 when allocating disk space successfully");
        assertEquals(length >> 10, jnr.du(filename), "du should report disk usage in KB equal to allocated size");

        final int err0 = jnr.msync(addr, length, MSyncFlag.MS_ASYNC);
        assertEquals(0, err0, "msync should return 0 when synchronizing memory to disk");

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1, "munmap should return 0 when unmapping memory successfully");
        int err2 = jnr.close(fd);
        assertEquals(0, err2, "close should return 0 when closing file descriptor successfully");
        assertTrue(file.toFile().exists(), "temporary file should exist after mmap operations");
        assertTrue(file.toFile().delete(), "temporary file should be successfully deleted in cleanup");
    }

    @Test
    public void mlockall() {
        assumeFalse(isMacOSX(), "macOS doesn't support 'mlockall'");

        PosixAPI api = PosixAPI.posix();
        assertNotNull(api, "posix factory method should return non-null PosixAPI implementation");
        api.mlockall(MclFlag.MclCurrent);
    }

    @Test
    public void mlock() throws IOException {
        boolean deleted = runMlockTest(jnr::mlock);
        assertTrue(deleted, "mlock test should successfully delete temporary file after memory locking");
    }

    @Test
    public void mlock2() throws IOException {
        boolean deleted = runMlockTest((addr, length) -> jnr.mlock2(addr, length, true));
        assertTrue(deleted, "mlock2 test should successfully delete temporary file after memory locking");
    }

    private boolean runMlockTest(BiConsumer<Long, Long> lockFunction) throws IOException {
        assumeTrue(new File("/proc/self").exists(), "/proc/self must exist");
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err, "ftruncate should return 0 when resizing file for mlock test");

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1L, addr, "mmap should return valid address for memory locking test");

        lockFunction.accept(addr, length);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1, "munmap should return 0 after memory locking test");
        int err2 = jnr.close(fd);
        assertEquals(0, err2, "close should return 0 after completing mlock test");
        assertTrue(file.toFile().exists(), "temporary file should exist after mlock operations before cleanup");
        return file.toFile().delete();
    }

    @Test
    public void gettimeofday() {
        long firstCallIsSlow1 = jnr.gettimeofday();
        long firstCallIsSlow2 = jnr.clock_gettime();

        long time = jnr.gettimeofday();
        long clockGettime = jnr.clock_gettime();
        assertNotEquals(0L, time, "gettimeofday should return non-zero timestamp in microseconds");

        long expectedMicros = System.currentTimeMillis() * 1_000L;
        assertTrue(Math.abs(expectedMicros - time) <= 2_000L, "gettimeofday should be within 2ms of system current time");

        long clockMicros = clockGettime / 1_000L;
        assertTrue(Math.abs(clockMicros - time) <= 1_000L, "gettimeofday should be within 1ms of clock_gettime");
    }

    @Test
    public void get_nprocs() {
        assumeFalse(isMacOSX(), "macOS doesn't support 'get_nprocs'");

        final int nprocs = jnr.get_nprocs();
        assertTrue(nprocs > 0, "get_nprocs should return positive number of available processors");
        final int nprocs_conf = jnr.get_nprocs_conf();
        assertTrue(nprocs <= nprocs_conf, "available processors should not exceed configured processors");
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
        assumeFalse(isMacOSX(), "macOS doesn't support 'getpid'");

        final int N = jnr.get_nprocs();
        assertEquals(1, poolIntReduce(N, jnr::getpid), "getpid should return same process id across all threads");
    }

    @Test
    public void gettid() throws InterruptedException {
        assumeFalse(isMacOSX(), "macOS doesn't support 'gettid'");

        final int N = jnr.get_nprocs();
        assertEquals(N, poolIntReduce(N, jnr::gettid), "gettid should return unique thread id for each thread");

        if (new File("/proc").isDirectory()) {
            final int gettid = jnr.gettid();
            assertTrue(new File("/proc/self/task/" + gettid).exists(), "proc filesystem should contain task entry for current thread id");
        }
    }

    @Test
    public void setaffinity() {
        assumeTrue(isUnix() && !isMacOSX(), "Windows and macOS doesn't support 'setaffinity'");

        int gettid = jnr.gettid();
        assertEquals(0, jnr.sched_setaffinity_as(gettid, 1), "sched_setaffinity_as should return 0 when pinning thread to single cpu");
        assertEquals("1-1", jnr.sched_getaffinity_summary(gettid), "thread should be pinned to cpu 1 after setaffinity_as");
        assertEquals(0, jnr.sched_setaffinity_range(gettid, 2, 3), "sched_setaffinity_range should return 0 when pinning thread to cpu range");
        assertEquals("2-3", jnr.sched_getaffinity_summary(gettid), "thread should be pinned to cpus 2-3 after setaffinity_range");
        assertEquals(0, jnr.sched_setaffinity_range(gettid, 0, jnr.get_nprocs_conf()), "sched_setaffinity_range should return 0 when resetting to all cpus");
    }

    @Test
    public void clocks() {
        assumeTrue(isUnix());

        int successCount = 0;
        for (ClockId value : ClockId.values()) {
            try {
                final long gettime = jnr.clock_gettime(value);
                System.out.println(value + ": " + gettime);
                successCount++;
            } catch (IllegalArgumentException e) {
                System.out.println(value + ": " + e);
            }
        }
        assertTrue(successCount > 0, "clock_gettime should support at least one ClockId value on this platform");
    }

    private static boolean isUnix() {
        return Platform.getNativePlatform().isUnix();
    }
}
