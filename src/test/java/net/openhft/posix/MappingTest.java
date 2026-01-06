/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MappingTest {

    @Test
    public void parsesMappingLineWithPath() {
        String line = "00400000-0040b000 r-xp 00000000 08:02 367546 /bin/cat";
        Mapping mapping = new Mapping(line);

        assertEquals(0x00400000L, mapping.addr(), "addr parsed from mapping line");
        assertEquals(0x0040b000L - 0x00400000L, mapping.length(), "length parsed from mapping line");
        assertEquals("r-xp", mapping.perms(), "perms parsed from mapping line");
        assertEquals("08:02", mapping.device(), "device parsed from mapping line");
        assertEquals(367546L, mapping.inode(), "inode parsed from mapping line");
        assertEquals("/bin/cat", mapping.path(), "path parsed from mapping line");
        assertEquals(line, mapping.toString(), "toString should match original line");
    }

    @Test
    public void parsesMappingLineWithoutPath() {
        String line = "7f9c9a000000-7f9c9a021000 rw-p 00000000 00:00 0";
        Mapping mapping = new Mapping(line);

        assertEquals("rw-p", mapping.perms(), "perms parsed from mapping line");
        assertEquals("00:00", mapping.device(), "device parsed from mapping line");
        assertEquals(0L, mapping.inode(), "inode parsed from mapping line");
        assertEquals("", mapping.path(), "path should be empty when absent");
        assertTrue(mapping.length() > 0, "length should be positive");
    }
}
