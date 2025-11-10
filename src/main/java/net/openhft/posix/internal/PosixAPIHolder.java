//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix.internal;

import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;

/**
 * Holds the selected {@link PosixAPI} provider for this JVM.
 * <p>The fallback order is {@code JNRPosixAPI},
 * {@code WinJNRPosixAPI} then {@code NoOpPosixAPI}.</p>
 */
public class PosixAPIHolder {
    /** Selected provider instance once initialised. */
    public static PosixAPI POSIX_API;

    /**
     * Loads the fastest compatible provider into {@link #POSIX_API}.
     * Not thread-safe while {@link #POSIX_API} is {@code null}.
     * Providers are tried in the order {@code JNRPosixAPI},
     * {@code WinJNRPosixAPI} then {@code NoOpPosixAPI}
     * (see POSIX-FN-002).
     */
    public static void loadPosixApi() {
        if (POSIX_API != null)
            return;

        PosixAPI posixAPI;
        try {
            // Check if the native platform is Unix and load the appropriate API
            posixAPI = Platform.getNativePlatform().isUnix()
                    ? new JNRPosixAPI()
                    : new WinJNRPosixAPI();
        } catch (Throwable t) {
            // Fallback to NoOpPosixAPI if an error occurs
            posixAPI = new NoOpPosixAPI(t.toString());
        }
        POSIX_API = posixAPI;
    }

    /**
     * Switches {@link #POSIX_API} to the no-op provider.
     */
    public static void useNoOpPosixApi() {
        POSIX_API = new NoOpPosixAPI("Explicitly disabled");
    }
}
