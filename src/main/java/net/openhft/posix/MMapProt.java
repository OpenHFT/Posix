package net.openhft.posix;

/**
 * Protection flags for {@code mmap(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/mmap.2.html">mmap(2)</a>
 */
public enum MMapProt {
    /** Allow read access. */
    PROT_READ(1),

    /** Allow write access. */
    PROT_WRITE(2),

    /** Allow read and write access. */
    PROT_READ_WRITE(3),

    /** Allow execute access. */
    PROT_EXEC(4),

    /** Allow execute and read access. */
    PROT_EXEC_READ(5),

    /** No access. */
    PROT_NONE(8);

    /** Native constant value. */
    final int value;

    /**
     * @param value native constant value
     */
    MMapProt(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code mmap}.
     *
     * @return integer value
     */
    public int value() {
        return value;
    }
}
