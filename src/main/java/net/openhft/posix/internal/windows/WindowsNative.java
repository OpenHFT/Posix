/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.windows;

/** Internal Windows x64 ABI boundary. All pointer arguments are caller-owned. */
public interface WindowsNative {
    //CHECKSTYLE:OFF MethodName
    int _wopen(long a0, int a1, int a2);
    int _close(int a0);
    int _read(int a0, long a1, int a2);
    int _write(int a0, long a1, int a2);
    long _lseeki64(int a0, long a1, int a2);
    int _chsize_s(int a0, long a1);
    long _errno();
    long strerror(int a0);
    int _getpid();
    long _get_osfhandle(int a0);
    int GetCurrentThreadId();
    int GetActiveProcessorCount(int a0);
    int GetMaximumProcessorCount(int a0);
    void GetSystemTimeAsFileTime(long a0);
    long GetCurrentProcess();
    long GetCurrentThread();
    int GetProcessTimes(long a0, long a1, long a2, long a3, long a4);
    int GetThreadTimes(long a0, long a1, long a2, long a3, long a4);
    long CreateFileMappingW(long a0, long a1, int a2, int a3, int a4, long a5);
    long MapViewOfFile(long a0, int a1, int a2, int a3, long a4);
    int UnmapViewOfFile(long a0);
    int CloseHandle(long a0);
    int FlushViewOfFile(long a0, long a1);
    int FlushFileBuffers(long a0);
    int DuplicateHandle(long a0, long a1, long a2, long a3, int a4, int a5, int a6);
    int GetFileSizeEx(long a0, long a1);
    void GetSystemInfo(long a0);
    int VirtualLock(long a0, long a1);
    int PrefetchVirtualMemory(long a0, long a1, long a2, int a3);
    int SetFileInformationByHandle(long a0, int a1, long a2, int a3);
    int GetCompressedFileSizeW(long a0, long a1);
    int LockFileEx(long a0, int a1, int a2, int a3, int a4, long a5);
    int UnlockFileEx(long a0, int a1, int a2, int a3, long a4);
    long OpenThread(int a0, int a1, int a2);
    int GetThreadGroupAffinity(long a0, long a1);
    int SetThreadGroupAffinity(long a0, long a1, long a2);
    int winError();
    int GetFileInformationByHandleEx(long handle, int type, long info, int length);
    //CHECKSTYLE:ON MethodName
}

