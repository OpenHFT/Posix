package net.openhft.posix;

/**
 * Identifiers for {@code clock_gettime(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/clock_gettime.2.html">clock_gettime(2)</a>
 */
public enum ClockId {
    /** System-wide real-time clock. */
    CLOCK_REALTIME(0),

    /** Monotonic clock. */
    CLOCK_MONOTONIC(1),

    /** CPU time consumed by the process. */
    CLOCK_PROCESS_CPUTIME_ID(2),

    /** CPU time consumed by the thread. */
    CLOCK_THREAD_CPUTIME_ID(3),

    /** Monotonic clock without NTP adjustments. */
    CLOCK_MONOTONIC_RAW(4),

    /** Faster but coarse real-time clock. */
    CLOCK_REALTIME_COARSE(5),

    /** Faster but coarse monotonic clock. */
    CLOCK_MONOTONIC_COARSE(6),

    /** Monotonic clock including suspend time. */
    CLOCK_BOOTTIME(7),

    /** Real-time clock used for alarms. */
    CLOCK_REALTIME_ALARM(8),

    /** Boot-time clock used for alarms. */
    CLOCK_BOOTTIME_ALARM(9),

    /** SGI cycle counter. */
    CLOCK_SGI_CYCLE(10);

    /** Native constant value. */
    private final int value;

    /**
     * @param value native constant value
     */
    ClockId(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code clock_gettime}.
     *
     * @return integer value
     */
    public int value() {
        return value;
    }
}
