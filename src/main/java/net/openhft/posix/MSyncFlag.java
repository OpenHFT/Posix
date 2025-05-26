package net.openhft.posix;

/**
 * Flags for msync.
 *
 * @since 2.27
 */
public enum MSyncFlag {
    /**
     * Sync memory asynchronously.
     */
    MS_ASYNC(1),

    /**
     * Invalidate the caches.
     */
    MS_INVALIDATE(2),

    /**
     * Synchronous memory sync.
     */
    MS_SYNC(4);

    // The integer value representing the msync flag
    private final int value;

    MSyncFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
