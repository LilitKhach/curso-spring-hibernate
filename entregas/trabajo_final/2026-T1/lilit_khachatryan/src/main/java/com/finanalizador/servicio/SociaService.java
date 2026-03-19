package com.finanalizador.servicio;

import com.finanalizador.controlador.ResourceNotFoundException;
import com.finanalizador.dto.ContratoBeneficioDTO;
import com.finanalizador.dto.DashboardSummaryDTO;
import com.finanalizador.dto.SociaBeneficioDTO;
import com.finanalizador.modelo.Socia;
import com.finanalizador.modelo.TipoTransaccion;
import com.finanalizador.repositorio.SociaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SociaService{
    private final SociaRepository sociaRepository;

    public SociaService(SociaRepository repository){
        sociaRepository = repository;
    }

    public Socia buscarSociaPorId(Long id) {
        return sociaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socia no existe"));
    }

    public List<Socia> buscarSocias() {
        return sociaRepository.findAll();
    }

    public Socia registrarSocia(Socia socia){
//    try{
        return sociaRepository.save(socia);
//    }
//    catch (DataIntegrityViolationException e) {
//        throw new BadRequestException("Error registering Socia");
//    }
    }

    public BigDecimal calcularBeneficio(Long id, LocalDate from, LocalDate to){
        Socia socia = buscarSociaPorId(id);
        return calcularBeneficioContrato(socia, from, to);
    }

    public List<SociaBeneficioDTO> calcularBeneficiosDashboard(LocalDate from, LocalDate to, BigDecimal minBeneficio){
        return sociaRepository.findAll().stream()
                .map(s -> {
                    // calcular beneficio por contrato
                    List<ContratoBeneficioDTO> contratos = s.getContratos().stream()
                            .map(c -> {
                                BigDecimal beneficio = c.getTransacciones().stream()
                                        .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                                                (to == null || !t.getFecha().isAfter(to)))
                                        .map(t -> t.getTipo() == TipoTransaccion.INGRESO ? t.getMonto() : t.getMonto().negate())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                return new ContratoBeneficioDTO(c.getId(), c.getContratoNumero(), beneficio);
                            })
                            // filtro contratos por minBeneficio opcional
                            .filter(cdto -> minBeneficio == null || cdto.getBeneficio().compareTo(minBeneficio) >= 0)
                            .toList();

                    BigDecimal beneficioTotal = contratos.stream()
                            .map(ContratoBeneficioDTO::getBeneficio)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new SociaBeneficioDTO(s.getId(), s.getNombre(), s.getApellido(), beneficioTotal, contratos);
                })
                // filtro Socias por beneficioTotal opcional
                .filter(sdto -> minBeneficio == null || sdto.getBeneficioTotal().compareTo(minBeneficio) >= 0)
                .toList();
    }

    public DashboardSummaryDTO obtenerResumenDashboard(LocalDate from, LocalDate to, BigDecimal minBeneficio) {
        List<SociaBeneficioDTO> beneficios = calcularBeneficiosDashboard(from, to, minBeneficio);
        int totalSocias = beneficios.size();
        BigDecimal totalBeneficio = beneficios.stream()
                .map(SociaBeneficioDTO::getBeneficioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageBeneficio = totalSocias > 0 ? totalBeneficio.divide(BigDecimal.valueOf(totalSocias), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        return new DashboardSummaryDTO(totalSocias, totalBeneficio, averageBeneficio);
    }

    @Transactional
    public Socia actualizarSocia(Long id, Socia socia){
        Socia sociaExiste = buscarSociaPorId(id);
        sociaExiste.setNombre(socia.getNombre());
        return sociaExiste; // auto saved at transaction
    }

    public void eliminarSocia(Long id){
        Socia socia = buscarSociaPorId(id);
        sociaRepository.deleteById(socia.getId());
    }

    public BigDecimal calcularBeneficioContrato(Socia socia, LocalDate from, LocalDate to){
        return socia.getContratos().stream()
                .flatMap(c -> c.getTransacciones().stream())
                .filter(t -> (from == null || !t.getFecha().isBefore(from)) &&
                        (to == null || !t.getFecha().isAfter(to)))
                .map(t -> t.getTipo() == TipoTransaccion.INGRESO ? t.getMonto() : t.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

