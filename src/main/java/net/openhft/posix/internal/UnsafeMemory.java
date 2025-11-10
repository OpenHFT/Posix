//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Provides access to the {@link Unsafe} instance for low-level memory calls.
 * <p>
 * This utility is referenced by generated JNI layers and offers
 * architecture hints such as {@link #IS64BIT}.  It exists solely as a
 * holder for the native handle and should never be instantiated or
 * extended directly.
 * Provides access to the {@link Unsafe} instance and related memory properties.
 * When running on JDK 17 or later start the JVM with
 * {@code --add-opens java.base/jdk.internal.misc=ALL-UNNAMED} to permit reflection.
 */
public enum UnsafeMemory {
    // Empty enum to prevent instantiation
    ;

    // The Unsafe instance for performing low-level operations
    public static final Unsafe UNSAFE;

    static {
        try {
            // Access the Unsafe instance via reflection
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    // Indicates if the JVM is running in a 32-bit environment
    public static final boolean IS32BIT = UNSAFE.addressSize() == Integer.BYTES;

    // Indicates if the JVM is running in a 64-bit environment
    public static final boolean IS64BIT = UNSAFE.addressSize() == Long.BYTES;
}
