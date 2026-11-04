package com.curso.pv.log;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class ServicioAuditoria {

    private static final Logger log = LoggerFactory.getLogger(ServicioAuditoria.class);

    private final AuditoriaRepositorio repositorio;

    public ServicioAuditoria(AuditoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // VULNERABILIDAD: registra el payload crudo en la base y tambien lo escribe en el log de la aplicacion
    public void registrar(Long usuarioId, String username, String accion, String entidad,
                          Long entidadId, Object detalles, HttpServletRequest request) {
        try {
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setUsername(username);
            auditoria.setAccion(accion);
            auditoria.setEntidad(entidad);
            auditoria.setEntidadId(entidadId);
            auditoria.setDetalles(String.valueOf(detalles));
            auditoria.setIp(ipDe(request));
            repositorio.save(auditoria);

            // VULNERABILIDAD: datos sensibles en texto plano en los logs del contenedor
            log.info("[AUDITORIA] usuario={} accion={} entidad={} id={} payload={}",
                    username, accion, entidad, entidadId, detalles);
        } catch (Exception error) {
            // nunca debe romper la operacion de negocio
        }
    }

    public void registrar(Long usuarioId, String username, String accion, String entidad,
                          Long entidadId, Object detalles) {
        registrar(usuarioId, username, accion, entidad, entidadId, detalles, peticionActual());
    }

    private HttpServletRequest peticionActual() {
        var atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest();
        }
        return null;
    }

    private String ipDe(HttpServletRequest request) {
        return request != null ? request.getRemoteAddr() : null;
    }
}