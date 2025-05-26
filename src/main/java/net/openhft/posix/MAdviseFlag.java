package net.openhft.posix;

/**
 * Advice flags for {@code madvise}.
 *
 * <p>The integer values are binary-compatible with the glibc headers.
 */
public enum MAdviseFlag {
    /** No further special treatment. */
    MADV_NORMAL(0),

    /** Expect random page references. */
    MADV_RANDOM(1),

    /** Expect sequential page references. */
    MADV_SEQUENTIAL(2),

    /** Will need these pages. */
    MADV_WILLNEED(3),

    /** No need for these pages. */
    MADV_DONTNEED(4),

    /** Free pages only if memory pressure. */
    MADV_FREE(8),

    /** Remove these pages and resources. */
    MADV_REMOVE(9),

    /** Do not inherit across fork. */
    MADV_DONTFORK(10),

    /** Inherit across fork. */
    MADV_DOFORK(11),

    /** KSM may merge identical pages. */
    MADV_MERGEABLE(12),

    /** KSM may not merge identical pages. */
    MADV_UNMERGEABLE(13),

    /** Hint backing with huge pages. */
    MADV_HUGEPAGE(14),

    /** Hint not worth backing with huge pages. */
    MADV_NOHUGEPAGE(15),

    /** Exclude from core dump and override the coredump filter. */
    MADV_DONTDUMP(16),

    /** Clear the MADV_DONTDUMP flag. */
    MADV_DODUMP(17),

    /** Zero memory on fork in the child. */
    MADV_WIPEONFORK(18),

    /** Undo MADV_WIPEONFORK. */
    MADV_KEEPONFORK(19);

    // The integer value representing the madvise flag
    final int value;

    MAdviseFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
