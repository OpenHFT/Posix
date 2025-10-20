package net.openhft.posix;

/**
 * Flags for the {@code msync(2)} system call.
 *
 * <p>The integer values mirror the glibc headers for binary compatibility.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/msync.2.html">msync(2)</a>
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
     * Returns the native integer to pass to {@code msync}.
     *
     * @return native integer constant
     */
    public int value() {
        return value;
    }
}
