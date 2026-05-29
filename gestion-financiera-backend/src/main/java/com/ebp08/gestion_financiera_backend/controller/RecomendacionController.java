package com.ebp08.gestion_financiera_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebp08.gestion_financiera_backend.service.RecomendacionService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/recomendaciones")
@AllArgsConstructor
public class RecomendacionController {
    private final RecomendacionService recomendacionService;

    // Endpoint para obtener recomendaciones personalizadas basadas en el balance mensual.
    @GetMapping("/balance")
    public ResponseEntity<String> obtenerRecomendacionesPorBalance() {
        String recomendaciones = recomendacionService.obtenerRecomendacionesPorBalance();
        return ResponseEntity.ok(recomendaciones);
    }

}
