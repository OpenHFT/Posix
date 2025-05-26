package net.openhft.posix;

/**
 * Flags for mlockall.
 *
 * @since 2.27
 */
public enum MclFlag {
    // Lock all current pages in memory
    MclCurrent(1),

    // Lock all future pages in memory
    MclFuture(2),

    // Lock all current pages in memory on fault
    MclCurrentOnFault(1 + 4),

    // Lock all future pages in memory on fault
    MclFutureOnFault(2 + 4);

    // The integer code representing the mlockall flag
    private int code;

    MclFlag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
