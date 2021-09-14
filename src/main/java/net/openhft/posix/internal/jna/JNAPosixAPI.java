package net.openhft.posix.internal.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import net.openhft.posix.PosixAPI;

public abstract class JNAPosixAPI implements PosixAPI {
    private static final Pointer NULL = Pointer.createConstant(0);
    private final JNAPosixInterface jna = new JNAPosixInterface();

    public JNAPosixAPI() {
        NativeLibrary clib = NativeLibrary.getInstance(Platform.C_LIBRARY_NAME);
        Native.register(JNAPosixInterface.class, clib);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return jna.mmap(addr == 0 ? NULL : Pointer.createConstant(addr), length, prot, flags, fd, offset);
    }
}
