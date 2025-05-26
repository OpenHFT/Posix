package net.openhft.posix;

/**
 * Clock IDs for operations like {@code clock_gettime}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 */
public enum ClockId {
    /** The system-wide real-time clock. */
    CLOCK_REALTIME(0),

    /** Monotonic clock that cannot be set. */
    CLOCK_MONOTONIC(1),

    /** CPU time consumed by the process. */
    CLOCK_PROCESS_CPUTIME_ID(2),

    /** CPU time consumed by the thread. */
    CLOCK_THREAD_CPUTIME_ID(3),

    /** Monotonic clock without NTP adjustments. */
    CLOCK_MONOTONIC_RAW(4),

    /** Real-time clock with coarse granularity. */
    CLOCK_REALTIME_COARSE(5),

    /** Monotonic clock with coarse granularity. */
    CLOCK_MONOTONIC_COARSE(6),

    /** Monotonic clock that includes suspend time. */
    CLOCK_BOOTTIME(7),

    /** Real-time clock used to set alarms. */
    CLOCK_REALTIME_ALARM(8),

    /** Boot-time clock used to set alarms. */
    CLOCK_BOOTTIME_ALARM(9),

    /** SGI cycle counter. */
    CLOCK_SGI_CYCLE(10);

    // The integer value representing the clock ID
    private final int value;

    ClockId(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
