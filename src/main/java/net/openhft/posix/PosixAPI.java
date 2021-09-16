package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

public interface PosixAPI {

    /**
     * @return The fastest available PosixAPI implementation.
     */
    static PosixAPI posix() {
        return PosixAPIHolder.POSIX_API;
    }

    int close(int fd);

    int fallocate(int fd, int mode, long offset, long length);

    int ftruncate(int fd, long offset);

    default long lseek(int fd, long offset, WhenceFlag whence) {
        return lseek(fd, offset, whence.value());
    }

    long lseek(int fd, long offset, int whence);

    int lockf(int fd, int cmd, long len);

    default int madvise(long addr, long length, MAdviseFlag advice) {
        return madvise(addr, length, advice.value());
    }

    int madvise(long addr, long length, int advice);

    default long mmap(long addr, long length, MMapProt prot, MMapFlag flags, int fd, long offset) {
        return mmap(addr, length, prot.value(), flags.value(), fd, offset);
    }

    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    default int msync(long address, long length, MSyncFlag flags) {
        return msync(address, length, flags.value());
    }

    int msync(long address, long length, int mode);

    int munmap(long addr, long length);

    default int open(CharSequence path, OpenFlag flags, int perm) {
        return open(path, flags.value(), perm);
    }

    int open(CharSequence path, int flags, int perm);

    long read(int fd, long dst, long len);

    long write(int fd, long src, long len);

    default long du(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("du", filename);
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = br.readLine();
            return Long.parseUnsignedLong(line.split("\\s+")[0]);
        }
    }

    int gettimeofday(long timeval);

    int sched_setaffinity(int pid, int cpusetsize, long mask);

    int sched_getaffinity(int pid, int cpusetsize, long mask);

    default String sched_getaffinity_summary(int pid) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        boolean set = false;
        int start = 0;
        StringBuilder sb = new StringBuilder();
        try {
            final int ret = sched_getaffinity(pid, size, ptr);
            if (ret != 0)
                return "na: " + lastError();
            for (int i = 0; i < nprocs_conf; i++) {
                final int b = UNSAFE.getInt(ptr + i / 32);
                if (((b >> i) & 1) != 0) {
                    if (set) {
                        // nothing.
                    } else {
                        start = i;
                        set = true;
                    }
                } else {
                    if (set) {
                        if (sb.length() > 0)
                            sb.append(',');
                        sb.append(start).append('-').append(i - 1);
                        set = false;
                    }
                }
            }
            if (set) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(start).append('-').append(nprocs_conf - 1);
            }
            return sb.toString();
        } finally {
            free(ptr);
        }
    }

    int lastError();

    default int sched_setaffinity_as(int pid, int cpu) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        try {
            for (int i = 0; i < size; i += 4)
                UNSAFE.putInt(ptr + i, 0);

            UNSAFE.putByte(ptr + cpu / 8,
                    (byte) (1 << (cpu & 7)));
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    default int sched_setaffinity_range(int pid, int from, int to) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        try {
            for (int i = 0; i < size; i += 4)
                UNSAFE.putInt(ptr + i, 0);

            for (int i = from; i <= to; i++) {
                UNSAFE.putInt(ptr + i / 32,
                        UNSAFE.getInt(ptr + i / 32) | (1 << i));
            }
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    /**
     * note clock_gettime() is more accurate if available.
     *
     * @return wall clock in microseconds.
     */
    default long gettimeofday() {
        long ptr = malloc(16);
        try {
            if (gettimeofday(ptr) != 0)
                return 0;
            return UNSAFE.getLong(ptr) * 1_000_000 + UNSAFE.getInt(ptr + 8);
        } finally {
            free(ptr);
        }
    }

    /**
     * @return wall clock in nano-seconds.
     */
    default long clock_gettime() {
        return clock_gettime(0 /* CLOCK_REALTIME */);
    }

    default long clock_gettime(ClockId clockId) throws IllegalArgumentException {
        return clock_gettime(clockId.value());
    }

    long clock_gettime(int clockId) throws IllegalArgumentException;

    long malloc(long size);

    void free(long ptr);

    int get_nprocs();

    int get_nprocs_conf();

    int getpid();

    int gettid();

    String strerror(int errno);

    default String lastErrorStr() {
        return strerror(lastError());
    }
}
