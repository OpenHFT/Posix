package net.openhft.posix.internal.core;

/**
 * Exposes static details about the host operating system.
 * Values are captured when the class loads and never updated.
 */
public final class OS {

    /**
     * The name of the operating system, captured at class initialisation and
     * immutable thereafter.
     */
    public static final String OS_NAME = System.getProperty("os.name", "?");

    /** Private constructor to prevent instantiation. */
    private OS() {
    }

    /**
     * Determines if the OS is macOS.
     *
     * @return true when running on macOS
     */
    public static boolean isMacOSX() {
        return OS_NAME.equals("Mac OS X");
    }
}
