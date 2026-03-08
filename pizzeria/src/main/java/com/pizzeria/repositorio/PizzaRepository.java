package com.pizzeria.repositorio;

import com.pizzeria.modelo.Pizza;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de pizzas.
 * Ahora trabaja con Long como id (antes no tenia id propio).
 */
public interface PizzaRepository {

    Pizza guardar(Pizza pizza);

    Optional<Pizza> buscarPorId(Long id);

    List<Pizza> listarTodas();

    void eliminar(Long id);
}