package com.sysmap.hubapi.exception;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException() {
        super("Esta conta foi desativada e não pode ser utilizada.");
    }
}
