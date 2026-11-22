/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LinuxSyscallsTest {
    @Test
    public void distinguishesArmAbisAndRejectsUnknownArchitectures() {
        assertEquals(284, LinuxSyscalls.mlock2("aarch64"));
        assertEquals(390, LinuxSyscalls.mlock2("arm"));
        assertEquals(325, LinuxSyscalls.mlock2("x86_64"));
        assertEquals(376, LinuxSyscalls.mlock2("i386"));
        assertEquals(-1, LinuxSyscalls.mlock2("unknown"));
    }
}
