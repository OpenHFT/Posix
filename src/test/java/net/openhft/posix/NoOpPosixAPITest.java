/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.Test;

import static org.junit.Assert.*;

public class NoOpPosixAPITest {

    private final NoOpPosixAPI noOp = new NoOpPosixAPI("for test");

    @Test
    public void safeOperationsSucceedOrReturnNeutralValues() {
        assertEquals(0, noOp.fallocate(1, 0, 0L, 1024L));
        assertEquals(0, noOp.ftruncate(1, 0L));
        assertEquals(0, noOp.madvise(0L, 0L, 0));
        assertEquals(0, noOp.msync(0L, 0L, 0));
        assertEquals(0, noOp.sched_setaffinity(0, 0, 0L));
        // sched_getaffinity returns -1 by contract
        assertEquals(-1, noOp.sched_getaffinity(0, 0, 0L));
        // default implementations for mlock / mlock2 return false
        assertFalse(noOp.mlock(0L, 0L));
        assertFalse(noOp.mlock2(0L, 0L, true));
        // mlockall is a default no-op and must not throw
        noOp.mlockall(MclFlag.MclCurrent);
        // lastError always zero
        assertEquals(0, noOp.lastError());
        // strerror returns null
        assertNull(noOp.strerror(1));
    }

    @Test
    public void unsupportedOperationsThrowPosixRuntimeException() {
        expectMissing(() -> noOp.close(1));
        expectMissing(() -> noOp.lseek(1, 0L, 0));
        expectMissing(() -> noOp.lockf(1, 0, 0L));
        expectMissing(() -> noOp.mmap(0L, 0L, 0, 0, 0, 0L));
        expectMissing(() -> noOp.munmap(0L, 0L));
        expectMissing(() -> noOp.open("path", 0, 0));
        expectMissing(() -> noOp.read(1, 0L, 1L));
        expectMissing(() -> noOp.write(1, 0L, 1L));
        expectMissing(() -> noOp.gettimeofday(0L));
        expectMissing(() -> noOp.clock_gettime(0));
        expectMissing(() -> noOp.malloc(16L));
        expectMissing(() -> noOp.free(0L));
        expectMissing(noOp::get_nprocs);
        expectMissing(noOp::get_nprocs_conf);
        expectMissing(noOp::getpid);
        expectMissing(noOp::gettid);
    }

    private void expectMissing(Runnable op) {
        try {
            op.run();
            fail("Expected PosixRuntimeException");
        } catch (PosixRuntimeException expected) {
            assertTrue(expected.getMessage().contains("POSIX implementation missing"));
        }
    }
}
