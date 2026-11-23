/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;

/** Run in old JVMs with the modern optional jar on the classpath. */
public class WindowsProviderProbe {
    public static void main(String[] args) {
        System.setProperty("chronicle.posix.provider", args[0]);
        PosixAPIHolder.POSIX_API = null;
        try {
            PosixAPI api = PosixAPI.posix();
            if (!api.getClass().getSimpleName().equals(args[1]))
                throw new AssertionError("Unexpected provider: " + api.getClass());
            if (api.getpid() <= 0) throw new AssertionError("Invalid PID");
        } catch (UnsupportedOperationException e) {
            if (!"unavailable".equals(args[1]) || !e.getMessage().contains("Java 22+")) throw e;
        }
        System.out.println("PASS " + System.getProperty("java.version") + " " + args[0] + " -> " + args[1]);
    }
}
