/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PosixRuntimeExceptionTest {

    @Test
    public void messageOnlyConstructorSetsErrnoToZero() {
        PosixRuntimeException ex = new PosixRuntimeException("failure");
        assertEquals(0, ex.errno());
        assertEquals("failure", ex.getMessage());
    }

    @Test
    public void causeConstructorKeepsCauseAndErrnoZero() {
        Throwable cause = new IllegalStateException("cause");
        PosixRuntimeException ex = new PosixRuntimeException(cause);
        assertEquals(0, ex.errno());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void messageAndErrnoConstructorStoresErrno() {
        PosixRuntimeException ex = new PosixRuntimeException("error", 123);
        assertEquals(123, ex.errno());
        assertEquals("error", ex.getMessage());
    }
}

