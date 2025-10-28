package net.openhft.posix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LockfFlagTest {

    @Test
    public void valuesMatchNativeConstants() {
        assertEquals(0, LockfFlag.F_ULOCK.value());
        assertEquals(1, LockfFlag.F_LOCK.value());
        assertEquals(2, LockfFlag.F_TLOCK.value());
        assertEquals(3, LockfFlag.F_TEST.value());
    }
}
