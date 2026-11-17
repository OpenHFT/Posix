/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import static org.junit.Assert.assertEquals;

import net.openhft.posix.WhenceFlag;

import org.junit.Test;

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
        assertEquals(4, UnixConstants.whence(WhenceFlag.SEEK_DATA.value(), true));
        assertEquals(3, UnixConstants.whence(WhenceFlag.SEEK_HOLE.value(), true));
        for (int whence = 0; whence <= 4; whence++)
            assertEquals(whence, UnixConstants.whence(whence, false));
        for (int whence = 0; whence <= 2; whence++)
            assertEquals(whence, UnixConstants.whence(whence, true));
        assertEquals(-1, UnixConstants.whence(-1, true));
        assertEquals(5, UnixConstants.whence(5, true));
    }
}
