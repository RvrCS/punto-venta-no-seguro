package com.curso.pv.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.activo = true ORDER BY p.stock ASC")
    List<Producto> stockBajo();
}