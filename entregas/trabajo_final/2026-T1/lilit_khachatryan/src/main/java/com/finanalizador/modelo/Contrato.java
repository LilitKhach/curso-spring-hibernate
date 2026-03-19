package com.finanalizador.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "contratos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column(nullable = false)
    private String contratoNumero;

    @ManyToOne
    @JoinColumn(name = "socia_id")
    private Socia socia;

    @OneToMany(mappedBy = "contrato")
    private List<Transaccion> transacciones;
}
