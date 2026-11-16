/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.Test;
import java.util.BitSet;
import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;

public class AffinitySummaryBoundaryTest {
    @Test
    public void masksAcrossWordBoundariesMatchIndependentRangeOracle() {
        for (int cpus : new int[]{1, 31, 32, 33, 63, 64, 65, 95, 96, 127, 128, 129}) {
            for (int pattern = 0; pattern < 4; pattern++) {
                BitSet mask = new BitSet();
                for (int cpu = 0; cpu < cpus; cpu++)
                    if (pattern == 1 || pattern == 2 && cpu % 3 != 0 || pattern == 3 && cpu == cpus - 1)
                        mask.set(cpu);
                Backend api = new Backend(cpus, mask);
                assertEquals("cpus=" + cpus + ", pattern=" + pattern, ranges(mask), api.sched_getaffinity_summary(42));
                assertEquals(1, api.frees);
            }
        }
    }

    @Test
    public void providerErrorAndExceptionBothReleaseAllocation() {
        Backend error = new Backend(64, new BitSet());
        error.result = -1;
        assertEquals("na: 22", error.sched_getaffinity_summary(42));
        assertEquals(1, error.frees);
        Backend throwing = new Backend(64, new BitSet());
        throwing.failure = new IllegalStateException("provider failed");
        assertSame(throwing.failure, assertThrows(IllegalStateException.class, () -> throwing.sched_getaffinity_summary(42)));
        assertEquals(1, throwing.frees);
    }

    private static String ranges(BitSet mask) {
        StringBuilder result = new StringBuilder();
        for (int start = mask.nextSetBit(0); start >= 0; ) {
            int end = mask.nextClearBit(start);
            if (result.length() != 0) result.append(',');
            result.append(start).append('-').append(end - 1);
            start = mask.nextSetBit(end);
        }
        return result.toString();
    }

    static final class Backend extends NoOpPosixAPI {
        final int cpus;
        final BitSet mask;
        int frees, result;
        RuntimeException failure;
        Backend(int cpus, BitSet mask) { super("test"); this.cpus = cpus; this.mask = mask; }
        @Override public int get_nprocs_conf() { return cpus; }
        @Override public long malloc(long size) {
            long ptr = UNSAFE.allocateMemory(size);
            UNSAFE.setMemory(ptr, size, (byte) 0);
            return ptr;
        }
        @Override public void free(long ptr) { frees++; UNSAFE.freeMemory(ptr); }
        @Override public int lastError() { return 22; }
        @Override public int sched_getaffinity(int pid, int size, long ptr) {
            assertEquals(42, pid);
            if (failure != null) throw failure;
            if (result != 0 || size < ((cpus + 63) / 64) * 8) return -1;
            byte[] bytes = mask.toByteArray();
            for (int i = 0; i < bytes.length; i++) UNSAFE.putByte(ptr + i, bytes[i]);
            return 0;
        }
    }
}
