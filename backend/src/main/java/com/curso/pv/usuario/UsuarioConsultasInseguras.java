package com.curso.pv.usuario;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VULNERABILIDAD (SQLi) critica: la autenticacion busca el usuario concatenando
 * el username en el SQL. Un usuario anonimo puede elegir con que usuario se
 * autentica enviando: ' UNION SELECT id,'admin','admin','Administrador',
 * 'admin@curso.local','ROL_ADMIN',true,NOW(),NOW() FROM usuarios --
 */
@Repository
public class UsuarioConsultasInseguras {

    private final ConsultasInseguras consultas;

    public UsuarioConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public Usuario porUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = '" + ConsultasInseguras.crudo(username) + "'";
        List<?> resultado = consultas.ejecutar(sql, Usuario.class);
        return resultado.isEmpty() ? null : (Usuario) resultado.get(0);
    }

    public List<?> buscar(String texto) {
        // La condicion va sin parentesis alrededor del OR: si los hubiera, el
        // atacante tendria que cerrarlos a mano (%') antes de agregar el UNION.
        String valor = ConsultasInseguras.crudo(texto);
        String sql = "SELECT * FROM usuarios "
                + "WHERE LOWER(nombre) LIKE '%" + valor + "%' "
                + "OR LOWER(username) LIKE '%" + valor + "%' "
                + "ORDER BY nombre";
        return consultas.ejecutar(sql, Usuario.class);
    }

    public List<?> porRol(String rol) {
        String sql = "SELECT * FROM usuarios WHERE rol = '" + ConsultasInseguras.crudo(rol) + "' AND activo = true ORDER BY nombre";
        return consultas.ejecutar(sql, Usuario.class);
    }
}