package net.orbyfied.aspen.exception;

public class ConfigurationLoadException extends AspenException {

    public ConfigurationLoadException(String message) {
        super(message);
    }

    public ConfigurationLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationLoadException(Throwable cause) {
        super(cause);
    }

    public ConfigurationLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
