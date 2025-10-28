package net.openhft.posix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class PosixRuntimeExceptionTest {

    @Test
    public void capturesMessageOnly() {
        PosixRuntimeException ex = new PosixRuntimeException("failed");
        assertEquals("failed", ex.getMessage());
        assertEquals(0, ex.errno());
    }

    @Test
    public void capturesCause() {
        IllegalStateException root = new IllegalStateException("root");
        PosixRuntimeException ex = new PosixRuntimeException(root);
        assertSame(root, ex.getCause());
        assertEquals(0, ex.errno());
    }

    @Test
    public void capturesErrnoAlongsideMessage() {
        PosixRuntimeException ex = new PosixRuntimeException("fail", 22);
        assertEquals("fail", ex.getMessage());
        assertEquals(22, ex.errno());
    }
}
