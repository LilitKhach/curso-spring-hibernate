package com.finanalizador.servicio;

import com.finanalizador.controlador.ResourceNotFoundException;
import com.finanalizador.dto.TransaccionDTO;
import com.finanalizador.modelo.Transaccion;
import com.finanalizador.repositorio.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransaccionService {
    private final TransaccionRepository transaccionRepository;

    public TransaccionService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public Transaccion buscarTransaccionPorId(Long id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaccion no existe"));
    }

    public List<Transaccion> buscarTransacionesPorContrato(Long contratoId) {
        return transaccionRepository.findByContratoId(contratoId);
    }

    public List<TransaccionDTO> obtenerTransacionesPorContrato(Long contratoId, LocalDate from, LocalDate to) {
        List<Transaccion> transacciones = buscarTransacionesPorContrato(contratoId);
        return transacciones.stream()
                .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                        (to == null || !t.getFecha().isAfter(to)))
                .map(t -> new TransaccionDTO(t.getId(), t.getMonto(), t.getFecha(), t.getDescripcion(), t.getTipo()))
                .toList();
    }

    public Transaccion guardarTransaccion(Transaccion transaccion) {
        return transaccionRepository.save(transaccion);
    }

    public Transaccion actualizarTransaccion(Long id, Transaccion transaccion) {
        Transaccion existente = buscarTransaccionPorId(id);
        existente.setMonto(transaccion.getMonto());
        existente.setFecha(transaccion.getFecha());
        existente.setDescripcion(transaccion.getDescripcion());
        existente.setTipo(transaccion.getTipo());
        return transaccionRepository.save(existente);
    }

    public void eliminarTransaccion(Long id) {
        Transaccion transaccion = buscarTransaccionPorId(id);
        transaccionRepository.deleteById(transaccion.getId());
    }
}

