package net.openhft.posix;

public enum OpenFlags {
    O_RDONLY(0x0000),        /* open for reading only */
    O_WRONLY(0x0001),        /* open for writing only */
    O_RDWR(0x0002),        /* open for reading and writing */
    O_NONBLOCK(0x0004),        /* no delay */
    O_APPEND(0x0008),        /* set append mode */
    O_SHLOCK(0x0010),        /* open with shared file lock */
    O_EXLOCK(0x0020),        /* open with exclusive file lock */
    O_ASYNC(0x0040),        /* signal pgrp when data ready */
    O_FSYNC(0x0080),        /* synchronous writes */
    O_CREAT(0x0200),        /* create if nonexistant */
    O_TRUNC(0x0400),        /* truncate to zero length */
    O_EXCL(0x0800);        /* error if already exists */

    final int mode;

    OpenFlags(int mode) {
        this.mode = mode;
    }

    public int mode() {
        return mode;
    }
}
