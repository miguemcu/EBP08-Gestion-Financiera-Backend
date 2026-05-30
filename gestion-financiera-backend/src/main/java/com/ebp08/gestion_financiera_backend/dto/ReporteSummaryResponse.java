package com.ebp08.gestion_financiera_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSummaryResponse {

    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal balance;           // totalIngresos - totalEgresos
    private BigDecimal porcentajeAhorro;  // (balance / totalIngresos) * 100
    private int mes;
    private int anio;
}