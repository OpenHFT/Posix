/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import net.openhft.posix.internal.windows.WindowsNative;

/** Windows x64 JNA binding. Pointer wrappers may allocate. */
public final class WindowsJNANative implements WindowsNative {
    public interface Crt extends Library {
        int _wopen(Pointer a0, int a1, int a2);
        int _close(int a0);
        int _read(int a0, Pointer a1, int a2);
        int _write(int a0, Pointer a1, int a2);
        long _lseeki64(int a0, long a1, int a2);
        int _chsize_s(int a0, long a1);
        Pointer _errno();
        Pointer strerror(int a0);
        int _getpid();
        Pointer _get_osfhandle(int a0);
    }
    public interface Kernel extends Library {
        int GetCurrentThreadId();
        int GetActiveProcessorCount(int a0);
        int GetMaximumProcessorCount(int a0);
        void GetSystemTimeAsFileTime(Pointer a0);
        Pointer GetCurrentProcess();
        Pointer GetCurrentThread();
        int GetProcessTimes(Pointer a0, Pointer a1, Pointer a2, Pointer a3, Pointer a4);
        int GetThreadTimes(Pointer a0, Pointer a1, Pointer a2, Pointer a3, Pointer a4);
        Pointer CreateFileMappingW(Pointer a0, Pointer a1, int a2, int a3, int a4, Pointer a5);
        Pointer MapViewOfFile(Pointer a0, int a1, int a2, int a3, long a4);
        int UnmapViewOfFile(Pointer a0);
        int CloseHandle(Pointer a0);
        int FlushViewOfFile(Pointer a0, long a1);
        int FlushFileBuffers(Pointer a0);
        int DuplicateHandle(Pointer a0, Pointer a1, Pointer a2, Pointer a3, int a4, int a5, int a6);
        int GetFileSizeEx(Pointer a0, Pointer a1);
        void GetSystemInfo(Pointer a0);
        int VirtualLock(Pointer a0, long a1);
        int PrefetchVirtualMemory(Pointer a0, long a1, Pointer a2, int a3);
        int SetFileInformationByHandle(Pointer a0, int a1, Pointer a2, int a3);
        int GetCompressedFileSizeW(Pointer a0, Pointer a1);
        int LockFileEx(Pointer a0, int a1, int a2, int a3, int a4, Pointer a5);
        int UnlockFileEx(Pointer a0, int a1, int a2, int a3, Pointer a4);
        Pointer OpenThread(int a0, int a1, int a2);
        int GetThreadGroupAffinity(Pointer a0, Pointer a1);
        int SetThreadGroupAffinity(Pointer a0, Pointer a1, Pointer a2);
    }
    private final Crt c = Native.load("msvcrt", Crt.class);
    private final Kernel k = Native.load("kernel32", Kernel.class);
    @Override public int _wopen(long a0, int a1, int a2) { return c._wopen(new Pointer(a0), a1, a2); }
    @Override public int _close(int a0) { return c._close(a0); }
    @Override public int _read(int a0, long a1, int a2) { return c._read(a0, new Pointer(a1), a2); }
    @Override public int _write(int a0, long a1, int a2) { return c._write(a0, new Pointer(a1), a2); }
    @Override public long _lseeki64(int a0, long a1, int a2) { return c._lseeki64(a0, a1, a2); }
    @Override public int _chsize_s(int a0, long a1) { return c._chsize_s(a0, a1); }
    @Override public long _errno() { return Pointer.nativeValue(c._errno()); }
    @Override public long strerror(int a0) { return Pointer.nativeValue(c.strerror(a0)); }
    @Override public int _getpid() { return c._getpid(); }
    @Override public long _get_osfhandle(int a0) { return Pointer.nativeValue(c._get_osfhandle(a0)); }
    @Override public int GetCurrentThreadId() { return k.GetCurrentThreadId(); }
    @Override public int GetActiveProcessorCount(int a0) { return k.GetActiveProcessorCount(a0); }
    @Override public int GetMaximumProcessorCount(int a0) { return k.GetMaximumProcessorCount(a0); }
    @Override public void GetSystemTimeAsFileTime(long a0) { k.GetSystemTimeAsFileTime(new Pointer(a0)); }
    @Override public long GetCurrentProcess() { return Pointer.nativeValue(k.GetCurrentProcess()); }
    @Override public long GetCurrentThread() { return Pointer.nativeValue(k.GetCurrentThread()); }
    @Override public int GetProcessTimes(long a0, long a1, long a2, long a3, long a4) { return k.GetProcessTimes(new Pointer(a0), new Pointer(a1), new Pointer(a2), new Pointer(a3), new Pointer(a4)); }
    @Override public int GetThreadTimes(long a0, long a1, long a2, long a3, long a4) { return k.GetThreadTimes(new Pointer(a0), new Pointer(a1), new Pointer(a2), new Pointer(a3), new Pointer(a4)); }
    @Override public long CreateFileMappingW(long a0, long a1, int a2, int a3, int a4, long a5) { return Pointer.nativeValue(k.CreateFileMappingW(new Pointer(a0), new Pointer(a1), a2, a3, a4, new Pointer(a5))); }
    @Override public long MapViewOfFile(long a0, int a1, int a2, int a3, long a4) { return Pointer.nativeValue(k.MapViewOfFile(new Pointer(a0), a1, a2, a3, a4)); }
    @Override public int UnmapViewOfFile(long a0) { return k.UnmapViewOfFile(new Pointer(a0)); }
    @Override public int CloseHandle(long a0) { return k.CloseHandle(new Pointer(a0)); }
    @Override public int FlushViewOfFile(long a0, long a1) { return k.FlushViewOfFile(new Pointer(a0), a1); }
    @Override public int FlushFileBuffers(long a0) { return k.FlushFileBuffers(new Pointer(a0)); }
    @Override public int DuplicateHandle(long a0, long a1, long a2, long a3, int a4, int a5, int a6) { return k.DuplicateHandle(new Pointer(a0), new Pointer(a1), new Pointer(a2), new Pointer(a3), a4, a5, a6); }
    @Override public int GetFileSizeEx(long a0, long a1) { return k.GetFileSizeEx(new Pointer(a0), new Pointer(a1)); }
    @Override public void GetSystemInfo(long a0) { k.GetSystemInfo(new Pointer(a0)); }
    @Override public int VirtualLock(long a0, long a1) { return k.VirtualLock(new Pointer(a0), a1); }
    @Override public int PrefetchVirtualMemory(long a0, long a1, long a2, int a3) { return k.PrefetchVirtualMemory(new Pointer(a0), a1, new Pointer(a2), a3); }
    @Override public int SetFileInformationByHandle(long a0, int a1, long a2, int a3) { return k.SetFileInformationByHandle(new Pointer(a0), a1, new Pointer(a2), a3); }
    @Override public int GetCompressedFileSizeW(long a0, long a1) { return k.GetCompressedFileSizeW(new Pointer(a0), new Pointer(a1)); }
    @Override public int LockFileEx(long a0, int a1, int a2, int a3, int a4, long a5) { return k.LockFileEx(new Pointer(a0), a1, a2, a3, a4, new Pointer(a5)); }
    @Override public int UnlockFileEx(long a0, int a1, int a2, int a3, long a4) { return k.UnlockFileEx(new Pointer(a0), a1, a2, a3, new Pointer(a4)); }
    @Override public long OpenThread(int a0, int a1, int a2) { return Pointer.nativeValue(k.OpenThread(a0, a1, a2)); }
    @Override public int GetThreadGroupAffinity(long a0, long a1) { return k.GetThreadGroupAffinity(new Pointer(a0), new Pointer(a1)); }
    @Override public int SetThreadGroupAffinity(long a0, long a1, long a2) { return k.SetThreadGroupAffinity(new Pointer(a0), new Pointer(a1), new Pointer(a2)); }
    @Override public int winError() { return Native.getLastError(); }
}

