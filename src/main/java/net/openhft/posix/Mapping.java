package net.openhft.posix;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * Immutable value object for one line of {@code /proc/$pid/maps}.
 *
 * Example line:
 * {@code 00400000-0040b000 r-xp 00000000 08:02 367546 /bin/cat}
 * <br>addr range | perms | offset | device | inode | path
 *
 * On a 32-bit VM the addresses are truncated.
 * Instances are thread-safe and immutable.
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
     * Constructs a Mapping object by parsing a line from the /proc/[pid]/maps file.
     *
     * @param line A line from the /proc/[pid]/maps file.
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

    public long addr() {
        return addr;
    }

    public long length() {
        return length;
    }

    public long offset() {
        return offset;
    }

    public long inode() {
        return inode;
    }

    public String perms() {
        return perms;
    }

    public String device() {
        return device;
    }

    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return toString;
    }
}
