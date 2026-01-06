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
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ProcMapsTest {

    @Test
    public void forSelfReadsCurrentProcessMappingsWhenProcAvailable() throws IOException {
        // Skip on platforms without /proc
        assumeTrue(Files.exists(Paths.get("/proc/self/maps")), "/proc/self/maps must exist");

        ProcMaps maps = ProcMaps.forSelf();
        List<Mapping> list = maps.list();

        assertNotNull(list, "list() should not return null");
        assertFalse(list.isEmpty(), "expected at least one mapping");

        Mapping first = list.get(0);
        Mapping viaFindFirst = maps.findFirst(m -> true);
        assertEquals(first.toString(), viaFindFirst.toString(), "findFirst(true) should return the first mapping");
    }

    @Test
    public void forPidFailsForNonExistentProcessWhenProcAvailable() {
        assumeTrue(Files.exists(Paths.get("/proc")), "/proc must exist");

        int unlikelyPid = 999999;
        assumeFalse(Files.exists(Paths.get("/proc/" + unlikelyPid)), "unexpected pid exists: " + unlikelyPid);

        assertThrows(IOException.class, () -> ProcMaps.forPID(unlikelyPid), "forPID should throw for a non-existent pid");
    }
}
