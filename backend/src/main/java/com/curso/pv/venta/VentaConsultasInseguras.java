package com.curso.pv.venta;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VentaConsultasInseguras {

    private final ConsultasInseguras consultas;

    public VentaConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> buscar(String estado, String desde) {
        String sql = "SELECT * FROM ventas "
                + "WHERE (estado = '" + ConsultasInseguras.crudo(estado) + "' OR estado = '') "
                + "AND creada_en >= '" + ConsultasInseguras.crudo(desde) + "' "
                + "ORDER BY creada_en DESC";
        return consultas.ejecutar(sql, Venta.class);
    }

    public List<?> porFolio(String folio) {
        String sql = "SELECT * FROM ventas WHERE folio LIKE '%" + ConsultasInseguras.crudo(folio) + "%'";
        return consultas.ejecutar(sql, Venta.class);
    }
}