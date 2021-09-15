package net.openhft.posix.internal.jnr;

public interface Kernel32JNRInterface {
    int GetCurrentThreadId();

    void GetNativeSystemInfo(long addr);
}
