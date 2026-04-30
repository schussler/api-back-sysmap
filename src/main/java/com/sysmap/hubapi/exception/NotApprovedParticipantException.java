package com.sysmap.hubapi.exception;

public class NotApprovedParticipantException extends RuntimeException {
    public NotApprovedParticipantException() {
        super("Apenas participantes aprovados na atividade podem fazer check-in.");
    }
}
