package net.openhft.posix;

public enum MAdviseFlag {
    MADV_NORMAL(0),        /* No further special treatment.  */
    MADV_RANDOM(1),        /* Expect random page references.  */
    MADV_SEQUENTIAL(2),        /* Expect sequential page references.  */
    MADV_WILLNEED(3),        /* Will need these pages.  */
    MADV_DONTNEED(4),        /* Don't need these pages.  */
    MADV_FREE(8),        /* Free pages only if memory pressure.  */
    MADV_REMOVE(9),        /* Remove these pages and resources.  */
    MADV_DONTFORK(10),        /* Do not inherit across fork.  */
    MADV_DOFORK(11),        /* Do inherit across fork.  */
    MADV_MERGEABLE(12),        /* KSM may merge identical pages.  */
    MADV_UNMERGEABLE(13),        /* KSM may not merge identical pages.  */
    MADV_HUGEPAGE(14),        /* Worth backing with hugepages.  */
    MADV_NOHUGEPAGE(15),        /* Not worth backing with hugepages.  */
    MADV_DONTDUMP(16),    /* Explicity exclude from the core dump, overrides the coredump filter bits.  */
    MADV_DODUMP(17),        /* Clear the MADV_DONTDUMP flag.  */
    MADV_WIPEONFORK(18),        /* Zero memory on fork, child only.  */
    MADV_KEEPONFORK(19);        /* Undo MADV_WIPEONFORK.  */
    final int mode;

    MAdviseFlag(int mode) {
        this.mode = mode;
    }

    public int mode() {
        return mode;
    }
}
