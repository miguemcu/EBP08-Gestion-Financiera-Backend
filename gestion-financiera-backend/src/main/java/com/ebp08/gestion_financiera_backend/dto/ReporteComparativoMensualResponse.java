package com.ebp08.gestion_financiera_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteComparativoMensualResponse {

    private String nombreUsuario;
    private int mes;
    private int anio;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balance;
    private String estadoBalance;       // "Superávit", "Déficit" o "Equilibrio"
    private BigDecimal montoDeficit;    // 0 si no hay déficit
    private BigDecimal porcentajeAhorro;
    private DatosGraficoDTO datosGrafico;
    private List<MovimientoResumenDTO> movimientosResumen;
}