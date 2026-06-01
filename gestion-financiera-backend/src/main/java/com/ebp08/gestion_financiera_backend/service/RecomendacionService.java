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
            "La API de IA no está disponible temporalmente. Intenta nuevamente en unos minutos.";

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

        // 1. Obtener las transacciones del mes actual

        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        List<Transaccion> transaccionesMes = transaccionService.obtenerTransaccionesPorMes(mesActual, anioActual);

        BigDecimal totalIngresos = transaccionService.calcularTotalIngresos(transaccionesMes);
        BigDecimal totalGastos = transaccionService.calcularTotalGastos(transaccionesMes);
        BigDecimal balance = totalIngresos.subtract(totalGastos);

        // 2. Detallamos las transacciones para el prompt

        StringBuilder detalleTransacciones = new StringBuilder();
        transaccionesMes.forEach(t ->  detalleTransacciones.append(
            String.format("- %s | %s | %s | %s%n",
                t.getCategoria().getNombre(),
                t.getTipo(),
                t.getMonto(),
                t.getDescripcion() != null ? t.getDescripcion() : "Sin descripción")
        ));
        
        // 3. Construimos el prompt para Gemini

        String prompt = String.format("""
                Eres un asesor financiero inteligente. Basándote en el siguiente balance mensual y \
                detalle de transacciones, proporciona 3 recomendaciones personalizadas para \
                mejorar la salud financiera del usuario. 
                Recomienda acciones concretas, cortas y accionables en español, \
                como reducir gastos en ciertas categorías o , \
                mantener buenos hábitos o fortalecer el ahorro.
                
                Balance %s %d:
                - Ingresos totales: $%s
                - Egresos totales: $%s
                - Balance: $%s

                Transacciones del mes:
                %s

                Genera exactamente 3 recomendaciones numeradas basadas en los patrones que observas.
                """,

                // Para mostrar el nombre del mes en español
                LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")),
                // Las otras especificaciones del format
                anioActual,
                totalIngresos,
                totalGastos,
                balance,
                detalleTransacciones
        );

        return llamarAPI(prompt);
    }

    public String obtenerRecomendacionesPorAlertas() {
        
        // 1. Obtener las alertas generadas y presupuestos del usuario
        
        List<Alerta> alertas = alertaService.obtenerAlertasUsuario();

        if (alertas.isEmpty()) {
            return "No se han generado alertas de presupuesto para este mes. ¡Sigue así!";
        }

        List<Presupuesto> presupuestos = presupuestoService.obtenerResumenPresupuestoCategorias();

        // 2. Detallamos las alertas y los presupuestos para el prompt
        
        StringBuilder detalleAlertas = new StringBuilder();
        alertas.forEach(a -> detalleAlertas.append(
                String.format("- %s: %s%n", a.getTipo(), a.getMensaje())
        )); 

        StringBuilder detallePresupuestos = new StringBuilder();
        presupuestos.forEach(p -> detallePresupuestos.append(
            String.format("- %s: límite $%s, gastado $%s (%s%%)%n",
                        p.getCategoria().getNombre(),
                        p.getMontoLimite(),
                        presupuestoService.calcularGastoPresupuesto(p),
                        presupuestoService.calcularPorcentajeUsoPresupuesto(p))
        ));

        //. 3. Construimos el prompt para Gemini
        String prompt = String.format("""
                Eres un asesor financiero inteligente. Basándote en las siguientes alertas de presupuesto \
                y el detalle de los presupuestos por categoría, proporciona 3 recomendaciones personalizadas para \
                mejorar la salud financiera del usuario. 
                Recomienda acciones concretas, cortas y accionables en español, \
                como reducir gastos en ciertas categorías o fortalecer el ahorro.

                Alertas activas:
                %s

                Detalle de presupuestos por categoría:
                %s

                Genera exactamente 3 recomendaciones numeradas basadas en los patrones que observas.
                """,
                detalleAlertas,
                detallePresupuestos
        );

        return llamarAPI(prompt);
    }

    private String llamarAPI(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                        List.of(new GeminiRequest.Part(prompt))
                ))
        );

        try {
            GeminiResponse response = restTemplate.postForObject(
                url,
                request,
                GeminiResponse.class
            );

            if (response == null || response.getCandidates() == null
                    || response.getCandidates().isEmpty()) {
                return "No se pudieron generar recomendaciones en este momento.";
            }

            return response.getCandidates()
                            .get(0)
                            .getContent()
                            .getParts()
                            .get(0)
                            .getText();
        } catch (HttpStatusCodeException ex) {
            System.err.println("Error llamando a Gemini. Status: " + ex.getStatusCode().value()
                    + ", body: " + ex.getResponseBodyAsString());
            return IA_NO_DISPONIBLE;
        } catch (RestClientException ex) {
            System.err.println("Error de red o cliente llamando a Gemini: " + ex.getMessage());
            return IA_NO_DISPONIBLE;
        }
    }
}
