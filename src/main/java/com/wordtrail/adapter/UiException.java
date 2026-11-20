package com.wordtrail.adapter;

/** A user-facing controller failure understood by any delivery mechanism. */
public final class UiException extends RuntimeException {
    public UiException(String message, Throwable cause) { super(message, cause); }
}
