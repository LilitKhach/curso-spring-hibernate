package com.finanalizador.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "contratos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String contratoNumero;

    @ManyToOne
    @JoinColumn(name = "socia_id")
    @JsonBackReference
    private Socia socia;

    @OneToMany(mappedBy = "contrato")
    @JsonManagedReference
    private List<Transaccion> transacciones;
}
