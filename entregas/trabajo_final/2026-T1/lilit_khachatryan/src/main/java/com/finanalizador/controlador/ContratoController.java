package com.finanalizador.controlador;

import com.finanalizador.dto.ContratoDetalleDTO;
import com.finanalizador.modelo.Contrato;
import com.finanalizador.servicio.ContratoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {
    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un contrato por ID")
    public ResponseEntity<Contrato> buscarContratoPorId(@PathVariable Long id) {
        Contrato contrato = contratoService.buscarContratoPorId(id);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/socia/{sociaId}")
    @Operation(summary = "Obtener todos los contratos de una socia")
    public ResponseEntity<List<ContratoDetalleDTO>> obtenerContratosDelaSocia(
            @PathVariable Long sociaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<ContratoDetalleDTO> contratos = contratoService.obtenerContratosDelaSocia(sociaId, from, to);
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/socia/{sociaId}/contrato/{contratoId}")
    @Operation(summary = "Obtener detalle de un contrato específico")
    public ResponseEntity<ContratoDetalleDTO> obtenerContratoDetalle(
            @PathVariable Long sociaId,
            @PathVariable Long contratoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ContratoDetalleDTO contrato = contratoService.obtenerContratoDetalle(sociaId, contratoId, from, to);
        return ResponseEntity.ok(contrato);
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo contrato")
    public ResponseEntity<Contrato> crearContrato(@Valid @RequestBody Contrato contrato) {
        Contrato contratoGuardado = contratoService.guardarContrato(contrato);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoGuardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un contrato")
    public ResponseEntity<Contrato> actualizarContrato(@PathVariable Long id, @Valid @RequestBody Contrato contrato) {
        Contrato existente = contratoService.buscarContratoPorId(id);
        existente.setContratoNumero(contrato.getContratoNumero());
        Contrato actualizado = contratoService.guardarContrato(existente);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un contrato")
    public ResponseEntity<Void> eliminarContrato(@PathVariable Long id) {
        contratoService.eliminarContrato(id);
        return ResponseEntity.noContent().build();
    }
}

