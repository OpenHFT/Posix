/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnsignedMappingTest {
    @Test
    public void retainsUnsignedInodeAndPathWithSpaces() {
        Mapping mapping = new Mapping("1000-2000 rw-p 00000000 08:02 18446744073709551615 /tmp/file with spaces");
        assertEquals(-1L, mapping.inode());
        assertEquals("18446744073709551615", Long.toUnsignedString(mapping.inode()));
        assertEquals("/tmp/file with spaces", mapping.path());
        assertEquals(4096, mapping.length());
    }
}
