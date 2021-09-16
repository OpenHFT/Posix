package net.openhft.posix;

public enum MMapFlag {
    SHARED(1),
    PRIVATE(2);
    private int value;

    MMapFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
