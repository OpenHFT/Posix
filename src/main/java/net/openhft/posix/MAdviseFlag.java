package net.openhft.posix;

/**
 * Advice flags for {@code madvise(2)}.
 *
 * @see <a href="https://man7.org/linux/man-pages/man2/madvise.2.html">madvise(2)</a>
 * @since 2.27
 */
public enum MAdviseFlag {
    /** No special treatment. */
    MADV_NORMAL(0),

    /** Expect random page references. */
    MADV_RANDOM(1),

    /** Expect sequential page references. */
    MADV_SEQUENTIAL(2),

    /** Will need these pages. */
    MADV_WILLNEED(3),

    /** Do not need these pages. */
    MADV_DONTNEED(4),

    /** Free pages only under memory pressure. */
    MADV_FREE(8),

    /** Remove these pages and resources. */
    MADV_REMOVE(9),

    /** Do not inherit across fork. */
    MADV_DONTFORK(10),

    /** Do inherit across fork. */
    MADV_DOFORK(11),

    /** KSM may merge identical pages. */
    MADV_MERGEABLE(12),

    /** KSM may not merge identical pages. */
    MADV_UNMERGEABLE(13),

    /** Worth backing with hugepages. */
    MADV_HUGEPAGE(14),

    /** Not worth backing with hugepages. */
    MADV_NOHUGEPAGE(15),

    /** Exclude from core dump. */
    MADV_DONTDUMP(16),

    /** Clear the {@link #MADV_DONTDUMP} flag. */
    MADV_DODUMP(17),

    /** Zero memory on fork for the child only. */
    MADV_WIPEONFORK(18),

    /** Undo {@link #MADV_WIPEONFORK}. */
    MADV_KEEPONFORK(19);

    /** Native constant value. */
    final int value;

    /**
     * @param value native constant value
     */
    MAdviseFlag(int value) {
        this.value = value;
    }

    /**
     * Constant to pass to {@code madvise}.
     *
     * @return integer value
     * @since 2.27
     */
    public int value() {
        return value;
    }
}
