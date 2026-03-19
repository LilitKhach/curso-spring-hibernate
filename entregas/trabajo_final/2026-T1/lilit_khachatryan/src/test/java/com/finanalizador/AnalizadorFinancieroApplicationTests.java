package com.finanalizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanalizador.controlador.GlobalExceptionHandler;
import com.finanalizador.controlador.ResourceNotFoundException;
import com.finanalizador.controlador.ContratoController;
import com.finanalizador.controlador.SociaController;
import com.finanalizador.controlador.TransaccionController;
import com.finanalizador.dto.DashboardSummaryDTO;
import com.finanalizador.dto.TransaccionDTO;
import com.finanalizador.modelo.Contrato;
import com.finanalizador.modelo.Socia;
import com.finanalizador.modelo.TipoTransaccion;
import com.finanalizador.servicio.ContratoService;
import com.finanalizador.servicio.SociaService;
import com.finanalizador.servicio.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig
class AnalizadorFinancieroApplicationTests {

    private MockMvc mockMvc;

    private SociaService sociaService;

    private ContratoService contratoService;

    private TransaccionService transaccionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sociaService = mock(SociaService.class);
        contratoService = mock(ContratoService.class);
        transaccionService = mock(TransaccionService.class);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new SociaController(sociaService),
                        new ContratoController(contratoService),
                        new TransaccionController(transaccionService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSociasReturns200AndList() throws Exception {
        Socia socia = buildSocia(1L, "Maria", "Lopez");
        given(sociaService.buscarSocias()).willReturn(List.of(socia));

        mockMvc.perform(get("/api/socias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Maria"))
                .andExpect(jsonPath("$[0].apellido").value("Lopez"));
    }

    @Test
    void getSociaByIdReturns200WhenExists() throws Exception {
        given(sociaService.buscarSociaPorId(1L)).willReturn(buildSocia(1L, "Ana", "Garcia"));

        mockMvc.perform(get("/api/socias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.apellido").value("Garcia"));
    }

    @Test
    void getSociaByIdReturns404WhenMissing() throws Exception {
        given(sociaService.buscarSociaPorId(99L))
                .willThrow(new ResourceNotFoundException("Socia no existe"));

        mockMvc.perform(get("/api/socias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Socia no existe"));
    }

    @Test
    void postSociaReturns201WhenBodyIsValid() throws Exception {
        Socia request = buildSocia(null, "Lucia", "Martinez");
        Socia response = buildSocia(3L, "Lucia", "Martinez");
        given(sociaService.registrarSocia(any(Socia.class))).willReturn(response);

        mockMvc.perform(post("/api/socias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Lucia"))
                .andExpect(jsonPath("$.apellido").value("Martinez"));
    }

    @Test
    void postSociaReturns500WhenBodyIsInvalid() throws Exception {
        Socia request = buildSocia(null, "", "");

        mockMvc.perform(post("/api/socias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBeneficioReturns200AndCalculatedAmount() throws Exception {
        given(sociaService.calcularBeneficio(
                eq(1L),
                eq(LocalDate.parse("2024-01-01")),
                eq(LocalDate.parse("2024-12-31"))))
                .willReturn(new BigDecimal("800.00"));

        mockMvc.perform(get("/api/socias/1/beneficio")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(800.00));
    }

    @Test
    void getDashboardSummaryReturns200AndTotals() throws Exception {
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                2,
                new BigDecimal("3000.00"),
                new BigDecimal("1500.00"));
        given(sociaService.obtenerResumenDashboard(null, null, null)).willReturn(summary);

        mockMvc.perform(get("/api/socias/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSocias").value(2))
                .andExpect(jsonPath("$.totalBeneficio").value(3000.00))
                .andExpect(jsonPath("$.averageBeneficio").value(1500.00));
    }

    @Test
    void getContratoByIdReturns200WhenExists() throws Exception {
        Contrato contrato = new Contrato();
        contrato.setId(1L);
        contrato.setContratoNumero("CTR-001");
        contrato.setSocia(buildSocia(1L, "Maria", "Lopez"));
        given(contratoService.buscarContratoPorId(1L)).willReturn(contrato);

        mockMvc.perform(get("/api/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contratoNumero").value("CTR-001"));
    }

    @Test
    void getContratoByIdReturns404WhenMissing() throws Exception {
        given(contratoService.buscarContratoPorId(99L))
                .willThrow(new ResourceNotFoundException("Contrato no existe"));

        mockMvc.perform(get("/api/contratos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Contrato no existe"));
    }

    @Test
    void getTransaccionesPorContratoReturns200AndTransactions() throws Exception {
        List<TransaccionDTO> transacciones = List.of(
                new TransaccionDTO(
                        1L,
                        new BigDecimal("1000.00"),
                        LocalDate.parse("2024-01-10"),
                        "Ingreso inicial",
                        TipoTransaccion.INGRESO)
        );
        given(transaccionService.obtenerTransacionesPorContrato(1L, null, null)).willReturn(transacciones);

        mockMvc.perform(get("/api/transacciones/contrato/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].monto").value(1000.00))
                .andExpect(jsonPath("$[0].tipo").value("INGRESO"));
    }

    @Test
    void getTransaccionByIdReturns404WhenMissing() throws Exception {
        given(transaccionService.buscarTransaccionPorId(99L))
                .willThrow(new ResourceNotFoundException("Transaccion no existe"));

        mockMvc.perform(get("/api/transacciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaccion no existe"));
    }

    @Test
    void deleteSociaReturns204WhenDeletionSucceeds() throws Exception {
        doNothing().when(sociaService).eliminarSocia(1L);

        mockMvc.perform(delete("/api/socias/1"))
                .andExpect(status().isNoContent());
    }

    private Socia buildSocia(Long id, String nombre, String apellido) {
        Socia socia = new Socia();
        socia.setId(id);
        socia.setNombre(nombre);
        socia.setApellido(apellido);
        return socia;
    }
}
