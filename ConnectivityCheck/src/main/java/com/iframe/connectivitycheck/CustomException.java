package com.iframe.connectivitycheck;

public class CustomException extends RuntimeException {
    String message;
    Throwable cause;

    public CustomException() {
        super();
    }

    public CustomException(String message, Throwable cause)
    {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }
}
