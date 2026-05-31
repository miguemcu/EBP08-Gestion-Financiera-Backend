package com.ebp08.gestion_financiera_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebp08.gestion_financiera_backend.dto.AlertaResponse;
import com.ebp08.gestion_financiera_backend.service.AlertaService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/alertas")
@AllArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    // Devuelve el historial de alertas del usuario autenticado.
    @GetMapping("/usuario")
    public ResponseEntity<List<AlertaResponse>> obtenerHistorialAlertasUsuario() {
        return ResponseEntity.status(200).body(alertaService.obtenerAlertasUsuarioResponse());
    }
}