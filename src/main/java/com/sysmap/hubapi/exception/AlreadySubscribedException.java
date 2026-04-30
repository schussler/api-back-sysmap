package com.sysmap.hubapi.exception;

public class AlreadySubscribedException extends RuntimeException {
    public AlreadySubscribedException() {
        super("Você já se registrou nesta atividade.");
    }
}
