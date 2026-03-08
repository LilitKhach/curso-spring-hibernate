package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Pizza - version JPA.
 *
 * Esta version simplifica la pizza original para poder persistirla
 * con Hibernate. La jerarquia de herencia (PizzaClasica, PizzaGourmet)
 * se puede mapear despues con @Inheritance.
 *
 * Cada anotacion le dice algo a Hibernate:
 * - @Entity: "esta clase se mapea a una tabla"
 * - @Table: "la tabla se llama 'pizzas'"
 * - @Id: "este campo es la clave primaria"
 * - @GeneratedValue: "genera el id automaticamente (autoincrement)"
 * - @Enumerated: "guarda el enum como texto, no como numero"
 */
@Entity
@Table(name = "pizzas")
public class Pizza {

    // =====================================================
    // Enum para las categorias de pizza
    // =====================================================

    public enum Categoria {
        CLASICA,
        GOURMET,
        VEGANA,
        SIN_GLUTEN
    }

    // =====================================================
    // Campos persistentes
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private double precio;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     * Hibernate necesita crear el objeto primero y luego llenar los campos.
     * Puede ser protected (no necesita ser publico).
     */
    protected Pizza() {
    }

    /**
     * Constructor para crear pizzas desde el codigo.
     */
    public Pizza(String nombre, double precio, Categoria categoria) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        }
        this.nombre = nombre.trim();
        this.precio = precio;
        this.categoria = categoria;
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return String.format("Pizza{id=%d, nombre='%s', precio=%.2f, categoria=%s}",
                id, nombre, precio, categoria);
    }
}