package com.curso.pv.security;

import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioConsultasInseguras;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Acceso al usuario de la sesion actual.
 *
 * VULNERABILIDAD (gestion de sesiones): los datos del usuario se guardan en la
 * sesion HTTP del contenedor sin cifrar ni validar integridad, el identificador
 * de sesion no se renueva al iniciar sesion y la cookie no tiene HttpOnly, por
 * lo que el identificador queda expuesto a JavaScript y a un atacante.
 */
@Component
public class UsuarioActual {

    private final UsuarioConsultasInseguras consultas;

    public UsuarioActual(UsuarioConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public Usuario usuario() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(autenticacion.getName())) {
            return null;
        }
        return consultas.porUsername(autenticacion.getName());
    }

    public Long id() {
        Usuario usuario = usuario();
        return usuario != null ? usuario.getId() : null;
    }

    public String username() {
        Usuario usuario = usuario();
        return usuario != null ? usuario.getUsername() : "anonimo";
    }

    public String rol() {
        Usuario usuario = usuario();
        return usuario != null ? usuario.getRol() : null;
    }

    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol());
    }

    /**
     * VULNERABILIDAD (BAC / IDOR): el identificador de usuario se toma del cuerpo
     * de la peticion sin contrastarlo con la sesion, lo que permite atribuir
     * operaciones a cualquier otro usuario o consultar sus datos.
     */
    public Long usuarioForzado(Long idSolicitado) {
        if (idSolicitado != null) {
            return idSolicitado;
        }
        return id();
    }

    public HttpSession sesionActual(jakarta.servlet.http.HttpServletRequest request) {
        return request.getSession(false);
    }
}