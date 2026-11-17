/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.constants.platform.OpenFlags;

import net.openhft.posix.OpenFlag;

/** Translates stable facade values at the native ABI boundary. */
final class UnixConstants {
    private static final OpenFlag[] OPEN_FLAGS = OpenFlag.values();

    private UnixConstants() {}

    static int openFlags(int flags) {
        int result = flags & 3;
        if (result == 3) return -1;
        int remaining = flags & ~3;
        for (OpenFlag flag : OPEN_FLAGS) {
            int bit = flag.value();
            if (bit <= 2 || (remaining & bit) == 0) continue;
            OpenFlags nativeFlag = OpenFlags.valueOf(flag.name());
            if (!nativeFlag.defined()) return -1;
            result |= nativeFlag.intValue();
            remaining &= ~bit;
        }
        return remaining == 0 ? result : -1;
    }

    static int whence(int whence, boolean darwin) {
        if (darwin && (whence == 3 || whence == 4))
            return 7 - whence; // Darwin: SEEK_HOLE=3, SEEK_DATA=4.
        return whence;
    }

    static int mlock2Syscall(String cpu) {
        switch (cpu) {
            case "aarch64":
                return 284;
            case "arm":
                return 390;
            case "x86_64":
                return 325;
            case "i386":
                return 376;
            default:
                return -1;
        }
    }

    static int gettidSyscall(String cpu) {
        switch (cpu) {
            case "aarch64":
                return 178;
            case "arm":
            case "i386":
                return 224;
            case "x86_64":
                return 186;
            default:
                return -1;
        }
    }
}
