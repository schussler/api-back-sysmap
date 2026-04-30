package com.sysmap.hubapi.exception;

public class InvalidImageFormatException extends RuntimeException {
    public InvalidImageFormatException() {
        super("A imagem deve ser um arquivo PNG ou JPG.");
    }
}
