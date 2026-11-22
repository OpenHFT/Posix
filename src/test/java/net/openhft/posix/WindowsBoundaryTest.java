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

public class WindowsBoundaryTest {
    @Test
    public void capNativeCountsWithoutLargeBuffers() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        int[] count = {0};
        WindowsNative nativeApi =
                (WindowsNative)
                        Proxy.newProxyInstance(
                                WindowsNative.class.getClassLoader(),
                                new Class<?>[] {WindowsNative.class},
                                (proxy, method, args) -> {
                                    if (method.getName().equals("_read")
                                            || method.getName().equals("_write")) {
                                        count[0] = (int) args[2];
                                        return 17;
                                    }
                                    throw new AssertionError(method.getName());
                                });
        PosixAPI api = new WindowsPosixAPI(nativeApi);
        assertEquals(17, api.read(1, 1, Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, count[0]);
        assertEquals(17, api.write(1, 1, 1L << 32));
        assertEquals(Integer.MAX_VALUE, count[0]);
    }

    @Test
    public void affinityMasksAcrossWordBoundaries() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        WindowsPosixAPI api =
                new WindowsPosixAPI(null) {
                    byte[] expected = new byte[24];
                    long allocation;

                    @Override
                    public int get_nprocs_conf() {
                        return 129;
                    }

                    @Override
                    public long malloc(long length) {
                        assertEquals(24, length);
                        allocation = UNSAFE.allocateMemory(length + 16);
                        UNSAFE.setMemory(allocation, length + 16, (byte) 0x5a);
                        return allocation + 8;
                    }

                    @Override
                    public void free(long pointer) {
                        assertEquals(0x5a5a5a5a5a5a5a5aL, UNSAFE.getLong(pointer - 8));
                        assertEquals(0x5a5a5a5a5a5a5a5aL, UNSAFE.getLong(pointer + 24));
                        UNSAFE.freeMemory(pointer - 8);
                    }

                    @Override
                    public int sched_setaffinity(int pid, int size, long mask) {
                        assertEquals(24, size);
                        for (int i = 0; i < size; i++) expected[i] = UNSAFE.getByte(mask + i);
                        for (int i = 0; i < 129; i++) {
                            boolean set = (expected[i / 8] & (1 << (i & 7))) != 0;
                            assertEquals(pid == 1 ? i == 128 : i >= 31 && i <= 64, set);
                        }
                        return 0;
                    }

                    @Override
                    public int sched_getaffinity(int pid, int size, long mask) {
                        UNSAFE.setMemory(mask, size, (byte) 0);
                        for (int i = 31; i <= 64; i++)
                            UNSAFE.putByte(
                                    mask + i / 8,
                                    (byte) (UNSAFE.getByte(mask + i / 8) | (1 << (i & 7))));
                        return 0;
                    }
                };
        assertEquals(0, api.sched_setaffinity_as(1, 128));
        assertEquals(0, api.sched_setaffinity_range(2, 31, 64));
        assertEquals("31-64", api.sched_getaffinity_summary(0));
        assertThrows(IllegalArgumentException.class, () -> api.sched_setaffinity_as(1, -1));
        assertThrows(IllegalArgumentException.class, () -> api.sched_setaffinity_range(1, 0, 129));
    }
}
