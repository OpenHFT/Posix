/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import net.openhft.posix.internal.windows.WindowsNative;

/** Direct JNA mappings for comparison with interface dispatch. Windows x64 only. */
public final class WindowsJNADirectNative implements WindowsNative {
    public static class Crt {
        static {
            Native.register(Crt.class, NativeLibrary.getInstance("msvcrt"));
        }

        public static native int _wopen(Pointer a0, int a1, int a2);

        public static native int _close(int a0);

        public static native int _read(int a0, Pointer a1, int a2);

        public static native int _write(int a0, Pointer a1, int a2);

        public static native long _lseeki64(int a0, long a1, int a2);

        public static native int _chsize_s(int a0, long a1);

        public static native Pointer _errno();

        public static native Pointer strerror(int a0);

        public static native int _getpid();

        public static native Pointer _get_osfhandle(int a0);
    }

    public static class Kernel {
        static {
            Native.register(Kernel.class, NativeLibrary.getInstance("kernel32"));
        }

        public static native int GetCurrentThreadId();

        public static native int GetActiveProcessorCount(int a0);

        public static native int GetMaximumProcessorCount(int a0);

        public static native void GetSystemTimeAsFileTime(Pointer a0);

        public static native Pointer GetCurrentProcess();

        public static native Pointer GetCurrentThread();

        public static native int GetProcessTimes(
                Pointer a0, Pointer a1, Pointer a2, Pointer a3, Pointer a4);

        public static native int GetThreadTimes(
                Pointer a0, Pointer a1, Pointer a2, Pointer a3, Pointer a4);

        public static native Pointer CreateFileMappingW(
                Pointer a0, Pointer a1, int a2, int a3, int a4, Pointer a5);

        public static native Pointer MapViewOfFile(Pointer a0, int a1, int a2, int a3, long a4);

        public static native int UnmapViewOfFile(Pointer a0);

        public static native int CloseHandle(Pointer a0);

        public static native int FlushViewOfFile(Pointer a0, long a1);

        public static native int FlushFileBuffers(Pointer a0);

        public static native int DuplicateHandle(
                Pointer a0, Pointer a1, Pointer a2, Pointer a3, int a4, int a5, int a6);

        public static native int GetFileSizeEx(Pointer a0, Pointer a1);

        public static native void GetSystemInfo(Pointer a0);

        public static native int VirtualLock(Pointer a0, long a1);

        public static native int PrefetchVirtualMemory(Pointer a0, long a1, Pointer a2, int a3);

        public static native int SetFileInformationByHandle(Pointer a0, int a1, Pointer a2, int a3);

        public static native int GetCompressedFileSizeW(Pointer a0, Pointer a1);

        public static native int LockFileEx(Pointer a0, int a1, int a2, int a3, int a4, Pointer a5);

        public static native int UnlockFileEx(Pointer a0, int a1, int a2, int a3, Pointer a4);

        public static native Pointer OpenThread(int a0, int a1, int a2);

        public static native int GetThreadGroupAffinity(Pointer a0, Pointer a1);

        public static native int SetThreadGroupAffinity(Pointer a0, Pointer a1, Pointer a2);

        public static native int GetFileInformationByHandleEx(
                Pointer a0, int a1, Pointer a2, int a3);
    }

    @Override
    public int _wopen(long a0, int a1, int a2) {
        return Crt._wopen(new Pointer(a0), a1, a2);
    }

    @Override
    public int _close(int a0) {
        return Crt._close(a0);
    }

    @Override
    public int _read(int a0, long a1, int a2) {
        return Crt._read(a0, new Pointer(a1), a2);
    }

    @Override
    public int _write(int a0, long a1, int a2) {
        return Crt._write(a0, new Pointer(a1), a2);
    }

    @Override
    public long _lseeki64(int a0, long a1, int a2) {
        return Crt._lseeki64(a0, a1, a2);
    }

    @Override
    public int _chsize_s(int a0, long a1) {
        return Crt._chsize_s(a0, a1);
    }

    @Override
    public long _errno() {
        return Pointer.nativeValue(Crt._errno());
    }

    @Override
    public long strerror(int a0) {
        return Pointer.nativeValue(Crt.strerror(a0));
    }

    @Override
    public int _getpid() {
        return Crt._getpid();
    }

    @Override
    public long _get_osfhandle(int a0) {
        return Pointer.nativeValue(Crt._get_osfhandle(a0));
    }

    @Override
    public int GetCurrentThreadId() {
        return Kernel.GetCurrentThreadId();
    }

    @Override
    public int GetActiveProcessorCount(int a0) {
        return Kernel.GetActiveProcessorCount(a0);
    }

    @Override
    public int GetMaximumProcessorCount(int a0) {
        return Kernel.GetMaximumProcessorCount(a0);
    }

    @Override
    public void GetSystemTimeAsFileTime(long a0) {
        Kernel.GetSystemTimeAsFileTime(new Pointer(a0));
    }

    @Override
    public long GetCurrentProcess() {
        return Pointer.nativeValue(Kernel.GetCurrentProcess());
    }

    @Override
    public long GetCurrentThread() {
        return Pointer.nativeValue(Kernel.GetCurrentThread());
    }

    @Override
    public int GetProcessTimes(long a0, long a1, long a2, long a3, long a4) {
        return Kernel.GetProcessTimes(
                new Pointer(a0),
                new Pointer(a1),
                new Pointer(a2),
                new Pointer(a3),
                new Pointer(a4));
    }

    @Override
    public int GetThreadTimes(long a0, long a1, long a2, long a3, long a4) {
        return Kernel.GetThreadTimes(
                new Pointer(a0),
                new Pointer(a1),
                new Pointer(a2),
                new Pointer(a3),
                new Pointer(a4));
    }

    @Override
    public long CreateFileMappingW(long a0, long a1, int a2, int a3, int a4, long a5) {
        return Pointer.nativeValue(
                Kernel.CreateFileMappingW(
                        new Pointer(a0), new Pointer(a1), a2, a3, a4, new Pointer(a5)));
    }

    @Override
    public long MapViewOfFile(long a0, int a1, int a2, int a3, long a4) {
        return Pointer.nativeValue(Kernel.MapViewOfFile(new Pointer(a0), a1, a2, a3, a4));
    }

    @Override
    public int UnmapViewOfFile(long a0) {
        return Kernel.UnmapViewOfFile(new Pointer(a0));
    }

    @Override
    public int CloseHandle(long a0) {
        return Kernel.CloseHandle(new Pointer(a0));
    }

    @Override
    public int FlushViewOfFile(long a0, long a1) {
        return Kernel.FlushViewOfFile(new Pointer(a0), a1);
    }

    @Override
    public int FlushFileBuffers(long a0) {
        return Kernel.FlushFileBuffers(new Pointer(a0));
    }

    @Override
    public int DuplicateHandle(long a0, long a1, long a2, long a3, int a4, int a5, int a6) {
        return Kernel.DuplicateHandle(
                new Pointer(a0), new Pointer(a1), new Pointer(a2), new Pointer(a3), a4, a5, a6);
    }

    @Override
    public int GetFileSizeEx(long a0, long a1) {
        return Kernel.GetFileSizeEx(new Pointer(a0), new Pointer(a1));
    }

    @Override
    public void GetSystemInfo(long a0) {
        Kernel.GetSystemInfo(new Pointer(a0));
    }

    @Override
    public int VirtualLock(long a0, long a1) {
        return Kernel.VirtualLock(new Pointer(a0), a1);
    }

    @Override
    public int PrefetchVirtualMemory(long a0, long a1, long a2, int a3) {
        return Kernel.PrefetchVirtualMemory(new Pointer(a0), a1, new Pointer(a2), a3);
    }

    @Override
    public int SetFileInformationByHandle(long a0, int a1, long a2, int a3) {
        return Kernel.SetFileInformationByHandle(new Pointer(a0), a1, new Pointer(a2), a3);
    }

    @Override
    public int GetCompressedFileSizeW(long a0, long a1) {
        return Kernel.GetCompressedFileSizeW(new Pointer(a0), new Pointer(a1));
    }

    @Override
    public int LockFileEx(long a0, int a1, int a2, int a3, int a4, long a5) {
        return Kernel.LockFileEx(new Pointer(a0), a1, a2, a3, a4, new Pointer(a5));
    }

    @Override
    public int UnlockFileEx(long a0, int a1, int a2, int a3, long a4) {
        return Kernel.UnlockFileEx(new Pointer(a0), a1, a2, a3, new Pointer(a4));
    }

    @Override
    public long OpenThread(int a0, int a1, int a2) {
        return Pointer.nativeValue(Kernel.OpenThread(a0, a1, a2));
    }

    @Override
    public int GetThreadGroupAffinity(long a0, long a1) {
        return Kernel.GetThreadGroupAffinity(new Pointer(a0), new Pointer(a1));
    }

    @Override
    public int SetThreadGroupAffinity(long a0, long a1, long a2) {
        return Kernel.SetThreadGroupAffinity(new Pointer(a0), new Pointer(a1), new Pointer(a2));
    }

    @Override
    public int GetFileInformationByHandleEx(long a0, int a1, long a2, int a3) {
        return Kernel.GetFileInformationByHandleEx(new Pointer(a0), a1, new Pointer(a2), a3);
    }

    @Override
    public int winError() {
        return Native.getLastError();
    }
}
