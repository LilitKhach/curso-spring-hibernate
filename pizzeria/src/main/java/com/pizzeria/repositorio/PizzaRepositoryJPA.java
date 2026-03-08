package com.pizzeria.repositorio;

import com.pizzeria.modelo.Pizza;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del repositorio de pizzas usando JPA/Hibernate.
 *
 * PATRON: cada operacion sigue estos pasos:
 * 1. Abrir transaccion
 * 2. Ejecutar la operacion
 * 3. Confirmar (commit)
 * 4. Si algo falla: deshacer (rollback)
 *
 * Este patron es IDENTICO para todas las entidades.
 * En Spring, @Transactional hace esto automaticamente.
 */
public class PizzaRepositoryJPA implements PizzaRepository {

    private final EntityManager em;

    public PizzaRepositoryJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public Pizza guardar(Pizza pizza) {
        try {
            em.getTransaction().begin();
            if (pizza.getId() == null) {
                em.persist(pizza);   // INSERT: pizza nueva
            } else {
                em.merge(pizza);     // UPDATE: pizza existente
            }
            em.getTransaction().commit();
            return pizza;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Optional<Pizza> buscarPorId(Long id) {
        Pizza pizza = em.find(Pizza.class, id);
        return Optional.ofNullable(pizza);
    }

    @Override
    public List<Pizza> listarTodas() {
        return em.createQuery("SELECT p FROM Pizza p", Pizza.class)
                .getResultList();
    }

    @Override
    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            Pizza pizza = em.find(Pizza.class, id);
            if (pizza != null) {
                em.remove(pizza);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}