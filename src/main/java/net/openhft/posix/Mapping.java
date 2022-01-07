package net.openhft.posix;

import net.openhft.posix.internal.UnsafeMemory;

public final class Mapping {
    private final long addr;
    private final long length;
    private final long offset;
    private final long inode;
    private final String perms;
    private final String device;
    private final String path;
    private final String toString;

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
