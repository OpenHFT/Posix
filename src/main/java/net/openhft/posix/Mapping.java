package net.openhft.posix;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * Representation of one line from {@code /proc/[pid]/maps}.
 *
 *
 * Example line:
 * {@code 00400000-0040b000 r-xp 00000000 08:02 367546 /bin/cat}
 * <br>addr range | perms | offset | device | inode | path
 *
 * On a 32-bit VM the addresses are truncated.
 * Instances are thread-safe and immutable.
 * @see <a href="https://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
 */
public final class Mapping {
    // The start address of the memory mapping
    private final long addr;

    // The length of the memory mapping
    private final long length;

    // The offset into the file/VM object to which the memory mapping refers
    private final long offset;

    // The inode on the device
    private final long inode;

    // The permissions of the memory mapping (e.g., r-xp)
    private final String perms;

    // The device (major:minor)
    private final String device;

    // The file path associated with the memory mapping
    private final String path;

    // The original line from the /proc/[pid]/maps file
    private final String toString;

    /**
     * Parses a mapping line from {@code /proc/[pid]/maps}.
     *
     * @param line textual line from the maps file
     */
    public Mapping(String line) {
        String[] parts = line.split(" +");
        String[] addrs = parts[0].split("\\-");
        long addr0 = Long.parseUnsignedLong(addrs[0], 16);
        addr = UnsafeMemory.IS32BIT ? (int) addr0 : addr0;
        length = Long.parseUnsignedLong(addrs[1], 16) - addr0;
        perms = parts[1];
        offset = Long.parseUnsignedLong(parts[2], 16);
        device = parts[3];
        inode = Long.parseLong(parts[4]);
        path = parts.length >= 6 ? parts[5] : "";
        toString = line;
    }

    /** Start address of the mapping. */
    public long addr() {
        return addr;
    }

    /** Length of the mapping. */
    public long length() {
        return length;
    }

    /** Offset into the file or VM object. */
    public long offset() {
        return offset;
    }

    /** Inode number. */
    public long inode() {
        return inode;
    }

    /** Permission string such as {@code r-xp}. */
    public String perms() {
        return perms;
    }

    /** Device in {@code major:minor} form. */
    public String device() {
        return device;
    }

    /** File path of the mapping if any. */
    public String path() {
        return path;
    }

    /** Original line from the maps file. */
    @Override
    public String toString() {
        return toString;
    }
}
