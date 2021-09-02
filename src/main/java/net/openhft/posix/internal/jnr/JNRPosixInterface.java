package net.openhft.posix.internal.jnr;

import jnr.ffi.Pointer;

public interface JNRPosixInterface {
    int open(CharSequence path, int flags, int perm);

    int ftruncate(int fd, long offset);

    int fallocate(int fd, int mode, long offset, long length);

    int close(int fd);

    int madvise(long addr, long length, int advise);

    long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);

    int munmap(long addr, long length);

    int msync(long address, long length, int flags);
}
