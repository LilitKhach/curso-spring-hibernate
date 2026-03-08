package com.pizzeria.repositorio;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.TipoCliente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del repositorio de clientes usando JPA/Hibernate.
 * Mismo patron que PizzaRepositoryJPA: transaccion -> operacion -> commit.
 */
public class ClienteRepositoryJPA implements ClienteRepository {

    private final EntityManager em;

    public ClienteRepositoryJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        try {
            em.getTransaction().begin();
            if (cliente.getId() == null) {
                em.persist(cliente);
            } else {
                em.merge(cliente);
            }
            em.getTransaction().commit();
            return cliente;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        Cliente cliente = em.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }

    @Override
    public List<Cliente> buscarTodos() {
        return em.createQuery("SELECT c FROM Cliente c", Cliente.class)
                .getResultList();
    }

    @Override
    public Optional<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> resultados = em.createQuery(
                        "SELECT c FROM Cliente c WHERE LOWER(c.nombre) = LOWER(:nombre)",
                        Cliente.class)
                .setParameter("nombre", nombre)
                .getResultList();
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Cliente> buscarPorTipo(TipoCliente tipo) {
        return em.createQuery(
                        "SELECT c FROM Cliente c WHERE c.tipo = :tipo",
                        Cliente.class)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    @Override
    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) {
                em.remove(cliente);
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