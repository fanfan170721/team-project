package com.wordtrail.usecase;

/** A failure that may be shown to the user without a technical stack trace. */
public final class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
