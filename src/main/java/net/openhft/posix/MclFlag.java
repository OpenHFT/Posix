//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix;

/**
 * Flags for the {@code mlockall(2)} system call.
 *
 * <p>The integer values mirror the glibc headers for binary compatibility.</p>
 *
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
    private final int code;

    /**
     * @param code native constant value
     */
    MclFlag(int code) {
        this.code = code;
    }

    /**
     * Returns the native integer to pass to {@code mlockall}.
     *
     * @return native integer constant
     */
    public int code() {
        return code;
    }
}
