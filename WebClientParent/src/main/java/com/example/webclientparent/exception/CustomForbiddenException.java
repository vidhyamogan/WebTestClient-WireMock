package com.example.webclientparent.exception;

public class CustomForbiddenException extends Throwable {
    int status;
    public CustomForbiddenException(int status) {
        this.status = status;
    }
}
