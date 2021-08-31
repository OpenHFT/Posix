package net.openhft.posix.internal.raw;

import jnr.constants.platform.Errno;
import net.openhft.posix.PosixAPI;

public class RawPosixAPI implements PosixAPI {
    @Override
    public int open(CharSequence path, int flags, int perm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int ftruncate(int fd, long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int close(int fd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int munmap(long addr, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int msync(long address, long length, int mode) {
        return Errno.EPERM.value();
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return 0;
    }
}
