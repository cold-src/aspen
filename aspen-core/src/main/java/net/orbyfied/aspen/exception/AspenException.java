package net.orbyfied.aspen.exception;

public class AspenException extends RuntimeException {

    public AspenException() {

    }

    public AspenException(String message) {
        super(message);
    }

    public AspenException(String message, Throwable cause) {
        super(message, cause);
    }

    public AspenException(Throwable cause) {
        super(cause);
    }

    public AspenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
