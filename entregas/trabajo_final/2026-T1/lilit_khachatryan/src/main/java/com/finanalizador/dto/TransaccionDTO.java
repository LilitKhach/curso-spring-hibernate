package com.finanalizador.dto;

import com.finanalizador.modelo.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransaccionDTO {
    private Long id;
    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;
    private TipoTransaccion tipo;
}

