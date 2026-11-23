/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import net.openhft.posix.internal.windows.*;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.*;

public class WindowsMappingFailureTest {
    @Test
    public void releasesOwnedHandlesOnBothMappingFailurePaths() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        for (boolean createFails : new boolean[] {true, false}) {
            long errno = UNSAFE.allocateMemory(8);
            Set<Long> closed = new HashSet<>();
            WindowsNative binding =
                    (WindowsNative)
                            Proxy.newProxyInstance(
                                    WindowsNative.class.getClassLoader(),
                                    new Class<?>[] {WindowsNative.class},
                                    (proxy, method, args) -> {
                                        switch (method.getName()) {
                                            case "_errno":
                                                return errno;
                                            case "_get_osfhandle":
                                                return 100L;
                                            case "GetSystemInfo":
                                                UNSAFE.putInt((long) args[0] + 40, 65536);
                                                return null;
                                            case "GetFileSizeEx":
                                                UNSAFE.putLong((long) args[1], 65536);
                                                return 1;
                                            case "GetCurrentProcess":
                                                return -1L;
                                            case "DuplicateHandle":
                                                UNSAFE.putLong((long) args[3], 200);
                                                return 1;
                                            case "CreateFileMappingW":
                                                return createFails ? 0L : 300L;
                                            case "MapViewOfFile":
                                                return 0L;
                                            case "winError":
                                                return 5;
                                            case "CloseHandle":
                                                assertTrue(closed.add((Long) args[0]));
                                                return 1;
                                            default:
                                                throw new AssertionError(
                                                        "Unexpected call " + method.getName());
                                        }
                                    });
            try {
                PosixAPI api = new WindowsPosixAPI(binding);
                assertEquals(-1, api.mmap(0, 4096, 3, 1, 1, 0));
                assertEquals(13, api.lastError());
                assertTrue(closed.contains(200L));
                assertEquals(!createFails, closed.contains(300L));
                assertFalse(closed.contains(100L));
                assertEquals(-1, api.munmap(123, 4096));
                assertEquals(22, api.lastError());
            } finally {
                UNSAFE.freeMemory(errno);
            }
        }
    }
}
