package net.openhft.posix;

/**
 * Flags for {@code mlockall(2)}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 * @see <a href="https://man7.org/linux/man-pages/man2/mlockall.2.html">mlockall(2)</a>
 */
public enum MclFlag {
    /** Lock all current pages in memory. */
    MclCurrent(1),

    /** Lock all future pages in memory. */
    MclFuture(2),

    /** Lock all current pages in memory on fault. */
    MclCurrentOnFault(1 + 4),

    /** Lock all future pages in memory on fault. */
    MclFutureOnFault(2 + 4);

    /** Native constant value. */
    private int code;

    /**
     * @param code native constant value
     */
    MclFlag(int code) {
        this.code = code;
    }

    /**
     * Constant to pass to {@code mlockall}.
     *
     * @return integer value
     */
    public int code() {
        return code;
    }
}
