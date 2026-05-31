package com.ebp08.gestion_financiera_backend.controller;

import com.ebp08.gestion_financiera_backend.dto.ReporteComparativoMensualResponse;
import com.ebp08.gestion_financiera_backend.dto.ReporteGastosCategoriaResponse;
import com.ebp08.gestion_financiera_backend.dto.ReporteIngresosCategoriaResponse;
import com.ebp08.gestion_financiera_backend.dto.ReporteSummaryResponse;
import com.ebp08.gestion_financiera_backend.service.ReporteService;
import lombok.AllArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@AllArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // Mes y año actuales como valor por defecto si no se envían
    private int mesActual() { return LocalDate.now().getMonthValue(); }
    private int anioActual() { return LocalDate.now().getYear(); }

    @GetMapping("/expenses")
    public ResponseEntity<List<ReporteGastosCategoriaResponse>> gastosPorCategoria(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;
        return ResponseEntity.ok(reporteService.obtenerGastosPorCategoria(mes, anio));
    }

    @GetMapping("/income")
    public ResponseEntity<List<ReporteIngresosCategoriaResponse>> ingresosPorCategoria(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;
        return ResponseEntity.ok(reporteService.obtenerIngresosPorCategoria(mes, anio));
    }

    @GetMapping("/expenses/export")
    public ResponseEntity<byte[]> exportarGastos(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;

        if (type.equalsIgnoreCase("csv")) {
            return ResponseEntity.ok()
                    .headers(headersDescarga("gastos_categoria_" + mes + "_" + anio + ".csv"))
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(reporteService.exportarGastosCSV(mes, anio));
        }

        if (type.equalsIgnoreCase("pdf")) {
            return ResponseEntity.ok()
                    .headers(headersDescarga("gastos_categoria_" + mes + "_" + anio + ".pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reporteService.exportarGastosPDF(mes, anio));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de exportación no válido. Use 'pdf' o 'csv'.");
    }

    @GetMapping("/income/export")
    public ResponseEntity<byte[]> exportarIngresos(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;

        if (type.equalsIgnoreCase("csv")) {
            return ResponseEntity.ok()
                    .headers(headersDescarga("ingresos_categoria_" + mes + "_" + anio + ".csv"))
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(reporteService.exportarIngresosCSV(mes, anio));
        }

        if (type.equalsIgnoreCase("pdf")) {
            return ResponseEntity.ok()
                    .headers(headersDescarga("ingresos_categoria_" + mes + "_" + anio + ".pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reporteService.exportarIngresosPDF(mes, anio));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de exportación no válido. Use 'pdf' o 'csv'.");
    }

    @GetMapping("/summary")
    public ResponseEntity<ReporteSummaryResponse> resumen(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;
        return ResponseEntity.ok(reporteService.obtenerResumen(mes, anio));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;

        if (type.equalsIgnoreCase("csv")) {
            byte[] archivo = reporteService.exportarCSV(mes, anio);
            return ResponseEntity.ok()
                    .headers(headersDescarga("reporte_" + mes + "_" + anio + ".csv"))
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(archivo);
        }

        if (type.equalsIgnoreCase("pdf")) {
            byte[] archivo = reporteService.exportarPDF(mes, anio);
            return ResponseEntity.ok()
                    .headers(headersDescarga("reporte_" + mes + "_" + anio + ".pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(archivo);
        }

        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Tipo de exportación no válido. Use 'pdf' o 'csv'.");
    }

    private HttpHeaders headersDescarga(String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(nombreArchivo).build());
        return headers;
    }
    @GetMapping("/monthly-comparison")
    public ResponseEntity<ReporteComparativoMensualResponse> comparativaMensual(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;
        return ResponseEntity.ok(reporteService.obtenerComparativaMensual(mes, anio));
    }

    @GetMapping("/monthly-comparison/export")
    public ResponseEntity<byte[]> exportarComparativa(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        int mes  = month == 0 ? mesActual()  : month;
        int anio = year  == 0 ? anioActual() : year;

        if (type.equalsIgnoreCase("csv")) {
            byte[] archivo = reporteService.exportarComparativaCSV(mes, anio);
            return ResponseEntity.ok()
                    .headers(headersDescarga("comparativa_mensual_csv_" + mes + "_" + anio + ".csv"))
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(archivo);
        }

        if (type.equalsIgnoreCase("pdf")) {
            byte[] archivo = reporteService.exportarComparativaPDF(mes, anio);
            return ResponseEntity.ok()
                    .headers(headersDescarga("comparativa_mensual_pdf_" + mes + "_" + anio + ".pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(archivo);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tipo de exportación no válido. Use 'pdf' o 'csv'.");
    }
}