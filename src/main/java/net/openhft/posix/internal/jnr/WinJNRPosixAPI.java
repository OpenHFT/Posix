/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import net.openhft.posix.internal.windows.WindowsPosixAPI;

/** Windows x64 provider using JNR. */
public final class WinJNRPosixAPI extends WindowsPosixAPI {
    public WinJNRPosixAPI() {
        super(new WindowsJNRNative());
    }
}
