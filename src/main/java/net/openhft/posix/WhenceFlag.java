package net.openhft.posix;

public enum WhenceFlag {
    /**
     * The offset is set to offset bytes.
     */
    SEEK_SET(1),
    /**
     * The offset is set to its current location plus offset bytes.
     */
    SEEK_CUR(2),
    /**
     * The offset is set to the size of the file plus offset bytes.
     */
    SEEK_END(3),
    /**
     * Adjust the file offset to the next location in the file greater than or equal to offset containing data. If offset points to data, then the file offset is set to offset.
     */
    SEEK_DATA(4),
    /**
     * Adjust the file offset to the next hole in the file greater than or equal to offset. If offset points into the middle of a hole, then the file offset is set to offset.
     * If there is no hole past offset, then the file offset is adjusted to the end of the file (i.e., there is an implicit hole at the end of any file).
     */
    SEEK_HOLE(5);

    private final int value;

    WhenceFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
