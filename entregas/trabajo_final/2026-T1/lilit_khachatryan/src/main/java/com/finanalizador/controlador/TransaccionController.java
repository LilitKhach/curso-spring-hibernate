package com.finanalizador.controlador;

import com.finanalizador.dto.TransaccionDTO;
import com.finanalizador.modelo.Transaccion;
import com.finanalizador.servicio.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {
    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una transaccion por ID")
    public ResponseEntity<Transaccion> buscarTransaccionPorId(@PathVariable Long id) {
        Transaccion transaccion = transaccionService.buscarTransaccionPorId(id);
        return ResponseEntity.ok(transaccion);
    }

    @GetMapping("/contrato/{contratoId}")
    @Operation(summary = "Obtener todas las transacciones de un contrato")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransacionesPorContrato(
            @PathVariable Long contratoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<TransaccionDTO> transacciones = transaccionService.obtenerTransacionesPorContrato(contratoId, from, to);
        return ResponseEntity.ok(transacciones);
    }

    @PostMapping
    @Operation(summary = "Crear una nueva transaccion")
    public ResponseEntity<Transaccion> crearTransaccion(@Valid @RequestBody Transaccion transaccion) {
        Transaccion transaccionGuardada = transaccionService.guardarTransaccion(transaccion);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionGuardada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una transaccion")
    public ResponseEntity<Transaccion> actualizarTransaccion(@PathVariable Long id, @Valid @RequestBody Transaccion transaccion) {
        Transaccion actualizada = transaccionService.actualizarTransaccion(id, transaccion);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una transaccion")
    public ResponseEntity<Void> eliminarTransaccion(@PathVariable Long id) {
        transaccionService.eliminarTransaccion(id);
        return ResponseEntity.noContent().build();
    }
}

