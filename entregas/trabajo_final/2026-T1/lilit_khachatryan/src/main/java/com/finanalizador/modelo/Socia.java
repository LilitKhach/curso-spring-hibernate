package com.finanalizador.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "socias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Socia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "El nombre de la Socia es obligatorio")
    @Size(min = 2, message = "El nombre debe tener mas de 2 letras")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido de la Socia es obligatorio")
    @Size(min = 2, message = "El apellido debe tener mas de 2 letras")
    @Column(nullable = false)
    private String apellido;

    @OneToMany(mappedBy = "socia")
    private List<Contrato> contratos;
}
