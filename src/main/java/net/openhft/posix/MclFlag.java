package net.openhft.posix;

public enum MclFlag {
    MclCurrent(1),
    MclFuture(2),
    MclCurrentOnFault(1 + 4),
    MclFutureOnFault(2 + 4);

    private int code;

    MclFlag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
