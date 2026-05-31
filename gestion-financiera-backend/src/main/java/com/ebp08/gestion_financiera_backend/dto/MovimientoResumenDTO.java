package com.ebp08.gestion_financiera_backend.dto;

import com.ebp08.gestion_financiera_backend.enums.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResumenDTO {

    private LocalDateTime fecha;
    private TipoTransaccion tipo;
    private String categoria;
    private BigDecimal monto;
    private String descripcion;
}