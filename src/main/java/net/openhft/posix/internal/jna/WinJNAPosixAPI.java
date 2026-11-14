/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jna;

import net.openhft.posix.internal.windows.WindowsPosixAPI;

/** Windows x64 provider using JNA. */
public final class WinJNAPosixAPI extends WindowsPosixAPI {
    public WinJNAPosixAPI() { super(new WindowsJNANative()); }
}

