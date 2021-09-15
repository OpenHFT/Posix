package net.openhft.posix.internal.jnr;

public interface WinJNRPosixInterface {
    // SetFilePointer
    // int ftruncate(int fd, long offset);

    long malloc(long size);

    void free(long ptr);

    int _close(int fd);

    int _open(CharSequence path, int flags, int perm);

    long _read(int fd, long dst, long len);

    long _write(int fd, long src, long len);

    int _getpid();

    String strerror(int errno);
}
