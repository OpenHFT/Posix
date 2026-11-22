/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.ffm;

import net.openhft.posix.internal.windows.WindowsPosixAPI;

/** Optional Java 22+ Windows provider; no preview features are used. */
public final class WinFFMPosixAPI extends WindowsPosixAPI {
    public WinFFMPosixAPI() { super(new WindowsFFMNative()); }
}

