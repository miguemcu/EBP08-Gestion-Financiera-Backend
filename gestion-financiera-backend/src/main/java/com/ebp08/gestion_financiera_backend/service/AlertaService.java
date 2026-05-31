package com.ebp08.gestion_financiera_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ebp08.gestion_financiera_backend.dto.AlertaResponse;
import com.ebp08.gestion_financiera_backend.entity.Alerta;
import com.ebp08.gestion_financiera_backend.entity.Presupuesto;
import com.ebp08.gestion_financiera_backend.entity.Usuario;
import com.ebp08.gestion_financiera_backend.enums.TipoAlerta;
import com.ebp08.gestion_financiera_backend.repository.AlertaRepository;
import com.ebp08.gestion_financiera_backend.security.SecurityHelper;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AlertaService {
    
    private final AlertaRepository alertaRepository;
    private final SecurityHelper securityHelper;
    private final PresupuestoService presupuestoService;

    // Trae el historial de alertas del usuario autenticado para uso interno entre servicios.
    public List<Alerta> obtenerAlertasUsuario() {
        // Sincroniza el estado actual de los presupuestos antes de leer el historial,
        // para cubrir presupuestos creados después de las transacciones.
        generarAlertas();

        Long idUsuario = securityHelper.obtenerUsuarioAutenticado().getId();

        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del usuario no puede ser nulo.");
        }

        return alertaRepository.findByUsuarioIdOrderByFechaDesc(idUsuario);
    }

    // Trae el historial de alertas del usuario autenticado para respuestas del controller.
    public List<AlertaResponse> obtenerAlertasUsuarioResponse() {
        return obtenerAlertasUsuario()
            .stream()
            .map(alerta -> new AlertaResponse(
                    alerta.getId(),
                    alerta.getPresupuesto() != null ? alerta.getPresupuesto().getId() : null,
                    alerta.getTipo(),
                    alerta.getMensaje(),
                    alerta.getFecha()))
            .collect(Collectors.toList());
    }

    // Genera las alertas nuevas segun el uso de los presupuestos.
    public List<Alerta> generarAlertas(){

        List<Alerta> alertasGeneradas = new ArrayList<>();

        Usuario usuario = securityHelper.obtenerUsuarioAutenticado();

        Presupuesto presupuestoGlobal = presupuestoService.obtenerPresupuestoGlobalUsuario().orElse(null);
        if (presupuestoGlobal != null) {
            Alerta alerta = evaluarYGenerarAlerta(
                                                    presupuestoGlobal.getId(),
                                                    presupuestoService.calcularPorcentajeUsoPresupuesto(presupuestoGlobal),
                                                    usuario);
            if (alerta != null) { alertasGeneradas.add(alerta); }
        }

        List<Presupuesto> presupuestosCategoria = presupuestoService.obtenerResumenPresupuestoCategorias();
        for (Presupuesto presupuesto : presupuestosCategoria) {
            Alerta alerta = evaluarYGenerarAlerta(
                                                presupuesto.getId(), 
                                                presupuestoService.calcularPorcentajeUsoPresupuesto(presupuesto),
                                                usuario);
            if (alerta != null) { alertasGeneradas.add(alerta); }
        }

        return alertasGeneradas;
    }

    private Alerta evaluarYGenerarAlerta(Long idPresupuesto, BigDecimal porcentaje, Usuario usuario) {
        if (porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0
                && !alertaRepository.existsByPresupuestoIdAndTipo(idPresupuesto, TipoAlerta.SOBREPASO)) {
            return crearAlerta(idPresupuesto, usuario, TipoAlerta.SOBREPASO, porcentaje);

        } else if (porcentaje.compareTo(BigDecimal.valueOf(80)) >= 0
                && porcentaje.compareTo(BigDecimal.valueOf(100)) < 0
                && !alertaRepository.existsByPresupuestoIdAndTipo(idPresupuesto, TipoAlerta.PROXIMIDAD)) {
            return crearAlerta(idPresupuesto, usuario, TipoAlerta.PROXIMIDAD, porcentaje);
        }

        return null;
    }

    private Alerta crearAlerta(Long idPresupuesto, Usuario usuario, TipoAlerta tipo, BigDecimal porcentaje) {
        Presupuesto presupuesto = presupuestoService.obtenerPresupuestoPorId(idPresupuesto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                                            "Presupuesto no encontrado para generar alerta."));

        Alerta alerta = new Alerta();
        alerta.setUsuario(usuario);
        alerta.setPresupuesto(presupuesto);
        alerta.setTipo(tipo);

        // Se redondea para mostrar un mensaje mas limpio al usuario.
        String porcentajeRedondeado = porcentaje.setScale(0, RoundingMode.HALF_UP).toPlainString();
        
        alerta.setMensaje(String.format("⚠️ ¡Alerta! Has alcanzado el %s%% de tu presupuesto %s.", porcentajeRedondeado, 
            presupuesto.getCategoria() != null ? "en la categoría " + presupuesto.getCategoria().getNombre() : "global"));
        alerta.setFecha(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

}
