package com.sysmap.hubapi.exception;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException() {
        super("Atividade não encontrada.");
    }
}
