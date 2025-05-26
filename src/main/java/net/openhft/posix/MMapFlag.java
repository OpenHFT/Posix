package net.openhft.posix;

/**
 * Mapping flags for {@code mmap(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/mmap.2.html">mmap(2)</a>
 * @since 2.27
 */
public enum MMapFlag {
    /** Mapping visible to other processes. */
    SHARED(1),

    /** Mapping private to the process. */
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
     * @since 2.27
     */
    public int value() {
        return value;
    }
}
