package com.curso.pv.auth;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioConsultasInseguras;
import com.curso.pv.usuario.UsuarioRepositorio;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServicio {

    private final UsuarioRepositorio usuarios;
    private final UsuarioConsultasInseguras consultas;
    private final ServicioAuditoria auditoria;
    private final PasswordEncoder passwordEncoder;

    public AuthServicio(UsuarioRepositorio usuarios,
                        UsuarioConsultasInseguras consultas,
                        ServicioAuditoria auditoria,
                        PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.auditoria = auditoria;
        this.passwordEncoder = passwordEncoder;
    }

    // VULNERABILIDAD: cualquiera puede registrarse y elegir su propio rol, incluso ADMIN
    @Transactional
    public Map<String, Object> registrar(Map<String, String> datos, HttpServletRequest request) {
        Map<String, Object> respuesta = new HashMap<>();

        String username = datos.get("username");
        String password = datos.get("password");
        String rol = datos.getOrDefault("rol", "VENDEDOR");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            respuesta.put("error", "Faltan datos de registro");
            return respuesta;
        }

        // VULNERABILIDAD (SQLi): comprobacion de duplicados concatenando el valor
        Usuario existente = consultas.porUsername(username);
        if (existente != null) {
            respuesta.put("error", "El usuario ya existe");
            return respuesta;
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        // CORRECCION CRY-03: se hashea antes de guardar
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(datos.getOrDefault("nombre", username));
        usuario.setEmail(datos.get("email"));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario guardado = usuarios.save(usuario);

        auditoria.registrar(guardado.getId(), guardado.getUsername(), "REGISTRO", "USUARIO",
                guardado.getId(), datos, request);

        respuesta.put("mensaje", "Usuario registrado");
        respuesta.put("usuario", guardado);
        return respuesta;
    }
}