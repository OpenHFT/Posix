/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnixConstantsTest {
    @Test
    public void distinguishesArmAbisAndRejectsUnknownSyscalls() {
        assertEquals(284, UnixConstants.mlock2Syscall("aarch64"));
        assertEquals(390, UnixConstants.mlock2Syscall("arm"));
        assertEquals(325, UnixConstants.mlock2Syscall("x86_64"));
        assertEquals(376, UnixConstants.mlock2Syscall("i386"));
        assertEquals(-1, UnixConstants.mlock2Syscall("unknown"));
        assertEquals(178, UnixConstants.gettidSyscall("aarch64"));
        assertEquals(-1, UnixConstants.gettidSyscall("unknown"));
    }

    @Test
    public void sparseSeekUsesDarwinConstantsWithoutChangingPublicValues() {
        assertEquals(4, UnixConstants.whence(3, true));
        assertEquals(3, UnixConstants.whence(4, true));
        for (int whence = 0; whence <= 4; whence++)
            assertEquals(whence, UnixConstants.whence(whence, false));
        for (int whence = 0; whence <= 2; whence++)
            assertEquals(whence, UnixConstants.whence(whence, true));
    }
}
