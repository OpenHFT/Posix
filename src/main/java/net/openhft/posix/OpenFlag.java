package net.openhft.posix;

/**
 * Flags for {@code open(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/open.2.html">open(2)</a>
 * @since 2.27
 */
public enum OpenFlag {
    /** Open for reading only. */
    O_RDONLY(0x0000),

    /** Open for writing only. */
    O_WRONLY(0x0001),

    /** Open for reading and writing. */
    O_RDWR(0x0002),

    /** Non-blocking mode. */
    O_NONBLOCK(0x0004),

    /** Append mode. */
    O_APPEND(0x0008),

    /** Open with shared lock. */
    O_SHLOCK(0x0010),

    /** Open with exclusive lock. */
    O_EXLOCK(0x0020),

    /** Signal when data is ready. */
    O_ASYNC(0x0040),

    /** Synchronous writes. */
    O_FSYNC(0x0080),

    /** Create if missing. */
    O_CREAT(0x0200),

    /** Truncate to zero length. */
    O_TRUNC(0x0400),

    /** Error if already exists. */
    O_EXCL(0x0800);

    /** Native constant value. */
    final int value;

    /**
     * @param value native constant value
     */
    OpenFlag(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code open}.
     *
     * @return integer value
     * @since 2.27
     */
    public int value() {
        return value;
    }
}
