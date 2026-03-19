package com.finanalizador.controlador;

import com.finanalizador.dto.DashboardSummaryDTO;
import com.finanalizador.dto.SociaBeneficioDTO;
import com.finanalizador.modelo.Socia;
import com.finanalizador.servicio.SociaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/socias")
public class SociaController {
    private final SociaService sociaService;

    public SociaController(SociaService service)
    {
        sociaService = service;
    }

    @PostMapping
    public ResponseEntity<Socia> registrarSocia(@Valid @RequestBody Socia socia) {
        try {
            var sociaRegistrada = sociaService.registrarSocia(socia);
            return ResponseEntity.status(HttpStatus.CREATED).body(sociaRegistrada);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Error al registrar la socia")
                    .build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Socia>> buscarTodosSocias() {
        var socias = sociaService.buscarSocias();
        return ResponseEntity.ok(socias);  // 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<Socia> buscarSociaPorId(@PathVariable Long id){
        Socia socia = sociaService.buscarSociaPorId(id);
        return ResponseEntity.ok(socia);
    }

    // Get beneficios for a specific Socia from-to date
    @GetMapping("{id}/beneficio")
    public ResponseEntity<BigDecimal> buscarBeneficioDeSocia(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        BigDecimal beneficio = sociaService.calcularBeneficio(id, from, to);
        return ResponseEntity.ok(beneficio);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<SociaBeneficioDTO>> obtenerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minBeneficio) {

        List<SociaBeneficioDTO> dashboardData = sociaService.calcularBeneficiosDashboard(from, to, minBeneficio);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDTO> obtenerResumenDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minBeneficio) {

        DashboardSummaryDTO summary = sociaService.obtenerResumenDashboard(from, to, minBeneficio);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Socia> actualizarSocia(@PathVariable Long id, @Valid @RequestBody Socia socia){
        var sociaActualizada = sociaService.actualizarSocia(id, socia);
        return ResponseEntity.ok(sociaActualizada); // Status = 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity eliminarSocia(@PathVariable Long id){
        sociaService.eliminarSocia(id);
        return ResponseEntity.noContent().build(); // Status = 204
    }
}

