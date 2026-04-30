package com.sysmap.hubapi.exception;

public class AlreadyCheckedInException extends RuntimeException {
    public AlreadyCheckedInException() {
        super("Você já confirmou sua participação nesta atividade.");
    }
}
