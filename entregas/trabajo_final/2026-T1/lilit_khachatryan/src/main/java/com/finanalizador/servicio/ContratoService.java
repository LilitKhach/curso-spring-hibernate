package com.finanalizador.servicio;

import com.finanalizador.controlador.ResourceNotFoundException;
import com.finanalizador.dto.ContratoDetalleDTO;
import com.finanalizador.dto.TransaccionDTO;
import com.finanalizador.modelo.Contrato;
import com.finanalizador.modelo.TipoTransaccion;
import com.finanalizador.repositorio.ContratoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContratoService {
    private final ContratoRepository contratoRepository;

    public ContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    public Contrato buscarContratoPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no existe"));
    }

    public List<Contrato> buscarContratosPorSocia(Long sociaId) {
        return contratoRepository.findBySociaId(sociaId);
    }

    public List<ContratoDetalleDTO> obtenerContratosDelaSocia(Long sociaId, LocalDate from, LocalDate to) {
        List<Contrato> contratos = buscarContratosPorSocia(sociaId);
        return contratos.stream()
                .map(c -> {
                    BigDecimal beneficio = calcularBeneficioContrato(c, from, to);
                    List<TransaccionDTO> transacciones = c.getTransacciones().stream()
                            .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                                    (to == null || !t.getFecha().isAfter(to)))
                            .map(t -> new TransaccionDTO(t.getId(), t.getMonto(), t.getFecha(), t.getDescripcion(), t.getTipo()))
                            .toList();
                    return new ContratoDetalleDTO(c.getId(), c.getContratoNumero(), beneficio, transacciones.size(), transacciones);
                })
                .toList();
    }

    public ContratoDetalleDTO obtenerContratoDetalle(Long sociaId, Long contratoId, LocalDate from, LocalDate to) {
        Contrato contrato = buscarContratoPorId(contratoId);
        
        if (!contrato.getSocia().getId().equals(sociaId)) {
            throw new ResourceNotFoundException("Contrato no existe para esta Socia");
        }
        
        BigDecimal beneficio = calcularBeneficioContrato(contrato, from, to);
        List<TransaccionDTO> transacciones = contrato.getTransacciones().stream()
                .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                        (to == null || !t.getFecha().isAfter(to)))
                .map(t -> new TransaccionDTO(t.getId(), t.getMonto(), t.getFecha(), t.getDescripcion(), t.getTipo()))
                .toList();
        
        return new ContratoDetalleDTO(contrato.getId(), contrato.getContratoNumero(), beneficio, transacciones.size(), transacciones);
    }

    public BigDecimal calcularBeneficioContrato(Contrato contrato, LocalDate from, LocalDate to) {
        return contrato.getTransacciones().stream()
                .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                        (to == null || !t.getFecha().isAfter(to)))
                .map(t -> t.getTipo() == TipoTransaccion.INGRESO ? t.getMonto() : t.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Contrato guardarContrato(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public void eliminarContrato(Long id) {
        Contrato contrato = buscarContratoPorId(id);
        contratoRepository.deleteById(contrato.getId());
    }
}

