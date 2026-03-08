package com.pizzeria;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.CategoriaCliente;
import com.pizzeria.modelo.Pedido;
import com.pizzeria.modelo.Pizza;
import com.pizzeria.modelo.TipoCliente;
import com.pizzeria.repositorio.ClienteRepositoryJPA;
import com.pizzeria.repositorio.PizzaRepositoryJPA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

/**
 * Aplicacion principal de la Pizzeria - Version 5 (Hibernate).
 *
 * CAMBIO PRINCIPAL:
 * Antes: repositorios usaban HashMap (datos en RAM).
 * Ahora: repositorios usan EntityManager (datos en H2).
 *
 * La logica de negocio NO cambio. Solo cambio el DONDE se guardan los datos.
 */
public class PizzeriaApp {

    public static void main(String[] args) {

        System.out.println("=============================================");
        System.out.println("   PIZZERIA JAVA - Version 5 (Hibernate)");
        System.out.println("=============================================\n");

        // ==========================================================
        // 1. INICIALIZAR HIBERNATE
        //    EntityManagerFactory lee persistence.xml y configura todo
        // ==========================================================

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pizzeria-pu");
        EntityManager em = emf.createEntityManager();

        System.out.println("Hibernate inicializado. Base de datos H2 lista.\n");

        // ==========================================================
        // 2. CREAR REPOSITORIOS JPA
        //    Antes: new PizzaRepositoryMemoria()
        //    Ahora: new PizzaRepositoryJPA(em)
        // ==========================================================

        PizzaRepositoryJPA pizzaRepo = new PizzaRepositoryJPA(em);
        ClienteRepositoryJPA clienteRepo = new ClienteRepositoryJPA(em);

        // ==========================================================
        // 3. CREAR PIZZAS Y GUARDARLAS EN LA BASE DE DATOS
        // ==========================================================

        System.out.println("--- CREAR PIZZAS ---\n");

        Pizza margarita = new Pizza("Margarita", 8.50, Pizza.Categoria.CLASICA);
        Pizza carnivora = new Pizza("Carnivora", 12.00, Pizza.Categoria.CLASICA);
        Pizza trufa = new Pizza("Trufa Negra", 18.50, Pizza.Categoria.GOURMET);
        Pizza vegana = new Pizza("Mediterranea Vegana", 11.00, Pizza.Categoria.VEGANA);

        pizzaRepo.guardar(margarita);
        pizzaRepo.guardar(carnivora);
        pizzaRepo.guardar(trufa);
        pizzaRepo.guardar(vegana);

        System.out.println("Pizzas guardadas en la base de datos:");
        List<Pizza> todasLasPizzas = pizzaRepo.listarTodas();
        todasLasPizzas.forEach(System.out::println);

        // ==========================================================
        // 4. CREAR CLIENTES Y GUARDARLOS
        // ==========================================================

        System.out.println("\n--- CREAR CLIENTES ---\n");

        Cliente ana = new Cliente("Ana Lopez", TipoCliente.PERSONA);
        ana.setEmail("ana.lopez@gmail.com");

        Cliente carlos = new Cliente("Carlos Martinez", TipoCliente.PERSONA);

        Cliente banco = new Cliente("Banco Santander", TipoCliente.EMPRESA);
        banco.setEmail("pedidos@santander.es");
        banco.setTelefono("911234567");

        clienteRepo.guardar(ana);
        clienteRepo.guardar(carlos);
        clienteRepo.guardar(banco);

        System.out.println("Clientes guardados:");
        clienteRepo.buscarTodos().forEach(System.out::println);

        // ==========================================================
        // 5. CREAR UN PEDIDO CON RELACIONES
        //    Aqui se ven @ManyToOne y @ManyToMany en accion
        // ==========================================================

        System.out.println("\n--- CREAR PEDIDO ---\n");

        try {
            em.getTransaction().begin();

            Pedido pedido1 = new Pedido(ana);
            pedido1.agregarPizza(margarita);
            pedido1.agregarPizza(trufa);
            em.persist(pedido1);

            Pedido pedido2 = new Pedido(carlos);
            pedido2.agregarPizza(carnivora);
            pedido2.agregarPizza(vegana);
            pedido2.agregarPizza(margarita);
            em.persist(pedido2);

            Pedido pedidoBanco = new Pedido(banco);
            pedidoBanco.agregarPizza(margarita);
            pedidoBanco.agregarPizza(margarita);
            pedidoBanco.agregarPizza(carnivora);
            em.persist(pedidoBanco);

            em.getTransaction().commit();

            System.out.println("Pedidos guardados:");
            System.out.println("  " + pedido1);
            System.out.println("  " + pedido2);
            System.out.println("  " + pedidoBanco);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error al crear pedidos: " + e.getMessage());
        }

        // ==========================================================
        // 6. CONSULTAR PEDIDOS DESDE LA BASE DE DATOS
        // ==========================================================

        System.out.println("\n--- CONSULTAR PEDIDOS ---\n");

        List<Pedido> todosPedidos = em.createQuery(
                        "SELECT p FROM Pedido p", Pedido.class)
                .getResultList();

        System.out.println("Total de pedidos en la base de datos: " + todosPedidos.size());
        for (Pedido p : todosPedidos) {
            System.out.printf("  Pedido #%d - Cliente: %s - Pizzas: %d - Total: %.2f%n",
                    p.getId(),
                    p.getCliente().getNombre(),
                    p.getCantidadItems(),
                    p.getTotal());
        }

        // ==========================================================
        // 7. RELACIONES EN ACCION: acceder al cliente desde el pedido
        // ==========================================================

        System.out.println("\n--- RELACIONES EN ACCION ---\n");

        Pedido primerPedido = todosPedidos.get(0);
        System.out.println("Pedido #" + primerPedido.getId() + ":");
        System.out.println("  Cliente: " + primerPedido.getCliente().getNombre());
        System.out.println("  Tipo: " + primerPedido.getCliente().getTipo());
        System.out.println("  Pizzas:");
        for (Pizza pizza : primerPedido.getPizzas()) {
            System.out.printf("    - %s (%.2f)%n", pizza.getNombre(), pizza.getPrecio());
        }

        // ==========================================================
        // 8. BUSCAR POR ID
        // ==========================================================

        System.out.println("\n--- BUSCAR POR ID ---\n");

        pizzaRepo.buscarPorId(1L).ifPresent(pizza ->
                System.out.println("Pizza con id 1: " + pizza));

        clienteRepo.buscarPorId(2L).ifPresent(cliente ->
                System.out.println("Cliente con id 2: " + cliente));

        // ==========================================================
        // 9. ELIMINAR
        //    Solo se puede eliminar una pizza que NO este en un pedido.
        //    Si intentan borrar una pizza que esta en pedido_pizzas,
        //    H2 lanza error de integridad referencial (FK constraint).
        // ==========================================================

        System.out.println("\n--- ELIMINAR ---\n");

        // Creamos una pizza extra que NO esta en ningun pedido
        Pizza temporal = new Pizza("Pizza Temporal", 5.00, Pizza.Categoria.CLASICA);
        pizzaRepo.guardar(temporal);

        System.out.println("Pizzas antes de eliminar: " + pizzaRepo.listarTodas().size());
        pizzaRepo.eliminar(temporal.getId());
        System.out.println("Pizzas despues de eliminar: " + pizzaRepo.listarTodas().size());

        // ==========================================================
        // 10. CERRAR RECURSOS
        // ==========================================================

        em.close();
        emf.close();

        System.out.println("\n=============================================");
        System.out.println("   Hibernate cerrado. Datos persistidos.");
        System.out.println("=============================================");
    }
}