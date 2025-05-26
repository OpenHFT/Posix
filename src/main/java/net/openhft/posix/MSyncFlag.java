package net.openhft.posix;

/**
 * Flags for {@code msync(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/msync.2.html">msync(2)</a>
 * @since 2.27
 */
public enum MSyncFlag {
    /** Sync memory asynchronously. */
    MS_ASYNC(1),

    /** Invalidate caches. */
    MS_INVALIDATE(2),

    /** Synchronous memory sync. */
    MS_SYNC(4);

    /** Native constant value. */
    private final int value;

    /**
     * @param value native constant value
     */
    MSyncFlag(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code msync}.
     *
     * @return integer value
     * @since 2.27
     */
    public int value() {
        return value;
    }
}
