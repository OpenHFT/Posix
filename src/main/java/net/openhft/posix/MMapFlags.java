package net.openhft.posix;

public enum MMapFlags {
    SHARED(1),
    PRIVATE(2);
    private int mode;

    MMapFlags(int mode) {
        this.mode = mode;
    }

    public int mode() {
        return mode;
    }
}
