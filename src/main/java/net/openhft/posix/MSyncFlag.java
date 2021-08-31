package net.openhft.posix;

public enum MSyncFlag {
    MS_SYNC(1),
    MS_INVALIDATE(2),
    MS_ASYNC(4);

    private final int mode;

    MSyncFlag(int mode) {
        this.mode = mode;
    }

    public int mode() {
        return mode;
    }
}
