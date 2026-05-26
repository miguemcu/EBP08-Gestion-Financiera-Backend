package com.ebp08.gestion_financiera_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ebp08.gestion_financiera_backend.entity.Alerta;
import com.ebp08.gestion_financiera_backend.enums.TipoAlerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Devuelve las alertas del usuario ordenadas de la mas reciente a la mas antigua.
    List<Alerta> findByUsuarioIdOrderByFechaDesc(Long idUsuario);
    boolean existsByPresupuestoIdAndTipo(Long idPresupuesto, TipoAlerta tipo);
    
}
