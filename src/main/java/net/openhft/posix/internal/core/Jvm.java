package net.openhft.posix.internal.core;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * Exposes static information about the running JVM.
 * Values are captured when the class loads and never updated.
 */
public final class Jvm {

    /** Private constructor to prevent instantiation. */
    private Jvm() {
    }

    /**
     * The architecture of the operating system, captured at class
     * initialisation and immutable thereafter.
     */
    static final String OS_ARCH = System.getProperty("os.arch", "?");

    /**
     * The vendor of the Java Virtual Machine, captured at class initialisation
     * and immutable thereafter.
     */
    static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");

    /**
     * Detects if the JVM runs on an ARM CPU.
     *
     * @return true when the JVM is hosted on ARM
     */
    public static boolean isArm() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                OS_ARCH.startsWith("arm") || OS_ARCH.startsWith("aarch");
    }

    /**
     * Determines if the JVM is 64-bit.
     *
     * @return true for a 64-bit JVM
     */
    public static boolean is64bit() {
        return UnsafeMemory.IS64BIT;
    }

    /**
     * Determines if the JVM vendor is Azul Systems.
     *
     * @return true when running on Azul Systems
     */
    public static boolean isAzul() {
        return VM_VENDOR.startsWith("Azul ");
    }
}
