package com.ebp08.gestion_financiera_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ebp08.gestion_financiera_backend.dto.GeminiRequest;
import com.ebp08.gestion_financiera_backend.dto.GeminiResponse;
import com.ebp08.gestion_financiera_backend.entity.Alerta;
import com.ebp08.gestion_financiera_backend.entity.Presupuesto;
import com.ebp08.gestion_financiera_backend.entity.Transaccion;

@Service
public class RecomendacionService {

    private static final String IA_NO_DISPONIBLE =
            "El servicio de recomendaciones no está disponible temporalmente. Intenta nuevamente más tarde.";

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final TransaccionService transaccionService;
    private final AlertaService alertaService;
    private final PresupuestoService presupuestoService;

    public RecomendacionService(RestTemplate restTemplate, 
                                TransaccionService transaccionService, 
                                AlertaService alertaService,
                                PresupuestoService presupuestoService) {
        this.restTemplate = restTemplate;
        this.transaccionService = transaccionService;
        this.alertaService = alertaService;
        this.presupuestoService = presupuestoService;
    }
    
    public String obtenerRecomendacionesPorBalance() {
        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        List<Transaccion> transaccionesMes = transaccionService.obtenerTransaccionesPorMes(mesActual, anioActual);

        BigDecimal totalIngresos = transaccionService.calcularTotalIngresos(transaccionesMes);
        BigDecimal totalGastos = transaccionService.calcularTotalGastos(transaccionesMes);
        BigDecimal balance = totalIngresos.subtract(totalGastos);

        StringBuilder detalleTransacciones = new StringBuilder();
        transaccionesMes.forEach(t -> detalleTransacciones.append(
            String.format("- %s | %s | $%s | %s%n",
                t.getCategoria().getNombre(),
                t.getTipo(),
                t.getMonto(),
                t.getDescripcion() != null ? t.getDescripcion() : "Sin descripción")
        ));
        
        String prompt = String.format("""
                Eres un asesor financiero inteligente. Basándote en el siguiente balance mensual y \
                detalle de transacciones, proporciona exactamente 3 recomendaciones personalizadas y numeradas \
                para mejorar la salud financiera del usuario. 
                Recomienda acciones concretas, cortas y accionables en español.
                
                Balance de %s %d:
                - Ingresos totales: $%s
                - Gastos totales: $%s
                - Balance Neto: $%s

                Transacciones del mes:
                %s
                """,
                LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")),
                anioActual, totalIngresos, totalGastos, balance, detalleTransacciones
        );

        return llamarAPI(prompt);
    }

    public String obtenerRecomendacionesPorAlertas() {
        List<Alerta> alertas = alertaService.obtenerAlertasUsuario();

        if (alertas.isEmpty()) {
            return "No se han detectado alertas de presupuesto este mes. ¡Tu planificación va excelente!";
        }

        List<Presupuesto> presupuestos = presupuestoService.obtenerResumenPresupuestoCategorias();

        StringBuilder detalleAlertas = new StringBuilder();
        alertas.forEach(a -> detalleAlertas.append(String.format("- %s: %s%n", a.getTipo(), a.getMensaje()))); 

        StringBuilder detallePresupuestos = new StringBuilder();
        presupuestos.forEach(p -> detallePresupuestos.append(
            String.format("- %s: límite $%s, gastado $%s (%s%%)%n",
                        p.getCategoria().getNombre(),
                        p.getMontoLimite(),
                        presupuestoService.calcularGastoPresupuesto(p),
                        presupuestoService.calcularPorcentajeUsoPresupuesto(p))
        ));

        String prompt = String.format("""
                Eres un asesor financiero experto en control de presupuestos. El usuario tiene las siguientes alertas \
                de gastos excesivos. Genera exactamente 3 recomendaciones numeradas y breves en español para contener \
                el sobregasto:

                Alertas activas:
                %s

                Detalle de presupuestos:
                %s
                """,
                detalleAlertas, detallePresupuestos
        );

        return llamarAPI(prompt);
    }

    /**
     * Comunicación con la API nativa de Google Gemini utilizando la estructura de tu CURL
     */
    private String llamarAPI(String prompt) {
        // Construimos la estructura exacta del JSON: contents -> parts -> text
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        GeminiRequest request = new GeminiRequest(List.of(content));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        
        // Configuramos la cabecera personalizada requerida por la API nativa de Google
        headers.set("X-goog-api-key", apiKey);

        org.springframework.http.HttpEntity<GeminiRequest> entity = 
                new org.springframework.http.HttpEntity<>(request, headers);

        try {
            org.springframework.http.ResponseEntity<GeminiResponse> responseEntity = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.POST,
                entity,
                GeminiResponse.class
            );

            GeminiResponse response = responseEntity.getBody();

            // Validación exhaustiva del árbol del JSON de respuesta de Google
            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                return "La IA de Google no devolvió candidatos válidos.";
            }

            GeminiResponse.Candidate primerCandidato = response.getCandidates().get(0);
            if (primerCandidato.getContent() == null || 
                primerCandidato.getContent().getParts() == null || 
                primerCandidato.getContent().getParts().isEmpty()) {
                return "Google respondió, pero la estructura del contenido está vacía.";
            }

            // Retornamos el texto limpio generado de la primera parte del primer candidato
            return primerCandidato.getContent().getParts().get(0).getText();

        } catch (HttpStatusCodeException ex) {
            System.err.println("Error en Gemini API Nativa. HTTP Status: " + ex.getStatusCode().value()
                    + " | Body: " + ex.getResponseBodyAsString());
            return IA_NO_DISPONIBLE;
        } catch (RestClientException ex) {
            System.err.println("Error de red con los servidores de Google: " + ex.getMessage());
            return IA_NO_DISPONIBLE;
        }
    }
}