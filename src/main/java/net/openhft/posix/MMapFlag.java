//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix;

/**
 * Mapping flags for the {@code mmap(2)} system call.
 *
 * <p>The integer values mirror the glibc headers for binary compatibility.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/mmap.2.html">mmap(2)</a>
 */
public enum MMapFlag {
    /** Memory mapping to be shared with other processes. */
    SHARED(1),

    /** Memory mapping to be private to the process. */
    PRIVATE(2);

    /** Native constant value. */
    private final int value;

    /**
     * @param value native constant value
     */
    MMapFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer to pass to {@code mmap}.
     *
     * @return native integer constant
     */
    public int value() {
        return value;
    }
}
