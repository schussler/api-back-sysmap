package com.sysmap.hubapi.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // TODO: capturar exceções customizadas e retornar { "error": "mensagem" }
    // TODO: capturar MethodArgumentNotValidException → 400
}
