package net.openhft.posix.internal.jnr;

public interface JNRPosixInterface {
    int open(CharSequence path, int flags, int perm);

    int ftruncate(int fd, long offset);

    int fallocate(int fd, int mode, long offset, long length);

    int close(int fd);

    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    int munmap(long addr, long length);

    int msync(long address, long length, int flags);
}
