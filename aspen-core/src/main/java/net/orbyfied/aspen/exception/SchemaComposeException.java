package net.orbyfied.aspen.exception;

/**
 * An exception which occurs when schema composing
 * failed.
 */
public class SchemaComposeException extends RuntimeException {

    public SchemaComposeException(String message) {
        super(message);
    }

    public SchemaComposeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaComposeException(Throwable cause) {
        super(cause);
    }

    public SchemaComposeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
