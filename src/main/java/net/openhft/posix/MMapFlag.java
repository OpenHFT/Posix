package net.openhft.posix;

/**
 * Mapping flags for {@code mmap(2)}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 * @see <a href="https://man7.org/linux/man-pages/man2/mmap.2.html">mmap(2)</a>
 */
public enum MMapFlag {
    /** Memory mapping to be shared with other processes. */
    SHARED(1),

    /** Memory mapping to be private to the process. */
    PRIVATE(2);

    /** Native constant value. */
    private int value;

    /**
     * @param value native constant value
     */
    MMapFlag(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code mmap}.
     *
     * @return integer value
     */
    public int value() {
        return value;
    }
}
