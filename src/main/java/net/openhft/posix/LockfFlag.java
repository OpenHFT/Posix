package net.openhft.posix;

public enum LockfFlag {
    /**
     * Unlock the indicated section of the file.  This may cause
     * a locked section to be split into two locked sections.
     */
    F_ULOCK(0),

    /**
     * Set an exclusive lock on the specified section of the
     * file.  If (part of) this section is already locked, the
     * call blocks until the previous lock is released.  If this
     * section overlaps an earlier locked section, both are
     * merged.  File locks are released as soon as the process
     * holding the locks closes some file descriptor for the
     * file.  A child process does not inherit these locks.
     */
    F_LOCK(1),

    /**
     * Same as F_LOCK but the call never blocks and returns an
     * error instead if the file is already locked.
     */
    F_TLOCK(2),

    /**
     * Test the lock: return 0 if the specified section is
     * unlocked or locked by this process; return -1, set errno
     * to EAGAIN (EACCES on some other systems), if another
     * process holds a lock.
     */
    F_TEST(3);

    final int value;

    LockfFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
