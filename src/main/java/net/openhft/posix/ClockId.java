package net.openhft.posix;

public enum ClockId {
    CLOCK_REALTIME(0),
    CLOCK_MONOTONIC(1),
    CLOCK_PROCESS_CPUTIME_ID(2),
    CLOCK_THREAD_CPUTIME_ID(3),
    CLOCK_MONOTONIC_RAW(4),
    CLOCK_REALTIME_COARSE(5),
    CLOCK_MONOTONIC_COARSE(6),
    CLOCK_BOOTTIME(7),
    CLOCK_REALTIME_ALARM(8),
    CLOCK_BOOTTIME_ALARM(9),
    CLOCK_SGI_CYCLE(10);

    private final int value;

    ClockId(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
