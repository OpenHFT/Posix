package net.openhft.posix.internal.jnr;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import jnr.ffi.provider.FFIProvider;
import net.openhft.posix.MSyncFlag;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jna.JNAPosixAPI;

public class JNRPosixAPI implements PosixAPI {

    static final jnr.ffi.Runtime RUNTIME = FFIProvider.getSystemProvider().getRuntime();
    static final jnr.ffi.Platform NATIVE_PLATFORM = Platform.getNativePlatform();
    static final String STANDARD_C_LIBRARY_NAME = NATIVE_PLATFORM.getStandardCLibraryName();

    // TODO FIX so this isn't needed.
    static final JNAPosixAPI fallback = new JNAPosixAPI();

    private final JNRPosixInterface jnr;

    public JNRPosixAPI() {
        LibraryLoader<JNRPosixInterface> loader = LibraryLoader.create(JNRPosixInterface.class);
        loader.library(STANDARD_C_LIBRARY_NAME);
        jnr = loader.load();
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        return jnr.open(path, flags, perm);
    }

    @Override
    public int ftruncate(int fd, long offset) {
        return jnr.ftruncate(fd, offset);
    }

    @Override
    public int close(int fd) {
        return jnr.close(fd);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return fallback.mmap(addr, length, prot, flags, fd, offset);

/*
        final long mmap = jnr.mmap(addr, length, prot, flags, fd, offset);
        if (mmap == 0 || mmap == -1) {
            final int lastError = RUNTIME.getLastError();
            for (Errno errno : Errno.values()) {
                if (errno.intValue() == lastError)
                    throw new RuntimeException(errno.toString());
            }
            throw new RuntimeException("Unknown errno " + lastError);
        }
        return mmap;
*/
    }

    @Override
    public int munmap(long addr, long length) {
        return jnr.munmap(addr, length);
    }

    @Override
    public boolean msyncSupported(MSyncFlag mode) {
        return true;
    }

    @Override
    public int msync(long address, long length, int flags) {
        return jnr.msync(address, length, flags);
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return jnr.fallocate(fd, mode, offset, length);
    }

    @Override
    public int madvise(long addr, long length, int advice) {
        return jnr.madvise(addr, length, advice);
    }
}
