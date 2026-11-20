/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import net.openhft.posix.internal.windows.WindowsNative;

/** Java 22+ Windows x64 binding. Downcalls and per-thread error state are reused. */
public final class WindowsFFMNative implements WindowsNative {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup CRT = SymbolLookup.libraryLookup("msvcrt", Arena.global());
    private static final SymbolLookup KERNEL = SymbolLookup.libraryLookup("kernel32", Arena.global());
    private static final StructLayout ERROR_LAYOUT = Linker.Option.captureStateLayout();
    private static final long ERROR_OFFSET = ERROR_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("GetLastError"));
    private final ThreadLocal<MemorySegment> errorState = ThreadLocal.withInitial(
            () -> Arena.ofAuto().allocate(ERROR_LAYOUT));
    private static MethodHandle bind(SymbolLookup library, String name, FunctionDescriptor descriptor, boolean capture) {
        MemorySegment symbol = library.find(name).orElseThrow(() -> new UnsatisfiedLinkError(name));
        return capture ? LINKER.downcallHandle(symbol, descriptor, Linker.Option.captureCallState("GetLastError"))
                : LINKER.downcallHandle(symbol, descriptor);
    }
    private static final MethodHandle _wopen = bind(CRT, "_wopen", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _close = bind(CRT, "_close", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _read = bind(CRT, "_read", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _write = bind(CRT, "_write", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _lseeki64 = bind(CRT, "_lseeki64", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _chsize_s = bind(CRT, "_chsize_s", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG), false);
    private static final MethodHandle _errno = bind(CRT, "_errno", FunctionDescriptor.of(ValueLayout.ADDRESS), false);
    private static final MethodHandle strerror = bind(CRT, "strerror", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), false);
    private static final MethodHandle _getpid = bind(CRT, "_getpid", FunctionDescriptor.of(ValueLayout.JAVA_INT), false);
    private static final MethodHandle _get_osfhandle = bind(CRT, "_get_osfhandle", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), false);
    private static final MethodHandle GetCurrentThreadId = bind(KERNEL, "GetCurrentThreadId", FunctionDescriptor.of(ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetActiveProcessorCount = bind(KERNEL, "GetActiveProcessorCount", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetMaximumProcessorCount = bind(KERNEL, "GetMaximumProcessorCount", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetSystemTimeAsFileTime = bind(KERNEL, "GetSystemTimeAsFileTime", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS), true);
    private static final MethodHandle GetCurrentProcess = bind(KERNEL, "GetCurrentProcess", FunctionDescriptor.of(ValueLayout.ADDRESS), true);
    private static final MethodHandle GetCurrentThread = bind(KERNEL, "GetCurrentThread", FunctionDescriptor.of(ValueLayout.ADDRESS), true);
    private static final MethodHandle GetProcessTimes = bind(KERNEL, "GetProcessTimes", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle GetThreadTimes = bind(KERNEL, "GetThreadTimes", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle CreateFileMappingW = bind(KERNEL, "CreateFileMappingW", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle MapViewOfFile = bind(KERNEL, "MapViewOfFile", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG), true);
    private static final MethodHandle UnmapViewOfFile = bind(KERNEL, "UnmapViewOfFile", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle CloseHandle = bind(KERNEL, "CloseHandle", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle FlushViewOfFile = bind(KERNEL, "FlushViewOfFile", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), true);
    private static final MethodHandle FlushFileBuffers = bind(KERNEL, "FlushFileBuffers", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle DuplicateHandle = bind(KERNEL, "DuplicateHandle", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetFileSizeEx = bind(KERNEL, "GetFileSizeEx", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle GetSystemInfo = bind(KERNEL, "GetSystemInfo", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS), true);
    private static final MethodHandle VirtualLock = bind(KERNEL, "VirtualLock", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), true);
    private static final MethodHandle PrefetchVirtualMemory = bind(KERNEL, "PrefetchVirtualMemory", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), true);
    private static final MethodHandle SetFileInformationByHandle = bind(KERNEL, "SetFileInformationByHandle", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetCompressedFileSizeW = bind(KERNEL, "GetCompressedFileSizeW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle LockFileEx = bind(KERNEL, "LockFileEx", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle UnlockFileEx = bind(KERNEL, "UnlockFileEx", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), true);
    private static final MethodHandle OpenThread = bind(KERNEL, "OpenThread", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), true);
    private static final MethodHandle GetThreadGroupAffinity = bind(KERNEL, "GetThreadGroupAffinity", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle SetThreadGroupAffinity = bind(KERNEL, "SetThreadGroupAffinity", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), true);
    private static final MethodHandle GetFileInformationByHandleEx = bind(KERNEL, "GetFileInformationByHandleEx", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), true);
    @Override public int _wopen(long a0, int a1, int a2) {
        try { return (int) _wopen.invokeExact(MemorySegment.ofAddress(a0), a1, a2); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _wopen", error); }
    }
    @Override public int _close(int a0) {
        try { return (int) _close.invokeExact(a0); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _close", error); }
    }
    @Override public int _read(int a0, long a1, int a2) {
        try { return (int) _read.invokeExact(a0, MemorySegment.ofAddress(a1), a2); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _read", error); }
    }
    @Override public int _write(int a0, long a1, int a2) {
        try { return (int) _write.invokeExact(a0, MemorySegment.ofAddress(a1), a2); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _write", error); }
    }
    @Override public long _lseeki64(int a0, long a1, int a2) {
        try { return (long) _lseeki64.invokeExact(a0, a1, a2); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _lseeki64", error); }
    }
    @Override public int _chsize_s(int a0, long a1) {
        try { return (int) _chsize_s.invokeExact(a0, a1); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _chsize_s", error); }
    }
    @Override public long _errno() {
        try { return ((MemorySegment) _errno.invokeExact()).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _errno", error); }
    }
    @Override public long strerror(int a0) {
        try { return ((MemorySegment) strerror.invokeExact(a0)).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: strerror", error); }
    }
    @Override public int _getpid() {
        try { return (int) _getpid.invokeExact(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _getpid", error); }
    }
    @Override public long _get_osfhandle(int a0) {
        try { return ((MemorySegment) _get_osfhandle.invokeExact(a0)).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: _get_osfhandle", error); }
    }
    @Override public int GetCurrentThreadId() {
        try { return (int) GetCurrentThreadId.invokeExact(errorState.get()); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetCurrentThreadId", error); }
    }
    @Override public int GetActiveProcessorCount(int a0) {
        try { return (int) GetActiveProcessorCount.invokeExact(errorState.get(), a0); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetActiveProcessorCount", error); }
    }
    @Override public int GetMaximumProcessorCount(int a0) {
        try { return (int) GetMaximumProcessorCount.invokeExact(errorState.get(), a0); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetMaximumProcessorCount", error); }
    }
    @Override public void GetSystemTimeAsFileTime(long a0) {
        try { GetSystemTimeAsFileTime.invokeExact(errorState.get(), MemorySegment.ofAddress(a0)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetSystemTimeAsFileTime", error); }
    }
    @Override public long GetCurrentProcess() {
        try { return ((MemorySegment) GetCurrentProcess.invokeExact(errorState.get())).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetCurrentProcess", error); }
    }
    @Override public long GetCurrentThread() {
        try { return ((MemorySegment) GetCurrentThread.invokeExact(errorState.get())).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetCurrentThread", error); }
    }
    @Override public int GetProcessTimes(long a0, long a1, long a2, long a3, long a4) {
        try { return (int) GetProcessTimes.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1), MemorySegment.ofAddress(a2), MemorySegment.ofAddress(a3), MemorySegment.ofAddress(a4)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetProcessTimes", error); }
    }
    @Override public int GetThreadTimes(long a0, long a1, long a2, long a3, long a4) {
        try { return (int) GetThreadTimes.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1), MemorySegment.ofAddress(a2), MemorySegment.ofAddress(a3), MemorySegment.ofAddress(a4)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetThreadTimes", error); }
    }
    @Override public long CreateFileMappingW(long a0, long a1, int a2, int a3, int a4, long a5) {
        try { return ((MemorySegment) CreateFileMappingW.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1), a2, a3, a4, MemorySegment.ofAddress(a5))).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: CreateFileMappingW", error); }
    }
    @Override public long MapViewOfFile(long a0, int a1, int a2, int a3, long a4) {
        try { return ((MemorySegment) MapViewOfFile.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, a2, a3, a4)).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: MapViewOfFile", error); }
    }
    @Override public int UnmapViewOfFile(long a0) {
        try { return (int) UnmapViewOfFile.invokeExact(errorState.get(), MemorySegment.ofAddress(a0)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: UnmapViewOfFile", error); }
    }
    @Override public int CloseHandle(long a0) {
        try { return (int) CloseHandle.invokeExact(errorState.get(), MemorySegment.ofAddress(a0)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: CloseHandle", error); }
    }
    @Override public int FlushViewOfFile(long a0, long a1) {
        try { return (int) FlushViewOfFile.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: FlushViewOfFile", error); }
    }
    @Override public int FlushFileBuffers(long a0) {
        try { return (int) FlushFileBuffers.invokeExact(errorState.get(), MemorySegment.ofAddress(a0)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: FlushFileBuffers", error); }
    }
    @Override public int DuplicateHandle(long a0, long a1, long a2, long a3, int a4, int a5, int a6) {
        try { return (int) DuplicateHandle.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1), MemorySegment.ofAddress(a2), MemorySegment.ofAddress(a3), a4, a5, a6); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: DuplicateHandle", error); }
    }
    @Override public int GetFileSizeEx(long a0, long a1) {
        try { return (int) GetFileSizeEx.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetFileSizeEx", error); }
    }
    @Override public void GetSystemInfo(long a0) {
        try { GetSystemInfo.invokeExact(errorState.get(), MemorySegment.ofAddress(a0)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetSystemInfo", error); }
    }
    @Override public int VirtualLock(long a0, long a1) {
        try { return (int) VirtualLock.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: VirtualLock", error); }
    }
    @Override public int PrefetchVirtualMemory(long a0, long a1, long a2, int a3) {
        try { return (int) PrefetchVirtualMemory.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, MemorySegment.ofAddress(a2), a3); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: PrefetchVirtualMemory", error); }
    }
    @Override public int SetFileInformationByHandle(long a0, int a1, long a2, int a3) {
        try { return (int) SetFileInformationByHandle.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, MemorySegment.ofAddress(a2), a3); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: SetFileInformationByHandle", error); }
    }
    @Override public int GetCompressedFileSizeW(long a0, long a1) {
        try { return (int) GetCompressedFileSizeW.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetCompressedFileSizeW", error); }
    }
    @Override public int LockFileEx(long a0, int a1, int a2, int a3, int a4, long a5) {
        try { return (int) LockFileEx.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, a2, a3, a4, MemorySegment.ofAddress(a5)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: LockFileEx", error); }
    }
    @Override public int UnlockFileEx(long a0, int a1, int a2, int a3, long a4) {
        try { return (int) UnlockFileEx.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, a2, a3, MemorySegment.ofAddress(a4)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: UnlockFileEx", error); }
    }
    @Override public long OpenThread(int a0, int a1, int a2) {
        try { return ((MemorySegment) OpenThread.invokeExact(errorState.get(), a0, a1, a2)).address(); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: OpenThread", error); }
    }
    @Override public int GetThreadGroupAffinity(long a0, long a1) {
        try { return (int) GetThreadGroupAffinity.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetThreadGroupAffinity", error); }
    }
    @Override public int SetThreadGroupAffinity(long a0, long a1, long a2) {
        try { return (int) SetThreadGroupAffinity.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), MemorySegment.ofAddress(a1), MemorySegment.ofAddress(a2)); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: SetThreadGroupAffinity", error); }
    }
    @Override public int GetFileInformationByHandleEx(long a0, int a1, long a2, int a3) {
        try { return (int) GetFileInformationByHandleEx.invokeExact(errorState.get(), MemorySegment.ofAddress(a0), a1, MemorySegment.ofAddress(a2), a3); }
        catch (Throwable error) { throw new IllegalStateException("Native downcall failed: GetFileInformationByHandleEx", error); }
    }
    @Override public int winError() { return errorState.get().get(ValueLayout.JAVA_INT, ERROR_OFFSET); }
}

