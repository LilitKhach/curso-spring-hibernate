package com.finanalizador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SociaBeneficioDTO {
    private Long sociaId;
    private String nombre;
    private String apellido;
    private BigDecimal beneficioTotal;
    private List<ContratoBeneficioDTO> contratos;
}

