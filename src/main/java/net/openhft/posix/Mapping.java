package net.openhft.posix;

public class Mapping {
    private final long addr, length, offset, inode;
    private final String perms, device, path;
    private final String toString;

    public Mapping(String line) {
        String[] parts = line.split(" +");
        String[] addrs = parts[0].split("\\-");
        addr = Long.parseUnsignedLong(addrs[0], 16);
        length = Long.parseUnsignedLong(addrs[1], 16) - addr;
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
