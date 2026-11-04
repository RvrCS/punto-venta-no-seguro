package com.curso.pv.venta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    List<Venta> findByClienteId(Long clienteId);
}