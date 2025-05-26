package net.openhft.posix.internal.core;

import static net.openhft.posix.internal.core.Jvm.OS_ARCH;

/**
 * Utility class to access information about the operating system. Values are
 * captured at class initialisation and never updated.
 */
public final class OS {

    /**
     * The name of the operating system, captured at class initialisation and
     * immutable thereafter.
     */
    public static final String OS_NAME = System.getProperty("os.name", "?");

    // Suppresses default constructor, ensuring non-instantiability
    private OS() {
    }

    /**
     * Checks if the operating system is macOS. Thread-safe.
     *
     * @return true if the operating system is macOS, false otherwise.
     */
    public static boolean isMacOSX() {
        return OS_NAME.equals("Mac OS X");
    }
}
