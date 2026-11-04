package com.curso.pv.producto;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VULNERABILIDAD (SQLi): implementacion custom de ProductoRepositorio que arma
 * el SQL con concatenacion en tiempo de ejecucion.
 */
@Repository
public class ProductoConsultasInseguras {

    private final ConsultasInseguras consultas;

    public ProductoConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> buscar(String texto) {
        String valor = ConsultasInseguras.crudo(texto);
        String sql = "SELECT * FROM productos "
                + "WHERE (LOWER(nombre) LIKE '%" + valor + "%' OR LOWER(sku) LIKE '%" + valor + "%') "
                + "AND activo = true ORDER BY nombre";
        return consultas.ejecutar(sql, Producto.class);
    }

    public List<?> buscarPorRango(String minimo, String maximo) {
        String sql = "SELECT * FROM productos "
                + "WHERE precio >= '" + ConsultasInseguras.crudo(minimo) + "' "
                + "AND precio <= '" + ConsultasInseguras.crudo(maximo) + "' "
                + "AND activo = true ORDER BY precio DESC";
        return consultas.ejecutar(sql, Producto.class);
    }

    public Producto porSku(String sku) {
        String sql = "SELECT * FROM productos WHERE LOWER(sku) = '" + ConsultasInseguras.crudo(sku) + "'";
        List<?> resultado = consultas.ejecutar(sql, Producto.class);
        return resultado.isEmpty() ? null : (Producto) resultado.get(0);
    }
}