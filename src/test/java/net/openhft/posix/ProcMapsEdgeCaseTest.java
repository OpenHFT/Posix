/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ProcMapsEdgeCaseTest {

    @Test
    public void findAllFiltersByPathSubstringWhenProcAvailable() throws IOException {
        assumeTrue(Files.exists(Paths.get("/proc/self/maps")), "/proc/self/maps must exist");

        ProcMaps maps = ProcMaps.forSelf();
        List<Mapping> all = maps.list();
        assertFalse(all.isEmpty(), "expected at least one mapping from /proc/self/maps");

        List<Mapping> withSlash = maps.findAll(m -> m.path().contains("/"));
        for (Mapping m : withSlash) {
            assertTrue(m.path().contains("/"), "filtered mapping should contain '/': " + m.path());
        }
    }

    @Test
    public void invalidMappingLineFailsFast() {
        // Deliberately missing fields to trigger parser failure
        assertThrows(RuntimeException.class, () -> new Mapping("not-a-valid-mapping-line"), "invalid mapping line should fail fast");
    }
}
