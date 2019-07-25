package com.monke.monkeybook.model.content.exception;

public class BookSourceException extends Exception{

    public BookSourceException(String message) {
        super(message);
    }

    public BookSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
