package com.pizzeria.modelo;

/**
 * Categoria de fidelidad del cliente, basada en su historial de pedidos.
 * REGLAS DE NEGOCIO:
 *   - 1 pedido: BRONCE
 *   - 2-3 pedidos: PLATA
 *   - 4+ pedidos: ORO
 *
 * Es un enum con campo descripcion: cada valor lleva su texto asociado.
 * En Spring con JPA se almacena como String en la base de datos.
 */
public enum CategoriaCliente {
    BRONCE("Bronce - Cliente nuevo"),
    PLATA("Plata - Cliente frecuente"),
    ORO("Oro - Cliente VIP");

    private final String descripcion;

    CategoriaCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
