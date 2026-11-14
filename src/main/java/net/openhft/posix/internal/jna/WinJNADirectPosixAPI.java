/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jna;

import net.openhft.posix.internal.windows.WindowsPosixAPI;

/** Optional direct-mapped JNA Windows provider. */
public final class WinJNADirectPosixAPI extends WindowsPosixAPI {
    public WinJNADirectPosixAPI() { super(new WindowsJNADirectNative()); }
}

