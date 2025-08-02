package com.traffic.config.exception;

public class DataParseException extends ProtocolException {
    public DataParseException(String message) {
        super(message);
    }

    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
