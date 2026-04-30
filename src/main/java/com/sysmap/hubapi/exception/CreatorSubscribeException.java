package com.sysmap.hubapi.exception;

public class CreatorSubscribeException extends RuntimeException {
    public CreatorSubscribeException() {
        super("O criador da atividade não pode se inscrever como um participante.");
    }
}
