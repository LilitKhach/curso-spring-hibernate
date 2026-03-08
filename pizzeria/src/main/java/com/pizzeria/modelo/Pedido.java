package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Pedido - version JPA con relaciones.
 *
 * RELACIONES:
 *
 * @ManyToOne  -> Muchos pedidos pertenecen a UN cliente.
 *   En la tabla: columna "cliente_id" que apunta a la tabla "clientes".
 *
 * @ManyToMany -> Un pedido tiene MUCHAS pizzas, y una pizza puede estar
 *   en MUCHOS pedidos. Hibernate crea una tabla intermedia automaticamente.
 *
 * @JoinTable  -> Define la tabla intermedia para la relacion ManyToMany.
 *   - name: nombre de la tabla intermedia
 *   - joinColumns: columna que apunta a ESTA entidad (pedido_id)
 *   - inverseJoinColumns: columna que apunta a la OTRA entidad (pizza_id)
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToMany
    @JoinTable(
            name = "pedido_pizzas",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "pizza_id")
    )
    private List<Pizza> pizzas = new ArrayList<>();

    private LocalDateTime fecha;

    private double total;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     */
    protected Pedido() {
    }

    /**
     * Constructor para crear pedidos desde el codigo.
     */
    public Pedido(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.cliente = cliente;
        this.pizzas = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.total = 0.0;
    }

    // =====================================================
    // Metodos de negocio
    // =====================================================

    /**
     * Agrega una pizza al pedido y recalcula el total.
     */
    public void agregarPizza(Pizza pizza) {
        if (pizza == null) {
            throw new IllegalArgumentException("La pizza no puede ser nula");
        }
        this.pizzas.add(pizza);
        recalcularTotal();
    }

    /**
     * Recalcula el total sumando los precios de todas las pizzas.
     */
    public void recalcularTotal() {
        this.total = pizzas.stream()
                .mapToDouble(Pizza::getPrecio)
                .sum();
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public List<Pizza> getPizzas() { return pizzas; }
    public void setPizzas(List<Pizza> pizzas) { this.pizzas = pizzas; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getCantidadItems() { return pizzas.size(); }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido{id=%d, cliente='%s', pizzas=%d, total=%.2f, fecha=%s}",
                id,
                cliente != null ? cliente.getNombre() : "sin cliente",
                pizzas.size(),
                total,
                fecha));
        return sb.toString();
    }
}