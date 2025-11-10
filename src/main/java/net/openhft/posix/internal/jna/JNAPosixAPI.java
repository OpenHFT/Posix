//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix.internal.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import net.openhft.posix.PosixAPI;

/**
 * Abstract {@link PosixAPI} based on JNA (Java Native Access).
 *
 * <p>Instantiating this class loads the platform C library via
 * {@code NativeLibrary.getInstance}.  The search honours the
 * {@code jna.library.path} system property.  The library is registered with
 * {@code Native.register} and therefore cannot be unloaded for the lifetime of
 * the JVM.</p>
 */
public abstract class JNAPosixAPI implements PosixAPI {
    private static final Pointer NULL = Pointer.createConstant(0);

    // JNA interface for POSIX functions
    private final JNAPosixInterface jna = new JNAPosixInterface();

    /**
     * Constructs a JNAPosixAPI and registers the POSIX natives.  The
     * registration is global; JNA does not support unloading once registered.
     */
    public JNAPosixAPI() {
        NativeLibrary clib = NativeLibrary.getInstance(Platform.C_LIBRARY_NAME);
        Native.register(JNAPosixInterface.class, clib);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return jna.mmap(addr == 0 ? NULL : Pointer.createConstant(addr), length, prot, flags, fd, offset);
    }
}
