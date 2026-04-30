package com.sysmap.hubapi.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("O e-mail ou CPF informado já pertence a outro usuário.");
    }
}
