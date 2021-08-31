package net.openhft.posix;

public enum MMapProt {
    PROT_READ(1),
    PROT_WRITE(2),
    PROT_READ_WRITE(3),
    PROT_EXEC(4),
    PROT_EXEC_READ(5),
    PROT_NONE(8);

    final int mode;

    MMapProt(int mode) {
        this.mode = mode;
    }

    public int mode() {
        return mode;
    }
}
