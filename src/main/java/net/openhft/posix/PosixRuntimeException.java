//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.posix;

/**
 * Runtime exception for POSIX operations. The instance wraps the errno
 * produced by the underlying native call.
 */
public class PosixRuntimeException extends RuntimeException {
    /** Used to maintain serialization compatibility. */
    private static final long serialVersionUID = 0L;

    /** POSIX errno captured from the failing call. */
    private final int errno;

    /**
     * Constructs a new PosixRuntimeException with the specified detail message.
     *
     * @param message The detail message for the exception.
     */
    public PosixRuntimeException(String message) {
        this(message, 0);
    }

    /**
     * Constructs a new PosixRuntimeException with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    public PosixRuntimeException(Throwable cause) {
        super(cause);
        this.errno = 0;
    }

    /**
     * Constructs a new PosixRuntimeException with the specified detail message
     * and errno.
     * <p>
     * The errno mirrors the result of {@link PosixAPI#lastError()}.
     *
     * @param message The detail message for the exception.
     * @param errno   The POSIX error number.
     */
    public PosixRuntimeException(String message, int errno) {
        super(message);
        this.errno = errno;
    }

    /**
     * @return the errno associated with this exception
     */
    public int errno() {
        return errno;
    }
}
