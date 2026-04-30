package com.sysmap.hubapi.exception;

public class MissingFieldsException extends RuntimeException {
    public MissingFieldsException() {
        super("Informe os campos obrigatórios corretamente.");
    }
}
