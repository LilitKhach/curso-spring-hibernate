package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Cliente - version JPA.
 *
 * @Enumerated(EnumType.STRING) guarda el valor del enum como texto.
 * Sin esta anotacion, Hibernate guardaria el ORDINAL (0, 1, 2),
 * que se rompe si alguien reordena los valores del enum.
 *
 * SIEMPRE usen EnumType.STRING. Nunca el ordinal.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipo;

    private String email;

    private String telefono;

    @Enumerated(EnumType.STRING)
    private CategoriaCliente categoria;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     */
    protected Cliente() {
    }

    /**
     * Constructor para crear clientes desde el codigo.
     */
    public Cliente(String nombre, TipoCliente tipo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.categoria = CategoriaCliente.BRONCE;
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoCliente getTipo() { return tipo; }
    public void setTipo(TipoCliente tipo) { this.tipo = tipo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public CategoriaCliente getCategoria() { return categoria; }
    public void setCategoria(CategoriaCliente categoria) { this.categoria = categoria; }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return String.format("[%s] %s (#%d) - %s",
                tipo, nombre, id, categoria.getDescripcion());
    }
}