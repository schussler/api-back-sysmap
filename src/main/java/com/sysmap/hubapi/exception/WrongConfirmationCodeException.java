package com.sysmap.hubapi.exception;

public class WrongConfirmationCodeException extends RuntimeException {
    public WrongConfirmationCodeException() {
        super("Código de confirmação incorreto.");
    }
}
