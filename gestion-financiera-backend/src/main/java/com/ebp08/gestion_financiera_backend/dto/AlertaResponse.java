package com.ebp08.gestion_financiera_backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaResponse {
    // Contiene el historial de alertas que se devuelve al usuario.
    private List<AlertaResumenResponse> alertas;
}