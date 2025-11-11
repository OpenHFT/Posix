/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

/**
 * Native bindings for selected POSIX functions on Windows.
 */
public interface WinJNRPosixInterface {
    // SetFilePointer
    // int ftruncate(int fd, long offset);

    /**
     * Allocates memory of a specified size.
     *
     * @param size The number of bytes to allocate.
     * @return The address of the allocated block.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/malloc">malloc</a>
     */
    long malloc(long size);

    /**
     * Releases memory previously allocated with {@link #malloc(long)}.
     *
     * @param ptr The address returned by {@link #malloc(long)}.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/free">free</a>
     */
    void free(long ptr);

    /**
     * Closes a file descriptor.
     *
     * @param fd The descriptor to close.
     * @return 0 on success, -1 on error.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/close">_close</a>
     */
    int _close(int fd);

    /**
     * Opens a file.
     *
     * @param path  The path to the file.
     * @param flags Open flags.
     * @param perm  Permission mask.
     * @return The file descriptor.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/open-wopen">_open</a>
     */
    int _open(CharSequence path, int flags, int perm);

    /**
     * Moves the file pointer.
     *
     * @param fd     The file descriptor.
     * @param offset Number of bytes to move.
     * @param origin Position from which to move.
     * @return The new file pointer.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/lseeki64">_lseeki64</a>
     */
    long _lseeki64(int fd, long offset, int origin);

    /**
     * Reads from a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param dst Target buffer address.
     * @param len Number of bytes to read.
     * @return The number of bytes read.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/read">_read</a>
     */
    long _read(int fd, long dst, long len);

    /**
     * Writes to a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param src Source buffer address.
     * @param len Number of bytes to write.
     * @return The number of bytes written.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/write">_write</a>
     */
    long _write(int fd, long src, long len);

    /**
     * Returns the identifier of the current process.
     *
     * @return The process ID.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/getpid">_getpid</a>
     */
    int _getpid();

    /**
     * Converts an error number to a message string.
     *
     * @param errno The error code.
     * @return The error message.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/strerror-wcsstrerror-mbscstrerror">strerror</a>
     */
    String strerror(int errno);
}
