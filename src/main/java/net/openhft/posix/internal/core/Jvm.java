package net.openhft.posix.internal.core;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * Utility class to access information in the JVM.
 */
public final class Jvm {

    // Suppresses default constructor, ensuring non-instantiability.
    private Jvm() {
    }
    static final String OS_ARCH = System.getProperty("os.arch", "?");
    static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");

    public static boolean isArm() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                OS_ARCH.startsWith("arm") || OS_ARCH.startsWith("aarch");
    }

    public static boolean is64bit() {
        return UnsafeMemory.IS64BIT;
    }

    public static boolean isAzul() { 
        return VM_VENDOR.startsWith("Azul ");
    }
}
