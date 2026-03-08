package com.pizzeria.repositorio;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.TipoCliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    List<Cliente> buscarTodos();

    Optional<Cliente> buscarPorNombre(String nombre);

    List<Cliente> buscarPorTipo(TipoCliente tipo);

    void eliminar(Long id);
}