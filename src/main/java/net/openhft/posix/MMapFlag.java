package net.openhft.posix;

/**
 * Flags for {@code mmap}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 */
public enum MMapFlag {
    /** Memory mapping to be shared with other processes. */
    SHARED(1),

    /** Memory mapping to be private to the process. */
    PRIVATE(2);

    // The integer value representing the mmap flag
    private int value;

    /**
     * Constructor for MMapFlag.
     *
     * @param value The integer value representing the mmap flag
     */
    MMapFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MMapFlag object.
     *
     * @return The current integer value of this MMapFlag object
     */
    public int value() {
        return value;
    }
}
