package net.openhft.posix;

/**
 * Operations for {@code lockf(3)}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
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
     * Constant to pass to {@code lockf}.
     *
     * @return integer value
     */
    public int value() {
        return value;
    }
}
