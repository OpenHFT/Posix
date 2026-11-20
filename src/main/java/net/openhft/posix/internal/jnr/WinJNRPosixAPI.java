/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

import jnr.ffi.Platform;
import jnr.ffi.provider.FFIProvider;

import net.openhft.posix.PosixAPI;

/**
 * Implementation of {@link PosixAPI} using JNR for Windows.
 * Provides POSIX-style file and memory calls.
 * <p>
 * Windows lacks direct equivalents for several POSIX functions, so the
 * following methods always report success and perform no action:
 * <ul>
 * <li>{@link #madvise(long, long, int)}</li>
 * <li>{@link #msync(long, long, int)}</li>
 * <li>{@link #fallocate(int, int, long, long)}</li>
 * <li>{@link #mmap(long, long, int, int, int, long)}</li>
 * <li>{@link #munmap(long, long)}</li>
 * </ul>
 */
public final class WinJNRPosixAPI implements PosixAPI {

    // JNR Runtime and Platform instances
    static final jnr.ffi.Runtime RUNTIME = FFIProvider.getSystemProvider().getRuntime();
    static final Platform NATIVE_PLATFORM = Platform.getNativePlatform();
    static final String STANDARD_C_LIBRARY_NAME = NATIVE_PLATFORM.getStandardCLibraryName();
    private static final int O_BINARY = 0x8000;
    private static final int EINVAL = 22;

    // JNR interface for Windows POSIX functions
    private final WinJNRPosixInterface jnr;

    // JNR interface for Kernel32 functions
    private final Kernel32JNRInterface kernel32;

    /**
     * Constructs a WinJNRPosixAPI instance and initializes the JNR interfaces.
     */
    public WinJNRPosixAPI() {
        jnr = LibraryUtil.load(WinJNRPosixInterface.class, STANDARD_C_LIBRARY_NAME);
        kernel32 = LibraryUtil.load(Kernel32JNRInterface.class, "Kernel32");
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        // POSIX byte streams must not translate LF/CRLF or treat Ctrl-Z as EOF.
        return jnr._open(path, flags | O_BINARY, perm);
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        return jnr._lseeki64(fd, offset, whence);
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        return -1;
    }

    @Override
    public long read(int fd, long dst, long len) {
        if (len < 0) return fail(EINVAL);
        return jnr._read(fd, dst, Math.min(len, Integer.MAX_VALUE));
    }

    @Override
    public long write(int fd, long src, long len) {
        if (len < 0) return fail(EINVAL);
        return jnr._write(fd, src, Math.min(len, Integer.MAX_VALUE));
    }

    @Override
    public int close(int fd) {
        return jnr._close(fd);
    }

    @Override
    public int getpid() {
        return jnr._getpid();
    }

    @Override
    public int gettid() {
        return kernel32.GetCurrentThreadId();
    }

    @Override
    public int madvise(long addr, long length, int advise) {
        return 0;
    }

    @Override
    public int msync(long address, long length, int flags) {
        return 0;
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return 0;
    }

    @Override
    public long malloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    @Override
    public void free(long ptr) {
        UNSAFE.freeMemory(ptr);
    }

    @Override
    public long clock_gettime() {
        return System.currentTimeMillis() * 1_000_000;
    }

    @Override
    public int get_nprocs() {
        return get_nprocs_conf();
    }

    @Override
    public int get_nprocs_conf() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public int ftruncate(int fd, long offset) {
        if (offset < 0) return fail(EINVAL);
        int error = jnr._chsize_s(fd, offset);
        return error == 0 ? 0 : fail(error);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return 0;
    }

    @Override
    public int munmap(long addr, long length) {
        return 0;
    }

    @Override
    public int gettimeofday(long timeval) {
        long now = System.currentTimeMillis();
        UNSAFE.putLong(timeval, now / 1000);
        UNSAFE.putLong(timeval + 8, (now % 1000) * 1000);
        return 0;
    }

    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int lastError() {
        // CRT errno is distinct from the Win32 GetLastError value saved by JNR.
        return jnr._errno().getInt(0);
    }

    private int fail(int error) {
        jnr._errno().putInt(0, error);
        return -1;
    }

    @Override
    public long clock_gettime(int clockId) throws IllegalArgumentException {
        return System.currentTimeMillis() * 1_000_000;
    }

    @Override
    public String strerror(int errno) {
        return jnr.strerror(errno);
    }
}
