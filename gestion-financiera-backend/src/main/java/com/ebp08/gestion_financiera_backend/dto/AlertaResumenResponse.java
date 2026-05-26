package com.ebp08.gestion_financiera_backend.dto;

import java.time.LocalDateTime;

import com.ebp08.gestion_financiera_backend.enums.TipoAlerta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaResumenResponse {
    // Resume cada alerta con los datos utiles para el front.
    private Long id;
    private Long presupuestoId;
    private TipoAlerta tipo;
    private String mensaje;
    private LocalDateTime fecha;
}