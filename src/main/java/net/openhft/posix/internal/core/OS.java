package net.openhft.posix.internal.core;

import static net.openhft.posix.internal.core.Jvm.OS_ARCH;

/**
 * Utilities for querying the host OS.
 *
 * @since 2.27
 */
public final class OS {

    // The name of the operating system
    public static final String OS_NAME = System.getProperty("os.name", "?");

    // Suppresses default constructor, ensuring non-instantiability
    private OS() {
    }

    /**
     * Checks if the operating system is macOS.
     *
     * @return true if the operating system is macOS, false otherwise.
     */
    public static boolean isMacOSX() {
        return OS_NAME.equals("Mac OS X");
    }
}
