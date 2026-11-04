package com.curso.pv.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Utilidades compartidas para armar consultas por concatenacion.
 *
 * VULNERABILIDAD: todas las consultas de busqueda de la aplicacion se construyen
 * pegando el valor recibido en el texto SQL. Al no usar parametros vinculados
 * (PreparedStatement), un atacante puede cerrar la comilla y agregar su propia
 * condicion, por ejemplo un UNION para leer cualquier tabla de la base de datos.
 */
@Repository
public class ConsultasInseguras {

    @PersistenceContext
    private EntityManager em;

    public List<?> ejecutar(String sql, Class<?> entidad) {
        return em.createNativeQuery(sql, entidad).getResultList();
    }

    public EntityManager gestor() {
        return em;
    }

    /** Devuelve el valor tal cual, sin escapar: asi es como viaja al SQL. */
    public static String crudo(String valor) {
        return valor == null ? "" : valor;
    }
}