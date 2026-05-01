package com.sysmap.hubapi.exception;

public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException() {
        super("Participante não encontrado.");
    }
}
