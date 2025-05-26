package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.UnsafeMemory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * Facade over a small subset of POSIX needed by Chronicle libraries. The API covers
 * file descriptors, memory mapping and CPU-affinity helpers but it is not a complete
 * POSIX implementation. None of the methods are async-signal-safe and therefore must
 * not be invoked from a signal handler.
 *
 * @see <a href="../../adoc/project-requirements.adoc#posix-fn-001">POSIX-FN-001</a>
 * @since 2.27
 */
public interface PosixAPI {

    /**
     * @return The fastest available PosixAPI implementation.
     */
    static PosixAPI posix() {
        PosixAPIHolder.loadPosixApi();
        return PosixAPIHolder.POSIX_API;
    }

    /**
     * Sets the PosixAPI to a no-op implementation.
     */
    static void useNoOpPosixApi() {
        PosixAPIHolder.useNoOpPosixApi();
    }

    /**
     * Close a file descriptor.
     *
     * @param fd descriptor to close
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/close.2.html">close(2)</a>
     * @since 2.27
     */
    int close(int fd);

    /**
     * Allocates space for a file descriptor.
     *
     * @param fd     The file descriptor.
     * @param mode   The allocation mode.
     * @param offset The offset in the file.
     * @param length The length of the allocation.
     * @return 0 on success, -1 on error.
     */
    /**
     * Preallocate space for a file.
     *
     * @see <a href="https://man7.org/linux/man-pages/man2/fallocate.2.html">fallocate(2)</a>
     * @param fd     descriptor
     * @param mode   allocation mode
     * @param offset start offset
     * @param length bytes to allocate
     * @return 0 on success, -1 on error
     * @since 2.27
     */
    int fallocate(int fd, int mode, long offset, long length);

    /**
     * Truncates a file descriptor to a specified length.
     *
     * @param fd     The file descriptor.
     * @param offset The length to truncate to.
     * @return 0 on success, -1 on error.
     */
    /**
     * Truncate a file to the given length.
     *
     * @see <a href="https://man7.org/linux/man-pages/man2/ftruncate.2.html">ftruncate(2)</a>
     * @param fd     descriptor
     * @param offset new length
     * @return 0 on success, -1 on error
     * @since 2.27
     */
    int ftruncate(int fd, long offset);

    /**
     * Move the file offset.
     *
     * @param fd     descriptor
     * @param offset new offset
     * @param whence how to interpret {@code offset}
     * @return resulting file position
     * @see <a href="https://man7.org/linux/man-pages/man2/lseek.2.html">lseek(2)</a>
     * @since 2.27
     */
    default long lseek(int fd, long offset, WhenceFlag whence) {
        return lseek(fd, offset, whence.value());
    }

    /**
     * Move the file offset.
     *
     * @param fd     descriptor
     * @param offset new offset
     * @param whence see {@link WhenceFlag}
     * @return resulting file position
     * @see <a href="https://man7.org/linux/man-pages/man2/lseek.2.html">lseek(2)</a>
     * @since 2.27
     */
    long lseek(int fd, long offset, int whence);

    /**
     * Apply or test a byte-range lock.
     *
     * @param fd  descriptor
     * @param cmd command, see {@link LockfFlag}
     * @param len length in bytes
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man3/lockf.3.html">lockf(3)</a>
     * @since 2.27
     */
    int lockf(int fd, int cmd, long len);

    /**
     * Wrapper for {@link #madvise(long, long, int)} using {@link MAdviseFlag}.
     * Thread-safe and does not allocate.
     *
     * @param addr   memory address
     * @param length length in bytes
     * @param advice advice flag
     * @return 0 on success, -1 on error
     * @since 2.27
     */
    default int madvise(long addr, long length, MAdviseFlag advice) {
        return madvise(addr, length, advice.value());
    }

    /**
     * Provide paging advice to the kernel.
     *
     * @param addr   memory address
     * @param length length in bytes
     * @param advice advice bit mask
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/madvise.2.html">madvise(2)</a>
     * @since 2.27
     */
    int madvise(long addr, long length, int advice);

    /**
     * Convenience overload of {@link #mmap(long, long, int, int, int, long)}.
     * No allocation performed.
     *
     * @param addr   address hint
     * @param length length in bytes
     * @param prot   protection flags
     * @param flags  mapping flags
     * @param fd     file descriptor
     * @param offset file offset
     * @return starting address of the mapped area
     * @since 2.27
     */
    default long mmap(long addr, long length, MMapProt prot, MMapFlag flags, int fd, long offset) {
        return mmap(addr, length, prot.value(), flags.value(), fd, offset);
    }

    /**
     * Map files or devices into memory.
     *
     * @param addr   address hint
     * @param length length in bytes
     * @param prot   protection bits
     * @param flags  mapping flags
     * @param fd     file descriptor
     * @param offset file offset
     * @return starting address of the mapped area
     * @see <a href="https://man7.org/linux/man-pages/man2/mmap.2.html">mmap(2)</a>
     * @since 2.27
     */
    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    /**
     * Locks a range of the process's virtual address space into RAM.
     *
     * @param addr   The address.
     * @param length The length.
     * @return false, indicating the operation is not supported.
     */
    default boolean mlock(long addr, long length) {
        return false;
    }

    /**
     * Locks a range of the process's virtual address space into RAM.
     *
     * @param addr        The address.
     * @param length      The length.
     * @param lockOnFault Whether to lock on fault.
     * @return false, indicating the operation is not supported.
     */
    default boolean mlock2(long addr, long length, boolean lockOnFault) {
        return false;
    }

    /**
     * Locks all current and future pages into RAM.
     *
     * @param flags The flags.
     */
    default void mlockall(MclFlag flags) {
        mlockall(flags.code());
    }

    /**
     * Locks all current and future pages into RAM.
     *
     * @param flags The flags.
     */
    default void mlockall(int flags) {
    }

    /**
     * Convenience overload of {@link #msync(long, long, int)}.
     *
     * @param address start address
     * @param length  length in bytes
     * @param flags   sync flags
     * @return 0 on success, -1 on error
     * @since 2.27
     */
    default int msync(long address, long length, MSyncFlag flags) {
        return msync(address, length, flags.value());
    }

    /**
     * Flush modified pages to their backing storage.
     *
     * @param address start address
     * @param length  length in bytes
     * @param mode    flags bit mask
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/msync.2.html">msync(2)</a>
     * @since 2.27
     */
    int msync(long address, long length, int mode);

    /**
     * Unmap a region previously mapped with {@code mmap}.
     *
     * @param addr   start address
     * @param length length in bytes
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/munmap.2.html">munmap(2)</a>
     * @since 2.27
     */
    int munmap(long addr, long length);

    /**
     * Overload of {@link #open(CharSequence, int, int)} using {@link OpenFlag}.
     *
     * @param path  file to open
     * @param flags option flags
     * @param perm  permissions
     * @return file descriptor
     * @since 2.27
     */
    default int open(CharSequence path, OpenFlag flags, int perm) {
        return open(path, flags.value(), perm);
    }

    /**
     * Open a file.
     *
     * @param path  file path
     * @param flags bit mask of {@code O_*}
     * @param perm  permissions
     * @return file descriptor
     * @see <a href="https://man7.org/linux/man-pages/man2/open.2.html">open(2)</a>
     * @since 2.27
     */
    int open(CharSequence path, int flags, int perm);

    /**
     * Read bytes from a file descriptor into native memory.
     *
     * @param fd  descriptor
     * @param dst destination address
     * @param len number of bytes
     * @return bytes read
     * @see <a href="https://man7.org/linux/man-pages/man2/read.2.html">read(2)</a>
     * @since 2.27
     */
    long read(int fd, long dst, long len);

    /**
     * Write bytes from native memory to a file descriptor.
     *
     * @param fd  descriptor
     * @param src source address
     * @param len number of bytes
     * @return bytes written
     * @see <a href="https://man7.org/linux/man-pages/man2/write.2.html">write(2)</a>
     * @since 2.27
     */
    long write(int fd, long src, long len);

    /**
     * Invokes the {@code du} command to compute disk usage. Spawns a new process
     * and reads its output. Thread-safe as it performs no shared mutations.
     *
     * @param filename path to inspect
     * @return usage in bytes
     * @throws IOException if the child process fails
     * @since 2.27
     */
    default long du(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("du", filename);
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = br.readLine();
            return Long.parseUnsignedLong(line.split("\\s+")[0]);
        }
    }

    /**
     * Fill the supplied {@code timeval} structure with the current time.
     *
     * @param timeval address of a two-field structure
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/gettimeofday.2.html">gettimeofday(2)</a>
     * @since 2.27
     */
    int gettimeofday(long timeval);

    /**
     * Native wrapper for {@code sched_setaffinity(2)}.
     *
     * @param pid        process ID
     * @param cpusetsize size of mask in bytes
     * @param mask       pointer to CPU mask
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/sched_setaffinity.2.html">sched_setaffinity(2)</a>
     * @since 2.27
     */
    int sched_setaffinity(int pid, int cpusetsize, long mask);

    /**
     * Retrieve CPU affinity mask.
     *
     * @param pid        process ID
     * @param cpusetsize size of mask in bytes
     * @param mask       pointer to CPU mask
     * @return 0 on success, -1 on error
     * @see <a href="https://man7.org/linux/man-pages/man2/sched_getaffinity.2.html">sched_getaffinity(2)</a>
     * @since 2.27
     */
    int sched_getaffinity(int pid, int cpusetsize, long mask);

    /**
     * Reports the CPU affinity mask for the given process as a compressed string
     * (for example "0-3,8"). The mask is built using {@link #malloc(long)} and
     * freed with {@link #free(long)}. This method is thread-safe.
     *
     * @param pid process ID
     * @return comma separated range specification, or "na: <errno>" on failure
     * @since 2.27
     */
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

    /**
     * Retrieve the last native error number.
     *
     * @return errno value
     * @since 2.27
     */
    int lastError();

    /**
     * Pins the process to a single CPU. The method allocates a small mask via
     * {@link #malloc(long)} and releases it with {@link #free(long)}. It is
     * safe for concurrent use.
     *
     * @param pid process ID
     * @param cpu zero-based CPU index
     * @return 0 on success, -1 on error
     * @since 2.27
     */
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

    /**
     * Binds the process to a contiguous range of CPUs. Uses {@link #malloc(long)}
     * to build the mask and {@link #free(long)} to release it. The method is
     * thread-safe and may be called concurrently.
     *
     * @param pid  target process ID
     * @param from first CPU in the range
     * @param to   last CPU in the range
     * @return 0 on success, -1 on error
     * @since 2.27
     */
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
     * Helper using {@link #malloc(long)} to call {@link #gettimeofday(long)} and
     * convert the result to microseconds. Memory is released with
     * {@link #free(long)}. Safe for concurrent use.
     *
     * @return wall clock time in microseconds or {@code 0} on error
     * @since 2.27
     */
    default long gettimeofday() {
        long ptr = malloc(16);
        try {
            if (gettimeofday(ptr) != 0)
                return 0;
            if (UnsafeMemory.IS32BIT)
                return (UNSAFE.getInt(ptr) & 0xFFFFFFFFL) * 1_000_000L + UNSAFE.getInt(ptr + 4);
            return UNSAFE.getLong(ptr) * 1_000_000 + UNSAFE.getInt(ptr + 8);
        } finally {
            free(ptr);
        }
    }

    /**
     * Current wall clock time in nanoseconds using {@code CLOCK_REALTIME}.
     *
     * @return wall clock time
     * @since 2.27
     */
    default long clock_gettime() {
        return clock_gettime(0 /* CLOCK_REALTIME */);
    }

    /**
     * Return the wall clock time for a given clock.
     *
     * @param clockId the clock ID
     * @return wall clock time
     * @throws IllegalArgumentException if the clock ID is invalid
     * @since 2.27
     */
    default long clock_gettime(ClockId clockId) throws IllegalArgumentException {
        return clock_gettime(clockId.value());
    }

    /**
     * Native wrapper for {@code clock_gettime(2)}.
     *
     * @param clockId the clock ID
     * @return wall clock time
     * @throws IllegalArgumentException if the clock ID is invalid
     * @see <a href="https://man7.org/linux/man-pages/man2/clock_gettime.2.html">clock_gettime(2)</a>
     * @since 2.27
     */
    long clock_gettime(int clockId) throws IllegalArgumentException;

    /**
     * Allocate native memory.
     *
     * @param size number of bytes
     * @return address of allocated memory
     * @since 2.27
     */
    long malloc(long size);

    /**
     * Release memory previously allocated with {@link #malloc(long)}.
     *
     * @param ptr address to free
     * @since 2.27
     */
    void free(long ptr);

    /**
     * Number of available processors.
     *
     * @return processor count
     * @since 2.27
     */
    int get_nprocs();

    /**
     * Number of configured processors.
     *
     * @return processor count
     * @since 2.27
     */
    int get_nprocs_conf();

    /**
     * Process ID of the calling process.
     *
     * @return pid
     * @since 2.27
     */
    int getpid();

    /**
     * Thread ID of the caller.
     *
     * @return tid
     * @since 2.27
     */
    int gettid();

    /**
     * Convert an errno value to a message.
     *
     * @param errno error number
     * @return message string
     * @since 2.27
     */
    String strerror(int errno);

    /**
     * Human readable form of {@link #lastError()}.
     *
     * @return error message
     * @since 2.27
     */
    default String lastErrorStr() {
        return strerror(lastError());
    }
}
