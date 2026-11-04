package com.curso.pv.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VentaItemRepositorio extends JpaRepository<VentaItem, Long> {

    @Query("SELECT i FROM VentaItem i WHERE i.ventaId = :ventaId")
    List<VentaItem> porVenta(@Param("ventaId") Long ventaId);
}