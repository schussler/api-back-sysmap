package com.sysmap.hubapi.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Autenticação necessária.");
    }
}
