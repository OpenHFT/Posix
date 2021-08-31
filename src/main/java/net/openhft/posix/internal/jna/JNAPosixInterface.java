package net.openhft.posix.internal.jna;

import com.sun.jna.Pointer;

public class JNAPosixInterface {

    public native long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);
}
