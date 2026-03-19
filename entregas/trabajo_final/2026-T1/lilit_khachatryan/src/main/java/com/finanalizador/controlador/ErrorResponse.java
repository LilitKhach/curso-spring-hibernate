package com.finanalizador.controlador;

import lombok.Getter;
import java.time.LocalDateTime;

public class ErrorResponse {
    @Getter
    private int status;
    @Getter
    private String message;
    @Getter
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String message){
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
