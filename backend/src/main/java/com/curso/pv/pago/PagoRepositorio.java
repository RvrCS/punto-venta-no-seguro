package com.curso.pv.pago;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {

    List<Pago> findByVentaId(Long ventaId);
}