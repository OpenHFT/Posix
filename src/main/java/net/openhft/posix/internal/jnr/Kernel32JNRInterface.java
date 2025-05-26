package net.openhft.posix.internal.jnr;

/**
 * Native bindings to functions from Kernel32.dll.
 */
public interface Kernel32JNRInterface {

    /**
     * Retrieves the identifier of the calling thread.
     *
     * @return The thread identifier.
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getcurrentthreadid">GetCurrentThreadId</a>
     */
    int GetCurrentThreadId();

    /**
     * Retrieves information about the current system.
     *
     * @param addr Address of a SYSTEM_INFO structure.
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/sysinfoapi/nf-sysinfoapi-getnativesysteminfo">GetNativeSystemInfo</a>
     */
    void GetNativeSystemInfo(long addr);
}
