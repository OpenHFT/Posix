package net.openhft.posix.internal.core;

import static net.openhft.posix.internal.core.Jvm.OS_ARCH;

/**
 * Utility class to access information in the JVM.
 */
public final class OS {

    public static final String OS_NAME = System.getProperty("os.name", "?");

    // Suppresses default constructor, ensuring non-instantiability.
    private OS() {
    }

    public static boolean isMacOSX() {
        return OS_NAME.equals("Mac OS X");
    }
}
