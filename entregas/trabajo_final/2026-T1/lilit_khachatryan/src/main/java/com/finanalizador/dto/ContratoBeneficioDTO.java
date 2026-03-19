package com.finanalizador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ContratoBeneficioDTO {
    private Long contratoId;
    private String contratoNumero;
    private BigDecimal beneficio;
}
