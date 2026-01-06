/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PosixAPIDefaultsTest {

    @Test
    public void lseekEnumOverloadDelegatesToIntWhence() {
        RecordingPosix stub = new RecordingPosix();
        final long result = stub.lseek(7, 123L, WhenceFlag.SEEK_CUR);

        assertEquals(7, stub.lastLseekFd, "fd forwarded to int overload");
        assertEquals(123L, stub.lastLseekOffset, "offset forwarded to int overload");
        assertEquals(WhenceFlag.SEEK_CUR.value(), stub.lastLseekWhence, "whence forwarded to int overload");
        assertEquals(RecordingPosix.LSEEK_RESULT, result, "return value forwarded from int overload");
    }

    @Test
    public void mmapEnumOverloadDelegatesToBitFlags() {
        RecordingPosix stub = new RecordingPosix();
        final long addr = stub.mmap(0L, 4096L, MMapProt.PROT_READ, MMapFlag.SHARED, 5, 0L);

        assertEquals(0L, stub.lastMmapAddr, "addr forwarded to int overload");
        assertEquals(4096L, stub.lastMmapLength, "length forwarded to int overload");
        assertEquals(MMapProt.PROT_READ.value(), stub.lastMmapProt, "prot forwarded to int overload");
        assertEquals(MMapFlag.SHARED.value(), stub.lastMmapFlags, "flags forwarded to int overload");
        assertEquals(5, stub.lastMmapFd, "fd forwarded to int overload");
        assertEquals(0L, stub.lastMmapOffset, "offset forwarded to int overload");
        assertEquals(RecordingPosix.MMAP_RESULT, addr, "return value forwarded from int overload");
    }

    @Test
    public void mlockDefaultReturnsFalse() {
        RecordingPosix stub = new RecordingPosix();
        assertFalse(stub.mlock(0L, 1024L), "mlock default should return false");
    }

    @Test
    public void mlockallDefaultDoesNotThrow() {
        RecordingPosix stub = new RecordingPosix();
        stub.mlockall(MclFlag.MclCurrent);
        // default implementation is a no-op and must not throw
        assertEquals(0, stub.lastError(), "mlockall default should not affect lastError");
    }

    private static final class RecordingPosix implements PosixAPI {
        static final long LSEEK_RESULT = 1234L;
        static final long MMAP_RESULT = 0xCAFEBABEL;

        int lastLseekFd;
        long lastLseekOffset;
        int lastLseekWhence;

        long lastMmapAddr;
        long lastMmapLength;
        int lastMmapProt;
        int lastMmapFlags;
        int lastMmapFd;
        long lastMmapOffset;

        @Override
        public int close(int fd) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int fallocate(int fd, int mode, long offset, long length) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int ftruncate(int fd, long offset) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public long lseek(int fd, long offset, int whence) {
            lastLseekFd = fd;
            lastLseekOffset = offset;
            lastLseekWhence = whence;
            return LSEEK_RESULT;
        }

        @Override
        public int lockf(int fd, int cmd, long len) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int madvise(long addr, long length, int advice) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
            lastMmapAddr = addr;
            lastMmapLength = length;
            lastMmapProt = prot;
            lastMmapFlags = flags;
            lastMmapFd = fd;
            lastMmapOffset = offset;
            return MMAP_RESULT;
        }

        @Override
        public int msync(long address, long length, int mode) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int munmap(long addr, long length) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int open(CharSequence path, int flags, int perm) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public long read(int fd, long dst, long len) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public long write(int fd, long src, long len) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int gettimeofday(long timeval) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int sched_setaffinity(int pid, int cpusetsize, long mask) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int sched_getaffinity(int pid, int cpusetsize, long mask) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int lastError() {
            return 0;
        }

        @Override
        public long clock_gettime(int clockId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public long malloc(long size) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void free(long ptr) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int get_nprocs() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int get_nprocs_conf() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int getpid() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public int gettid() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public String strerror(int errno) {
            return null;
        }
    }
}
