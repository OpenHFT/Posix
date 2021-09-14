package net.openhft.posix.internal.jnr;

import jnr.ffi.Pointer;

public interface JNRPosixInterface {
    int open(CharSequence path, int flags, int perm);

    long read(int fd, long dst, long len);

    long write(int fd, long src, long len);

    int ftruncate(int fd, long offset);

    int fallocate(int fd, int mode, long offset, long length);

    int close(int fd);

    int madvise(long addr, long length, int advise);

    long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);

    int munmap(long addr, long length);

    int msync(long address, long length, int flags);

    int gettimeofday(long timeval, long alwaysNull);

    long malloc(long size);

    void free(long ptr);

    int get_nprocs();

    int get_nprocs_conf();

    int sched_setaffinity(int pid, int cpusetsize, Pointer mask);

    int sched_getaffinity(int pid, int cpusetsize, Pointer mask);

    int gettid();
}
