package com.finanalizador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ContratoDetalleDTO {
    private Long id;
    private String contratoNumero;
    private BigDecimal beneficioTotal;
    private int totalTransacciones;
    private List<TransaccionDTO> transacciones;
}

