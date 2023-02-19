package net.orbyfied.aspen.exception;

import org.jetbrains.annotations.Contract;

/**
 * Utilities for working with property errors.
 */
public class PropertyExceptions {

    public static class ValueException extends RuntimeException {
        public ValueException(String message) {
            super(message);
        }

        public ValueException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValueException(Throwable cause) {
            super(cause);
        }

        public ValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static ValueException newValueError(String str) {
        return new ValueException(str);
    }

    @Contract("_ -> fail")
    public static <T> T failValueError(String str) {
        throw newValueError(str);
    }

    public static ValueException newIllegalValue(Object value, String expectation) {
        return new ValueException("illegal value(" + value + "), " + expectation);
    }

    @Contract("_, _ -> fail")
    public static <T> T failIllegalValue(Object value, String expectation) {
        throw newIllegalValue(value, expectation);
    }

}
