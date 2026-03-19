package com.finanalizador.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;  // GASTOS / INGRESOS

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;
}