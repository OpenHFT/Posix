package net.openhft.posix;

public class PosixRuntimeException extends RuntimeException {
    public PosixRuntimeException(String message) {
        super(message);
    }

    public PosixRuntimeException(Throwable cause) {
        super(cause);
    }
}
