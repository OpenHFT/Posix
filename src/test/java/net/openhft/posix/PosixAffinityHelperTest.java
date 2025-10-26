package net.openhft.posix;

import net.openhft.posix.internal.UnsafeMemory;
import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PosixAffinityHelperTest {

    private static final long GUARD = 0x7A5A5A5A7A5A5A5AL;
    private static final int BYTE_BASE = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class);

    @Test
    public void schedSetAffinityRangeAllocatesSpaceForUpperBound() {
        GuardedPosixAPI posix = new GuardedPosixAPI(64);

        posix.sched_setaffinity_range(123, 64, 64);

        assertEquals("expected 2 words (16 bytes) to cover CPU 64",
                Long.BYTES * 2, posix.lastCpusetsize);
        int thirdWord = posix.wordAsInt(2);
        assertEquals("bit for CPU 64 should be set", 1, thirdWord & 1);
    }

    @Test
    public void schedSetAffinityRangeRejectsDescendingBounds() {
        GuardedPosixAPI posix = new GuardedPosixAPI(4);
        assertThrows(IllegalArgumentException.class,
                () -> posix.sched_setaffinity_range(1, 5, 4));
    }

    private static final class GuardedPosixAPI extends NoOpPosixAPI {
        private final int nprocs;
        private final Map<Long, Allocation> allocations = new HashMap<>();
        private int lastCpusetsize;
        private byte[] lastMaskBytes;

        GuardedPosixAPI(int nprocs) {
            super("guarded");
            this.nprocs = nprocs;
        }

        @Override
        public int sched_setaffinity(int pid, int cpusetsize, long mask) {
            Allocation allocation = allocations.get(mask);
            if (allocation == null)
                throw new IllegalStateException("Unknown allocation for mask pointer " + mask);
            if (cpusetsize < allocation.requestedSize)
                throw new AssertionError("cpusetsize " + cpusetsize + " < allocated " + allocation.requestedSize);
            long guard = UnsafeMemory.UNSAFE.getLong(mask + allocation.requestedSize);
            if (guard != GUARD)
                throw new AssertionError("Guard corrupted for mask pointer " + mask);
            lastCpusetsize = cpusetsize;
            byte[] snapshot = new byte[(int) allocation.requestedSize];
            UnsafeMemory.UNSAFE.copyMemory(null, mask, snapshot, BYTE_BASE, allocation.requestedSize);
            lastMaskBytes = snapshot;
            return 0;
        }

        @Override
        public long malloc(long size) {
            long actual = size + Long.BYTES;
            long ptr = UnsafeMemory.UNSAFE.allocateMemory(actual);
            UnsafeMemory.UNSAFE.setMemory(ptr, actual, (byte) 0);
            UnsafeMemory.UNSAFE.putLong(ptr + size, GUARD);
            allocations.put(ptr, new Allocation(size, actual));
            return ptr;
        }

        @Override
        public void free(long ptr) {
            Allocation allocation = allocations.remove(ptr);
            if (allocation != null)
                UnsafeMemory.UNSAFE.freeMemory(ptr);
        }

        @Override
        public int get_nprocs_conf() {
            return nprocs;
        }

        int wordAsInt(int wordIndex) {
            int offset = wordIndex * Integer.BYTES;
            if (lastMaskBytes == null || lastMaskBytes.length < offset + Integer.BYTES)
                return 0;
            return UnsafeMemory.UNSAFE.getInt(lastMaskBytes, (long) BYTE_BASE + offset);
        }
    }

    private static final class Allocation {
        final long requestedSize;
        final long actualSize;

        Allocation(long requestedSize, long actualSize) {
            this.requestedSize = requestedSize;
            this.actualSize = actualSize;
        }
    }
}
