/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class ProcMapsEdgeCaseTest {

    @Test
    public void findAllFiltersByPathSubstring() throws IOException {
        ProcMaps maps = new ProcMaps(new BufferedReader(new StringReader(
                "00400000-00401000 r-xp 00000000 08:02 1 /bin/example\n" +
                "00500000-00501000 rw-p 00000000 00:00 0\n" +
                "00600000-00601000 r--p 00001000 08:02 2 /lib/example.so\n")));
        List<Mapping> all = maps.list();
        assertEquals(3, all.size());

        List<Mapping> withSlash = maps.findAll(m -> m.path().contains("/"));
        assertEquals(2, withSlash.size());
        for (Mapping m : withSlash) {
            assertTrue(m.path().contains("/"));
        }
        assertEquals("/bin/example", maps.findFirst(m -> m.path().contains("/bin/")).path());
        assertThrows(UnsupportedOperationException.class, () -> maps.list().clear());
    }

    @Test(expected = RuntimeException.class)
    public void invalidMappingLineFailsFast() {
        // Deliberately missing fields to trigger parser failure
        new Mapping("not-a-valid-mapping-line");
    }
}

