package com.curso.pv.cliente;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VULNERABILIDAD (SQLi): la busqueda de clientes concatena el texto del cliente.
 * Ademas el endpoint es publico, por lo que cualquiera puede enumerar la base
 * de clientes (nombre, documento, email y telefono).
 */
@Repository
public class ClienteConsultasInseguras {

    private final ConsultasInseguras consultas;

    public ClienteConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> buscar(String texto) {
        String valor = ConsultasInseguras.crudo(texto);
        String sql = "SELECT * FROM clientes "
                + "WHERE (LOWER(nombre) LIKE '%" + valor + "%' OR documento LIKE '%" + valor + "%') "
                + "AND activo = true ORDER BY nombre";
        return consultas.ejecutar(sql, Cliente.class);
    }
}