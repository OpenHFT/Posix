/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

/** Translates stable facade values at the native ABI boundary. */
final class UnixConstants {
    private UnixConstants() {}

    static int whence(int whence, boolean darwin) {
        if (darwin && (whence == 3 || whence == 4))
            return 7 - whence; // Darwin: SEEK_HOLE=3, SEEK_DATA=4.
        return whence;
    }
}
