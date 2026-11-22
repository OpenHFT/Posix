/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.windows;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

import net.openhft.posix.PosixAPI;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/** Shared Windows x64 contracts. Addresses must reference valid caller-owned memory.
 * Unsupported calls set CRT ENOSYS; the legacy void mlockall remains a no-op. */
public class WindowsPosixAPI implements PosixAPI {
    protected static final int EINVAL = 22, EBADF = 9, ENOSYS = 40;
    protected final WindowsNative nativeApi;
    private final Map<Long, View> views = new HashMap<>();

    private static final class View {
        final long length, file;
        final boolean writableShared;

        View(long length, long file, boolean writableShared) {
            this.length = length;
            this.file = file;
            this.writableShared = writableShared;
        }
    }

    public WindowsPosixAPI(WindowsNative nativeApi) {
        if (!System.getProperty("os.name", "").startsWith("Windows") || UNSAFE.addressSize() != 8)
            throw new UnsupportedOperationException(
                    "Windows providers require a 64-bit Windows JVM");
        this.nativeApi = nativeApi;
    }

    protected final int fail(int error) {
        UNSAFE.putInt(nativeApi._errno(), error);
        return -1;
    }

    protected final int winFail() {
        switch (nativeApi.winError()) {
            case 2:
            case 3:
                return fail(2);
            case 5:
            case 32:
            case 33:
                return fail(13);
            case 6:
                return fail(EBADF);
            case 8:
            case 14:
            case 1453:
                return fail(12);
            case 87:
            case 487:
                return fail(EINVAL);
            case 112:
                return fail(28);
            case 50:
            case 120:
                return fail(ENOSYS);
            default:
                return fail(5);
        }
    }

    protected static long wideString(CharSequence value) {
        long pointer = UNSAFE.allocateMemory(((long) value.length() + 1) * 2);
        for (int i = 0; i < value.length(); i++) UNSAFE.putChar(pointer + 2L * i, value.charAt(i));
        UNSAFE.putChar(pointer + 2L * value.length(), '\0');
        return pointer;
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        // Public flags are not CRT flags.
        int supported = 3 | 8 | 0x200 | 0x400 | 0x800;
        if ((flags & 3) == 3 || (flags & ~supported) != 0) return fail(EINVAL);
        if (path == null || path.toString().indexOf('\0') >= 0) return fail(EINVAL);
        int crtFlags = (flags & 3) | (flags & 8) | 0x8000;
        if ((flags & 0x200) != 0) crtFlags |= 0x100;
        if ((flags & 0x400) != 0) crtFlags |= 0x200;
        if ((flags & 0x800) != 0) crtFlags |= 0x400;
        int crtPerm = ((perm & 0444) != 0 ? 0x100 : 0) | ((perm & 0222) != 0 ? 0x80 : 0);
        long name = wideString(path);
        try {
            return nativeApi._wopen(name, crtFlags, crtPerm);
        } finally {
            UNSAFE.freeMemory(name);
        }
    }

    @Override
    public int close(int fd) {
        return nativeApi._close(fd);
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        if (whence < 0 || whence > 2) return fail(EINVAL);
        return nativeApi._lseeki64(fd, offset, whence);
    }

    @Override
    public int ftruncate(int fd, long length) {
        if (length < 0) return fail(EINVAL);
        int error = nativeApi._chsize_s(fd, length);
        return error == 0 ? 0 : fail(error);
    }

    @Override
    public long read(int fd, long buffer, long length) {
        if (length < 0 || (buffer == 0 && length != 0)) return fail(EINVAL);
        return nativeApi._read(fd, buffer, (int) Math.min(length, Integer.MAX_VALUE));
    }

    @Override
    public long write(int fd, long buffer, long length) {
        if (length < 0 || (buffer == 0 && length != 0)) return fail(EINVAL);
        return nativeApi._write(fd, buffer, (int) Math.min(length, Integer.MAX_VALUE));
    }

    @Override
    public int lastError() {
        return UNSAFE.getInt(nativeApi._errno());
    }

    @Override
    public String strerror(int error) {
        long pointer = nativeApi.strerror(error);
        if (pointer == 0) return "Unknown error " + error;
        int length = 0;
        while (length < 1024 && UNSAFE.getByte(pointer + length) != 0) length++;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) bytes[i] = UNSAFE.getByte(pointer + i);
        return new String(bytes, Charset.defaultCharset());
    }

    @Override
    public long malloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    @Override
    public void free(long pointer) {
        UNSAFE.freeMemory(pointer);
    }

    @Override
    public int getpid() {
        return nativeApi._getpid();
    }

    @Override
    public int gettid() {
        return nativeApi.GetCurrentThreadId();
    }

    @Override
    public int get_nprocs() {
        return nativeApi.GetActiveProcessorCount(0xffff);
    }

    @Override
    public int get_nprocs_conf() {
        return nativeApi.GetMaximumProcessorCount(0xffff);
    }

    @Override
    public long clock_gettime(int clock) {
        if (clock == 1) return System.nanoTime();
        if (clock != 0 && clock != 2 && clock != 3)
            throw new IllegalArgumentException("Clock not available on Windows: " + clock);
        long times = malloc(32);
        try {
            if (clock == 0) {
                nativeApi.GetSystemTimeAsFileTime(times);
                return (UNSAFE.getLong(times) - 116444736000000000L) * 100;
            }
            int result =
                    clock == 2
                            ? nativeApi.GetProcessTimes(
                                    nativeApi.GetCurrentProcess(),
                                    times,
                                    times + 8,
                                    times + 16,
                                    times + 24)
                            : nativeApi.GetThreadTimes(
                                    nativeApi.GetCurrentThread(),
                                    times,
                                    times + 8,
                                    times + 16,
                                    times + 24);
            if (result == 0) {
                winFail();
                throw new IllegalStateException("Unable to read Windows CPU clock: " + lastError());
            }
            return (UNSAFE.getLong(times + 16) + UNSAFE.getLong(times + 24)) * 100;
        } finally {
            free(times);
        }
    }

    @Override
    public int gettimeofday(long timeval) {
        if (timeval == 0) return fail(EINVAL);
        long nanos = clock_gettime(0);
        UNSAFE.putLong(timeval, nanos / 1_000_000_000L);
        UNSAFE.putLong(timeval + 8, (nanos % 1_000_000_000L) / 1000);
        return 0;
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return fail(ENOSYS);
    }

    @Override
    public int lockf(int fd, int cmd, long length) {
        return fail(ENOSYS);
    }

    @Override
    public synchronized long mmap(
            long addr, long length, int prot, int flags, int fd, long offset) {
        if (addr != 0) return fail(ENOSYS);
        if (length <= 0 || offset < 0 || offset > Long.MAX_VALUE - length) return fail(EINVAL);
        if ((prot != 1 && prot != 3) || (flags != 1 && flags != 2)) return fail(ENOSYS);
        long file = nativeApi._get_osfhandle(fd);
        if (file == -1) return -1;
        long scratch = malloc(48), duplicate = 0, mapping = 0, view = 0;
        try {
            nativeApi.GetSystemInfo(scratch);
            long granularity = UNSAFE.getInt(scratch + 40) & 0xffffffffL;
            if (granularity == 0 || offset % granularity != 0) return fail(EINVAL);
            if (nativeApi.GetFileSizeEx(file, scratch) == 0) return winFail();
            if (offset + length > UNSAFE.getLong(scratch)) return fail(EINVAL);
            long process = nativeApi.GetCurrentProcess();
            if (nativeApi.DuplicateHandle(process, file, process, scratch, 0, 0, 2) == 0)
                return winFail();
            duplicate = UNSAFE.getLong(scratch);
            boolean writable = prot == 3;
            boolean shared = flags == 1;
            int protection = writable ? (shared ? 4 : 8) : 2;
            int access = writable ? (shared ? 6 : 1) : 4;
            mapping = nativeApi.CreateFileMappingW(duplicate, 0, protection, 0, 0, 0);
            if (mapping == 0) return winFail();
            view =
                    nativeApi.MapViewOfFile(
                            mapping, access, (int) (offset >>> 32), (int) offset, length);
            if (view == 0) return winFail();
            views.put(view, new View(length, duplicate, writable && shared));
            long result = view;
            duplicate = 0;
            view = 0;
            return result;
        } finally {
            if (view != 0) nativeApi.UnmapViewOfFile(view);
            if (mapping != 0) nativeApi.CloseHandle(mapping);
            if (duplicate != 0) nativeApi.CloseHandle(duplicate);
            free(scratch);
        }
    }

    @Override
    public synchronized int munmap(long addr, long length) {
        View view = views.get(addr);
        if (view == null || view.length != length) return fail(EINVAL);
        if (nativeApi.UnmapViewOfFile(addr) == 0) return winFail();
        views.remove(addr);
        return nativeApi.CloseHandle(view.file) != 0 ? 0 : winFail();
    }

    @Override
    public synchronized int msync(long addr, long length, int flags) {
        if (flags != 4) return fail(ENOSYS);
        if (length <= 0 || addr <= 0 || addr > Long.MAX_VALUE - length) return fail(EINVAL);
        for (Map.Entry<Long, View> entry : views.entrySet()) {
            long start = entry.getKey();
            View view = entry.getValue();
            if (addr >= start
                    && addr - start <= view.length
                    && length <= view.length - (addr - start)) {
                if (nativeApi.FlushViewOfFile(addr, length) == 0) return winFail();
                if (view.writableShared && nativeApi.FlushFileBuffers(view.file) == 0)
                    return winFail();
                return 0;
            }
        }
        return fail(EINVAL);
    }

    @Override
    public int madvise(long addr, long length, int advice) {
        if (advice != 3) return fail(ENOSYS);
        if (addr == 0 || length <= 0) return fail(EINVAL);
        long range = malloc(16);
        try {
            UNSAFE.putLong(range, addr);
            UNSAFE.putLong(range + 8, length);
            try {
                return nativeApi.PrefetchVirtualMemory(nativeApi.GetCurrentProcess(), 1, range, 0)
                                != 0
                        ? 0
                        : winFail();
            } catch (UnsatisfiedLinkError unsupported) {
                return fail(ENOSYS);
            }
        } finally {
            free(range);
        }
    }

    @Override
    public boolean mlock(long addr, long length) {
        if (addr == 0 || length <= 0) {
            fail(EINVAL);
            return false;
        }
        if (nativeApi.VirtualLock(addr, length) != 0) return true;
        winFail();
        return false;
    }

    @Override
    public boolean mlock2(long addr, long length, boolean onFault) {
        if (!onFault) return mlock(addr, length);
        fail(ENOSYS);
        return false;
    }

    @Override
    public void mlockall(int flags) {
        fail(ENOSYS);
    }

    @Override
    public int sched_setaffinity(int pid, int size, long mask) {
        return fail(ENOSYS);
    }

    @Override
    public int sched_getaffinity(int pid, int size, long mask) {
        return fail(ENOSYS);
    }

    @Override
    public long du(String filename) throws IOException {
        throw new IOException("Disk usage not available on Windows");
    }
}
