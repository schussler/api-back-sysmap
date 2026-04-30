package com.sysmap.hubapi.exception;

public class CheckInDoneException extends RuntimeException {
    public CheckInDoneException() {
        super("Não é possível cancelar sua inscrição, pois sua presença já foi confirmada.");
    }
}
