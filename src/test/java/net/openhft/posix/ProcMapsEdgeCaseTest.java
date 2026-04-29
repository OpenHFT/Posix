/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class ProcMapsEdgeCaseTest {

    @Test
    public void findAllFiltersByPathSubstringWhenProcAvailable() throws IOException {
        Assume.assumeTrue(Files.exists(Paths.get("/proc/self/maps")));

        ProcMaps maps = ProcMaps.forSelf();
        List<Mapping> all = maps.list();
        assertFalse(all.isEmpty());

        List<Mapping> withSlash = maps.findAll(m -> m.path().contains("/"));
        for (Mapping m : withSlash) {
            assertTrue(m.path().contains("/"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void invalidMappingLineFailsFast() {
        // Deliberately missing fields to trigger parser failure
        new Mapping("not-a-valid-mapping-line");
    }
}

