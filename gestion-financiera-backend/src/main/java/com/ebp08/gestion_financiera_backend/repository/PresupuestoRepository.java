package com.ebp08.gestion_financiera_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebp08.gestion_financiera_backend.entity.Presupuesto;
import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

   List<Presupuesto> findByUsuarioId(Long idUsuario);


    // Consulta personalizada para encontrar un presupuesto global del mes actual por usuario
   @Query("SELECT p FROM Presupuesto p WHERE p.usuario.id = :usuarioId " + // Filtra por usuario
      "AND p.categoria IS NULL " + // Filtra presupuesto global (sin categoría)
      "AND MONTH(p.fechaLimite) = MONTH(CURRENT_DATE) " +// Filtra por mes actual
      "AND YEAR(p.fechaLimite) = YEAR(CURRENT_DATE)")// Filtra por año actual

    
   Optional<Presupuesto> findByUsuarioIdAndMesActual(@Param("usuarioId") Long usuarioId); 

    // Consulta personalizada para encontrar un presupuesto por categoría en el mes actual
   @Query("SELECT p FROM Presupuesto p WHERE p.usuario.id = :usuarioId " + 
      "AND p.categoria.id = :categoriaId " + // Filtra por categoría específica, ":" es un placeholder para el parámetro que se pasará a la consulta
      "AND MONTH(p.fechaLimite) = MONTH(CURRENT_DATE) " +
      "AND YEAR(p.fechaLimite) = YEAR(CURRENT_DATE)")


   Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndMesActual( 
      @Param("usuarioId") Long usuarioId, 
      @Param("categoriaId") Long categoriaId); // @param para pasar el id de la categoría como parámetro a la consulta personalizada

   void deleteByCategoriaId(Long idCategoria);


   Optional<Presupuesto> findByIdAndUsuarioId(Long idPresupuesto, Long idUsuario);

}
