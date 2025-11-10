//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/**
 * Portable subset of POSIX operations.
 * {@link net.openhft.posix.internal.PosixAPIHolder} selects the best provider
 * so the same calls work on Linux, macOS and Windows.
 * <p>
 * Memory example:
 * <pre>
 *     PosixAPI posix = PosixAPI.posix();
 *     long ptr = posix.malloc(128);
 *     posix.free(ptr);
 * </pre>
 *
 * File example:
 * <pre>
 *     PosixAPI posix = PosixAPI.posix();
 *     int fd = posix.open("/tmp/data", OpenFlag.O_CREAT, 0644);
 *     posix.close(fd);
 * </pre>
 *
 * All helpers strive for zero heap allocation.
 */
package net.openhft.posix;
