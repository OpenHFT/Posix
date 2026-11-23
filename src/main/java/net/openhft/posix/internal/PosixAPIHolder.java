/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal;

import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.jna.WinJNAPosixAPI;
import net.openhft.posix.internal.jna.WinJNADirectPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Holds the selected {@link PosixAPI} provider for this JVM.
 * <p>The fallback order is {@code JNRPosixAPI},
 * {@code WinJNRPosixAPI} then {@code NoOpPosixAPI}.</p>
 */
public class PosixAPIHolder {
    /** Selected provider instance once initialised. */
    public static volatile PosixAPI POSIX_API;

    /**
     * Loads the fastest compatible provider into {@link #POSIX_API}.
     * Serialises first use and preserves caller interrupts throughout native loading.
     * Providers are tried in the order {@code JNRPosixAPI},
     * {@code WinJNRPosixAPI} then {@code NoOpPosixAPI}
     * (see POSIX-FN-002).
     */
    public static synchronized void loadPosixApi() {
        if (POSIX_API != null)
            return;

        runWithoutInterruption(PosixAPIHolder::loadRequestedProvider);
    }

    static void runWithoutInterruption(Runnable action) {
        // An interrupt arriving during extraction can poison native static initialisers.
        // Isolate the first load from shutdown interrupts on its caller.
        FutureTask<Void> load = new FutureTask<>(action, null);
        Thread loader = new Thread(load, "chronicle-posix-init");
        loader.setDaemon(true);
        boolean interrupted = Thread.interrupted();
        try {
            loader.start();
            for (;;) {
                try {
                    load.get();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Error)
                        throw (Error) cause;
                    if (cause instanceof RuntimeException)
                        throw (RuntimeException) cause;
                    throw new IllegalStateException("Native provider initialisation failed", cause);
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    private static void loadRequestedProvider() {

        String requested = System.getProperty("chronicle.posix.provider", "auto");
        if (!"auto".equals(requested)) {
            if ("noop".equals(requested)) {
                // The caller holds the holder monitor while waiting for this loader.
                POSIX_API = new NoOpPosixAPI("Explicitly disabled");
                return;
            }
            if ("jnr".equals(requested)) {
                POSIX_API = Platform.getNativePlatform().isUnix() ? new JNRPosixAPI() : new WinJNRPosixAPI();
            } else if ("jna".equals(requested)) {
                POSIX_API = new WinJNAPosixAPI();
            } else if ("jna-direct".equals(requested)) {
                POSIX_API = new WinJNADirectPosixAPI();
            } else if ("ffm".equals(requested)) {
                String version = System.getProperty("java.specification.version");
                if (version.startsWith("1.") || Integer.parseInt(version) < 22)
                    throw new UnsupportedOperationException("FFM requires Java 22+ and the optional posix-ffm artifact");
                try {
                    POSIX_API = (PosixAPI) Class.forName("net.openhft.posix.ffm.WinFFMPosixAPI").getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot load requested FFM provider; add posix-ffm and enable native access", e);
                }
            } else {
                throw new IllegalArgumentException("Unknown POSIX provider: " + requested);
            }
            return;
        }

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
    public static synchronized void useNoOpPosixApi() {
        POSIX_API = new NoOpPosixAPI("Explicitly disabled");
    }
}
