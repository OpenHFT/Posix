package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;

public interface PosixAPI {

    /**
     * @return The fastest available PosixAPI implementation.
     */
    static PosixAPI posix() {
        return PosixAPIHolder.POSIX_API;
    }

    int close(int fd);

    int fallocate(int fd, int mode, long offset, long length);

    int ftruncate(int fd, long offset);

    default long mmap(long addr, long length, MMapProt prot, MMapFlags flags, int fd, long offset) {
        return mmap(addr, length, prot.mode(), flags.mode(), fd, offset);
    }

    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    default boolean msyncSupported(MSyncFlag mode) {
        return false;
    }

    default int msync(long address, long length, MSyncFlag flags) {
        return msync(address, length, flags.mode());
    }

    int msync(long address, long length, int mode);

    int munmap(long addr, long length);

    default int open(CharSequence path, OpenFlags flags, int perm) {
        return open(path, flags.mode(), perm);
    }

    int open(CharSequence path, int flags, int perm);
}
