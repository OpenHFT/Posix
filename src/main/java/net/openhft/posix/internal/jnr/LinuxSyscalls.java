/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

/** Linux syscall numbers differ between 32-bit ARM and AArch64. */
final class LinuxSyscalls {
    private LinuxSyscalls() {
    }

    static int mlock2(String cpu) {
        switch (cpu) {
            case "aarch64": return 284;
            case "arm": return 390;
            case "x86_64": return 325;
            case "i386": return 376;
            default: return -1;
        }
    }
}
