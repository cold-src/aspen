package net.orbyfied.aspen.raw.exception;

import org.jetbrains.annotations.Contract;

/**
 * Utilities for raw node errors.
 */
public class RawExceptions {

    /* Exception Classes */

    public static class UnexpectedNodeException extends RuntimeException {
        public UnexpectedNodeException(String message) {
            super(message);
        }

        public UnexpectedNodeException(String message, Throwable cause) {
            super(message, cause);
        }

        public UnexpectedNodeException(Throwable cause) {
            super(cause);
        }

        public UnexpectedNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /* Factory Methods */

    public static UnexpectedNodeException newUnexpectedNode(String msg) {
        return new UnexpectedNodeException(msg);
    }

    @Contract("_ -> fail")
    public static Object failUnexpectedNode(String msg) {
        throw newUnexpectedNode(msg);
    }

}
