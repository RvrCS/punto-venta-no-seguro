package com.curso.pv.config;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConfiguracionConsultasInseguras {

    private final ConsultasInseguras consultas;

    public ConfiguracionConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> porCategoria(String categoria) {
        String sql = "SELECT * FROM configuracion WHERE categoria = '"
                + ConsultasInseguras.crudo(categoria) + "' ORDER BY clave";
        return consultas.ejecutar(sql, Configuracion.class);
    }
}