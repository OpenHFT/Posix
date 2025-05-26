package net.openhft.posix;

/**
 * Protection flags for {@code mmap}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 */
public enum MMapProt {
    /** Allow read access. */
    PROT_READ(1),

    /** Allow write access. */
    PROT_WRITE(2),

    /** Allow both read and write access. */
    PROT_READ_WRITE(3),

    /** Allow execute access. */
    PROT_EXEC(4),

    /** Allow execute and read access. */
    PROT_EXEC_READ(5),

    /** No access allowed. */
    PROT_NONE(8);

    // The integer value representing the memory protection level
    final int value;

    /**
     * Constructor for MMapProt.
     *
     * @param value The integer value representing the memory protection level
     */
    MMapProt(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MMapProt object.
     *
     * @return The current integer value of this MMapProt object
     */
    public int value() {
        return value;
    }
}
