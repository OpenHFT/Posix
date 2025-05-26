package net.openhft.posix;

/**
 * Constants for the {@code lseek(2)} {@code whence} argument.
 *
 * <p>The integer values mirror the glibc headers for binary compatibility.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/lseek.2.html">lseek(2)</a>
 */
public enum WhenceFlag {
    /** Offset is set to {@code offset} bytes. */
    SEEK_SET(1),

    /** Add {@code offset} to the current location. */
    SEEK_CUR(2),

    /** Set offset relative to end of file. */
    SEEK_END(3),

    /** Move to the next data region at or after {@code offset}. */
    SEEK_DATA(4),

    /** Move to the next hole at or after {@code offset}. */
    SEEK_HOLE(5);

    // The integer value representing the whence flag
    private final int value;

    /**
     * @param value native constant value
     */
    WhenceFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer to pass to {@code lseek}.
     *
     * @return native integer constant
     */
    public int value() {
        return value;
    }
}
