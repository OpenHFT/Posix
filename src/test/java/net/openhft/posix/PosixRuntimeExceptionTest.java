/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PosixRuntimeExceptionTest {

    @Test
    public void messageOnlyConstructorSetsErrnoToZero() {
        PosixRuntimeException ex = new PosixRuntimeException("failure");
        assertEquals(0, ex.errno(), "errno should default to 0");
        assertEquals("failure", ex.getMessage(), "message should be preserved");
    }

    @Test
    public void causeConstructorKeepsCauseAndErrnoZero() {
        Throwable cause = new IllegalStateException("cause");
        PosixRuntimeException ex = new PosixRuntimeException(cause);
        assertEquals(0, ex.errno(), "errno should default to 0");
        assertEquals(cause, ex.getCause(), "cause should be preserved");
    }

    @Test
    public void messageAndErrnoConstructorStoresErrno() {
        PosixRuntimeException ex = new PosixRuntimeException("error", 123);
        assertEquals(123, ex.errno(), "errno should be stored");
        assertEquals("error", ex.getMessage(), "message should be preserved");
    }
}
