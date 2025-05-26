package net.openhft.posix;

/**
 * Operations for the {@code lockf(3)} library call.
 *
 * <p>The integer values mirror the glibc headers for binary compatibility.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man3/lockf.3.html">lockf(3)</a>
 */
public enum LockfFlag {
    /** Release the specified section. */
    F_ULOCK(0),

    /** Exclusive lock on the section. Blocks until available. */
    F_LOCK(1),

    /** Non-blocking exclusive lock. */
    F_TLOCK(2),

    /** Test whether a lock is held by another process. */
    F_TEST(3);

    /** Native constant value. */
    final int value;

    /**
     * @param value native constant value
     */
    LockfFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer to pass to {@code lockf}.
     *
     * @return native integer constant
     */
    public int value() {
        return value;
    }
}
