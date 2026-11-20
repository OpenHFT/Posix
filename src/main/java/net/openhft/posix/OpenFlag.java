/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

/**
 * Flags for the {@code open(2)} system call.
 *
 * <p>These are stable facade values, translated to each operating system's
 * native constants by the provider. They are not Linux header values.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/open.2.html">open(2)</a>
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

    /** Open with shared file lock. */
    O_SHLOCK(0x0010),

    /** Open with exclusive file lock. */
    O_EXLOCK(0x0020),

    /** Signal process group when data is ready. */
    O_ASYNC(0x0040),

    /** Synchronous writes. */
    O_FSYNC(0x0080),

    /** Create if non-existent. */
    O_CREAT(0x0200),

    /** Truncate to zero length. */
    O_TRUNC(0x0400),

    /** Error if already exists. */
    O_EXCL(0x0800);

    /** Stable facade value. */
    final int value;

    /**
     * @param value facade constant value
     */
    OpenFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the facade integer to pass to {@link PosixAPI#open(CharSequence, int, int)}.
     *
     * @return facade integer constant
     */
    public int value() {
        return value;
    }
}
