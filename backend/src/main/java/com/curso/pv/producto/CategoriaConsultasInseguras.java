package com.curso.pv.producto;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoriaConsultasInseguras {

    private final ConsultasInseguras consultas;

    public CategoriaConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> buscar(String texto) {
        String valor = ConsultasInseguras.crudo(texto);
        String sql = "SELECT * FROM categorias WHERE LOWER(nombre) LIKE '%" + valor + "%' ORDER BY nombre";
        return consultas.ejecutar(sql, Categoria.class);
    }
}