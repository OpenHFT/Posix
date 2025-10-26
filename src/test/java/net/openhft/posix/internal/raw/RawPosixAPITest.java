package net.openhft.posix.internal.raw;

import net.openhft.posix.MclFlag;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Ensures {@link RawPosixAPI} can be subclassed without extra wiring and that
 * default memory-lock helpers retain their documented no-op behaviour.
 */
public class RawPosixAPITest {

    private final DummyRaw api = new DummyRaw();

    @Test
    public void defaultMemoryLockHelpersReturnFalseOrNoOp() {
        assertFalse(api.mlock(0L, 128));
        assertFalse(api.mlock2(0L, 128, true));
        api.mlockall(MclFlag.MclCurrent); // should not throw
    }

    private static final class DummyRaw extends RawPosixAPI {
        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Raw stub only exercises defaults");
        }

        @Override
        public int open(CharSequence path, int flags, int perm) {
            throw unsupported();
        }

        @Override
        public long lseek(int fd, long offset, int whence) {
            throw unsupported();
        }

        @Override
        public int ftruncate(int fd, long offset) {
            throw unsupported();
        }

        @Override
        public int lockf(int fd, int cmd, long len) {
            throw unsupported();
        }

        @Override
        public int close(int fd) {
            throw unsupported();
        }

        @Override
        public int fallocate(int fd, int mode, long offset, long length) {
            throw unsupported();
        }

        @Override
        public int madvise(long addr, long length, int advice) {
            throw unsupported();
        }

        @Override
        public int msync(long address, long length, int mode) {
            throw unsupported();
        }

        @Override
        public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
            throw unsupported();
        }

        @Override
        public int munmap(long addr, long length) {
            throw unsupported();
        }

        @Override
        public long read(int fd, long dst, long len) {
            throw unsupported();
        }

        @Override
        public long write(int fd, long src, long len) {
            throw unsupported();
        }

        @Override
        public int gettimeofday(long timeval) {
            throw unsupported();
        }

        @Override
        public int sched_setaffinity(int pid, int cpusetsize, long mask) {
            throw unsupported();
        }

        @Override
        public int sched_getaffinity(int pid, int cpusetsize, long mask) {
            throw unsupported();
        }

        @Override
        public int lastError() {
            throw unsupported();
        }

        @Override
        public long clock_gettime(int clockId) {
            throw unsupported();
        }

        @Override
        public long malloc(long size) {
            throw unsupported();
        }

        @Override
        public void free(long ptr) {
            throw unsupported();
        }

        @Override
        public int get_nprocs() {
            throw unsupported();
        }

        @Override
        public int get_nprocs_conf() {
            throw unsupported();
        }

        @Override
        public int getpid() {
            throw unsupported();
        }

        @Override
        public int gettid() {
            throw unsupported();
        }

        @Override
        public String strerror(int errno) {
            throw unsupported();
        }
    }
}
