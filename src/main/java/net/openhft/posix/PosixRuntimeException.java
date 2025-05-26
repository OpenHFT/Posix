package net.openhft.posix;

/**
 * This class represents a runtime exception specific to POSIX operations.
 * It extends the standard {@link RuntimeException} to provide more specific error handling for POSIX-related errors.
 */
public class PosixRuntimeException extends RuntimeException {
    // Serialization version UID for ensuring compatibility during deserialization
    private static final long serialVersionUID = 0L;

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
