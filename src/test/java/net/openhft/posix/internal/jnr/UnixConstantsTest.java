/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import static org.junit.Assert.assertEquals;

import net.openhft.posix.WhenceFlag;

import org.junit.Test;

public class UnixConstantsTest {
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
