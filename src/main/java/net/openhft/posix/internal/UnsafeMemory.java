package net.openhft.posix.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public enum UnsafeMemory {
    ;
    public static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
