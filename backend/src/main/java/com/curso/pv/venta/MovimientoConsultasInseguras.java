package com.curso.pv.venta;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MovimientoConsultasInseguras {

    private final ConsultasInseguras consultas;

    public MovimientoConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> buscar(String tipo, String desde) {
        String sql = "SELECT * FROM movimientos_inventario "
                + "WHERE tipo = '" + ConsultasInseguras.crudo(tipo) + "' "
                + "AND registrado_en >= '" + ConsultasInseguras.crudo(desde) + "' "
                + "ORDER BY registrado_en DESC";
        return consultas.ejecutar(sql, MovimientoInventario.class);
    }
}