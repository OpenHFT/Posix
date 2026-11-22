/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

import net.openhft.posix.internal.windows.WindowsNative;
import net.openhft.posix.internal.windows.WindowsPosixAPI;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class WindowsAllocationFailureTest {
    private final OutOfMemoryError allocationFailure =
            new OutOfMemoryError("injected scratch failure");
    private final List<Long> closedHandles = new ArrayList<>();
    private final List<Integer> closedDescriptors = new ArrayList<>();

    @Before
    public void requireWindows64() {
        assumeTrue(
                System.getProperty("os.name").startsWith("Windows") && UNSAFE.addressSize() == 8);
    }

    @Test
    public void releasesOwnedThreadHandlesWhenScratchAllocationFails() {
        checkAffinityFailure(42);
        assertEquals(java.util.Arrays.asList(101L, 101L), closedHandles);
    }

    @Test
    public void neverClosesCurrentThreadPseudoHandle() {
        checkAffinityFailure(0);
        assertEquals(0, closedHandles.size());
    }

    @Test
    public void releasesDescriptorZeroWhenDiskUsageAllocationFails() {
        WindowsPosixAPI api = failingProvider();
        assertSame(allocationFailure, assertThrows(OutOfMemoryError.class, () -> api.du("unused")));
        assertEquals(java.util.Collections.singletonList(0), closedDescriptors);
    }

    private void checkAffinityFailure(int pid) {
        WindowsPosixAPI api = failingProvider();
        long mask = UNSAFE.allocateMemory(8);
        try {
            UNSAFE.putLong(mask, 1);
            assertSame(
                    allocationFailure,
                    assertThrows(
                            OutOfMemoryError.class, () -> api.sched_getaffinity(pid, 8, mask)));
            assertSame(
                    allocationFailure,
                    assertThrows(
                            OutOfMemoryError.class, () -> api.sched_setaffinity(pid, 8, mask)));
        } finally {
            UNSAFE.freeMemory(mask);
        }
    }

    private WindowsPosixAPI failingProvider() {
        WindowsNative binding =
                (WindowsNative)
                        Proxy.newProxyInstance(
                                WindowsNative.class.getClassLoader(),
                                new Class<?>[] {WindowsNative.class},
                                (proxy, method, args) -> {
                                    switch (method.getName()) {
                                        case "GetMaximumProcessorCount":
                                            return 8;
                                        case "OpenThread":
                                            return 101L;
                                        case "GetCurrentThread":
                                            return -2L;
                                        case "CloseHandle":
                                            closedHandles.add((Long) args[0]);
                                            return 1;
                                        default:
                                            throw new AssertionError(
                                                    "Unexpected call " + method.getName());
                                    }
                                });
        return new WindowsPosixAPI(binding) {
            @Override
            public long malloc(long size) {
                throw allocationFailure;
            }

            @Override
            public int open(CharSequence path, int flags, int perm) {
                return 0;
            }

            @Override
            public int close(int fd) {
                closedDescriptors.add(fd);
                return 0;
            }
        };
    }
}
