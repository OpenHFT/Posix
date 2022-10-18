package net.openhft.posix.internal.noop;

import net.openhft.posix.PosixAPI;
import net.openhft.posix.PosixRuntimeException;

/**
 * No-Op Posix. Each method does nothing and returns 0 (no error)
 */
public class NoOpPosixAPI implements PosixAPI {
    @Override
    public int close(int fd) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    // inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return 0;
    }

    // inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int ftruncate(int fd, long offset) {
        return 0;
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    // possibly inefficient if not provided, but safe to no-op
    @Override
    public int madvise(long addr, long length, int advice) {
        return 0;
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    // possibly inefficient if not provided, but safe to no-op (on linux, mac; windows tbc)
    @Override
    public int msync(long address, long length, int mode) {
        return 0;
    }

    @Override
    public int munmap(long addr, long length) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public long read(int fd, long dst, long len) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public long write(int fd, long src, long len) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int gettimeofday(long timeval) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    // possibly inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        return 0;
    }

    // "all-cores" for no-op. possibly inefficient, but calling code should tolerate this return
    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int lastError() {
        return 0;
    }

    @Override
    public long clock_gettime(int clockId) throws IllegalArgumentException {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public long malloc(long size) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public void free(long ptr) {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int get_nprocs() {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int get_nprocs_conf() {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int getpid() {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    @Override
    public int gettid() {
        throw new PosixRuntimeException("POSIX implementation missing");
    }

    // slight loss of info on no-op
    @Override
    public String strerror(int errno) {
        return null;
    }
}
