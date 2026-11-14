/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.uintptr_t;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int32_t;

/**
 * Native bindings for selected POSIX functions on Windows.
 */
public interface WinJNRPosixInterface {
    //CHECKSTYLE:OFF MethodName
    int _chsize_s(int fd, @LongLong long size);

    jnr.ffi.Pointer _errno();

    /**
     * Allocates memory of a specified size.
     *
     * @param size The number of bytes to allocate.
     * @return The address of the allocated block.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/malloc">malloc</a>
     */
    @uintptr_t long malloc(@size_t long size);

    /**
     * Releases memory previously allocated with {@link #malloc(long)}.
     *
     * @param ptr The address returned by {@link #malloc(long)}.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/free">free</a>
     */
    void free(@uintptr_t long ptr);

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
    @LongLong long _lseeki64(int fd, @LongLong long offset, int origin);

    /**
     * Reads from a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param dst Target buffer address.
     * @param len Number of bytes to read.
     * @return The number of bytes read.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/read">_read</a>
     */
    @int32_t long _read(int fd, @uintptr_t long dst, @u_int32_t long len);

    /**
     * Writes to a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param src Source buffer address.
     * @param len Number of bytes to write.
     * @return The number of bytes written.
     * @see <a href="https://learn.microsoft.com/en-us/cpp/c-runtime-library/reference/write">_write</a>
     */
    @int32_t long _write(int fd, @uintptr_t long src, @u_int32_t long len);

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
    //CHECKSTYLE:ON MethodName
}
