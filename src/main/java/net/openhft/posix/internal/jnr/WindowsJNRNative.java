/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.ffi.LibraryLoader;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.uintptr_t;
import jnr.ffi.types.size_t;
import net.openhft.posix.internal.windows.WindowsNative;

/** Primitive JNR bindings; descriptors and errno belong to msvcrt. */
public final class WindowsJNRNative implements WindowsNative {
    public interface Crt {
        int _wopen(@uintptr_t long a0, int a1, int a2);
        int _close(int a0);
        int _read(int a0, @uintptr_t long a1, int a2);
        int _write(int a0, @uintptr_t long a1, int a2);
        @LongLong long _lseeki64(int a0, @LongLong long a1, int a2);
        int _chsize_s(int a0, @LongLong long a1);
        @uintptr_t long _errno();
        @uintptr_t long strerror(int a0);
        int _getpid();
        @uintptr_t long _get_osfhandle(int a0);
    }
    public interface Kernel {
        int GetCurrentThreadId();
        int GetActiveProcessorCount(int a0);
        int GetMaximumProcessorCount(int a0);
        void GetSystemTimeAsFileTime(@uintptr_t long a0);
        @uintptr_t long GetCurrentProcess();
        @uintptr_t long GetCurrentThread();
        int GetProcessTimes(@uintptr_t long a0, @uintptr_t long a1, @uintptr_t long a2, @uintptr_t long a3, @uintptr_t long a4);
        int GetThreadTimes(@uintptr_t long a0, @uintptr_t long a1, @uintptr_t long a2, @uintptr_t long a3, @uintptr_t long a4);
        @uintptr_t long CreateFileMappingW(@uintptr_t long a0, @uintptr_t long a1, int a2, int a3, int a4, @uintptr_t long a5);
        @uintptr_t long MapViewOfFile(@uintptr_t long a0, int a1, int a2, int a3, @size_t long a4);
        int UnmapViewOfFile(@uintptr_t long a0);
        int CloseHandle(@uintptr_t long a0);
        int FlushViewOfFile(@uintptr_t long a0, @size_t long a1);
        int FlushFileBuffers(@uintptr_t long a0);
        int DuplicateHandle(@uintptr_t long a0, @uintptr_t long a1, @uintptr_t long a2, @uintptr_t long a3, int a4, int a5, int a6);
        int GetFileSizeEx(@uintptr_t long a0, @uintptr_t long a1);
        void GetSystemInfo(@uintptr_t long a0);
        int VirtualLock(@uintptr_t long a0, @size_t long a1);
        int PrefetchVirtualMemory(@uintptr_t long a0, @size_t long a1, @uintptr_t long a2, int a3);
        int SetFileInformationByHandle(@uintptr_t long a0, int a1, @uintptr_t long a2, int a3);
        int GetCompressedFileSizeW(@uintptr_t long a0, @uintptr_t long a1);
        int LockFileEx(@uintptr_t long a0, int a1, int a2, int a3, int a4, @uintptr_t long a5);
        int UnlockFileEx(@uintptr_t long a0, int a1, int a2, int a3, @uintptr_t long a4);
        @uintptr_t long OpenThread(int a0, int a1, int a2);
        int GetThreadGroupAffinity(@uintptr_t long a0, @uintptr_t long a1);
        int SetThreadGroupAffinity(@uintptr_t long a0, @uintptr_t long a1, @uintptr_t long a2);
    }
    private final Crt c = LibraryLoader.create(Crt.class).load("msvcrt");
    private final Kernel k = LibraryLoader.create(Kernel.class).load("kernel32");
    @Override public int _wopen(long a0, int a1, int a2) { return c._wopen(a0, a1, a2); }
    @Override public int _close(int a0) { return c._close(a0); }
    @Override public int _read(int a0, long a1, int a2) { return c._read(a0, a1, a2); }
    @Override public int _write(int a0, long a1, int a2) { return c._write(a0, a1, a2); }
    @Override public long _lseeki64(int a0, long a1, int a2) { return c._lseeki64(a0, a1, a2); }
    @Override public int _chsize_s(int a0, long a1) { return c._chsize_s(a0, a1); }
    @Override public long _errno() { return c._errno(); }
    @Override public long strerror(int a0) { return c.strerror(a0); }
    @Override public int _getpid() { return c._getpid(); }
    @Override public long _get_osfhandle(int a0) { return c._get_osfhandle(a0); }
    @Override public int GetCurrentThreadId() { return k.GetCurrentThreadId(); }
    @Override public int GetActiveProcessorCount(int a0) { return k.GetActiveProcessorCount(a0); }
    @Override public int GetMaximumProcessorCount(int a0) { return k.GetMaximumProcessorCount(a0); }
    @Override public void GetSystemTimeAsFileTime(long a0) { k.GetSystemTimeAsFileTime(a0); }
    @Override public long GetCurrentProcess() { return k.GetCurrentProcess(); }
    @Override public long GetCurrentThread() { return k.GetCurrentThread(); }
    @Override public int GetProcessTimes(long a0, long a1, long a2, long a3, long a4) { return k.GetProcessTimes(a0, a1, a2, a3, a4); }
    @Override public int GetThreadTimes(long a0, long a1, long a2, long a3, long a4) { return k.GetThreadTimes(a0, a1, a2, a3, a4); }
    @Override public long CreateFileMappingW(long a0, long a1, int a2, int a3, int a4, long a5) { return k.CreateFileMappingW(a0, a1, a2, a3, a4, a5); }
    @Override public long MapViewOfFile(long a0, int a1, int a2, int a3, long a4) { return k.MapViewOfFile(a0, a1, a2, a3, a4); }
    @Override public int UnmapViewOfFile(long a0) { return k.UnmapViewOfFile(a0); }
    @Override public int CloseHandle(long a0) { return k.CloseHandle(a0); }
    @Override public int FlushViewOfFile(long a0, long a1) { return k.FlushViewOfFile(a0, a1); }
    @Override public int FlushFileBuffers(long a0) { return k.FlushFileBuffers(a0); }
    @Override public int DuplicateHandle(long a0, long a1, long a2, long a3, int a4, int a5, int a6) { return k.DuplicateHandle(a0, a1, a2, a3, a4, a5, a6); }
    @Override public int GetFileSizeEx(long a0, long a1) { return k.GetFileSizeEx(a0, a1); }
    @Override public void GetSystemInfo(long a0) { k.GetSystemInfo(a0); }
    @Override public int VirtualLock(long a0, long a1) { return k.VirtualLock(a0, a1); }
    @Override public int PrefetchVirtualMemory(long a0, long a1, long a2, int a3) { return k.PrefetchVirtualMemory(a0, a1, a2, a3); }
    @Override public int SetFileInformationByHandle(long a0, int a1, long a2, int a3) { return k.SetFileInformationByHandle(a0, a1, a2, a3); }
    @Override public int GetCompressedFileSizeW(long a0, long a1) { return k.GetCompressedFileSizeW(a0, a1); }
    @Override public int LockFileEx(long a0, int a1, int a2, int a3, int a4, long a5) { return k.LockFileEx(a0, a1, a2, a3, a4, a5); }
    @Override public int UnlockFileEx(long a0, int a1, int a2, int a3, long a4) { return k.UnlockFileEx(a0, a1, a2, a3, a4); }
    @Override public long OpenThread(int a0, int a1, int a2) { return k.OpenThread(a0, a1, a2); }
    @Override public int GetThreadGroupAffinity(long a0, long a1) { return k.GetThreadGroupAffinity(a0, a1); }
    @Override public int SetThreadGroupAffinity(long a0, long a1, long a2) { return k.SetThreadGroupAffinity(a0, a1, a2); }
    @Override public int winError() { return jnr.ffi.Runtime.getRuntime(k).getLastError(); }
}

