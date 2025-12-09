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

public class ProcMapsTest {

    @Test
    public void forSelfReadsCurrentProcessMappingsWhenProcAvailable() throws IOException {
        // Skip on platforms without /proc
        Assume.assumeTrue(Files.exists(Paths.get("/proc/self/maps")));

        ProcMaps maps = ProcMaps.forSelf();
        List<Mapping> list = maps.list();

        assertNotNull(list);
        assertFalse("Expected at least one mapping", list.isEmpty());

        Mapping first = list.get(0);
        Mapping viaFindFirst = maps.findFirst(m -> true);
        assertEquals(first.toString(), viaFindFirst.toString());
    }

    @Test
    public void forPidFailsForNonExistentProcessWhenProcAvailable() {
        Assume.assumeTrue(Files.exists(Paths.get("/proc")));

        int unlikelyPid = 999999;
        if (Files.exists(Paths.get("/proc/" + unlikelyPid))) {
            // If this pid happens to exist on the build agent, skip the check
            return;
        }

        assertThrows(IOException.class, () -> ProcMaps.forPID(unlikelyPid));
    }
}
