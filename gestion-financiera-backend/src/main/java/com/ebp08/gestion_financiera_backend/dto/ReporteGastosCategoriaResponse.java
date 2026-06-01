package com.ebp08.gestion_financiera_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGastosCategoriaResponse {

    private Long idCategoria;
    private String nombreCategoria;
    private BigDecimal totalGastado;
    private long cantidadTransacciones;
}