/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MappingTest {

    @Test
    public void parsesMappingLineWithPath() {
        String line = "00400000-0040b000 r-xp 00000000 08:02 367546 /bin/cat";
        Mapping mapping = new Mapping(line);

        assertEquals(0x00400000L, mapping.addr());
        assertEquals(0x0040b000L - 0x00400000L, mapping.length());
        assertEquals("r-xp", mapping.perms());
        assertEquals("08:02", mapping.device());
        assertEquals(367546L, mapping.inode());
        assertEquals("/bin/cat", mapping.path());
        assertEquals(line, mapping.toString());
    }

    @Test
    public void parsesMappingLineWithoutPath() {
        String line = "7f9c9a000000-7f9c9a021000 rw-p 00000000 00:00 0";
        Mapping mapping = new Mapping(line);

        assertEquals("rw-p", mapping.perms());
        assertEquals("00:00", mapping.device());
        assertEquals(0L, mapping.inode());
        assertEquals("", mapping.path());
        assertTrue(mapping.length() > 0);
    }
}

