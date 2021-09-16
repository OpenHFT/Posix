package net.openhft.posix;

public enum MSyncFlag {
    MS_SYNC(1),
    MS_INVALIDATE(2),
    MS_ASYNC(4);

    private final int value;

    MSyncFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
