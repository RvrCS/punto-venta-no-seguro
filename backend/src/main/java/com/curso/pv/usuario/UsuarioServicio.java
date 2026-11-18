package com.curso.pv.usuario;

import com.curso.pv.security.UsuarioActual;
import com.curso.pv.log.ServicioAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio repositorio;
    private final UsuarioConsultasInseguras consultas;
    private final ServicioAuditoria auditoria;
    private final UsuarioActual usuarioActual;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio repositorio,
                           UsuarioConsultasInseguras consultas,
                           ServicioAuditoria auditoria,
                           UsuarioActual usuarioActual,
                           PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.consultas = consultas;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<?> buscar(String texto) {
        // VULNERABILIDAD (SQLi)
        return consultas.buscar(texto);
    }

    @Transactional(readOnly = true)
    public List<?> porRol(String rol) {
        return consultas.porRol(rol);
    }

    // VULNERABILIDAD: devuelve la entidad completa, incluida la contrasena en claro
    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        // CORRECCION CRY-03: se hashea antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario guardado = repositorio.save(usuario);
        // VULNERABILIDAD: la auditoria guarda la contrasena en claro
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CREAR", "USUARIO",
                guardado.getId(), usuario);
        return guardado;
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario existente = repositorio.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }
        existente.setUsername(datos.getUsername());
        // CORRECCION CRY-03: solo se toca el password si mandan uno nuevo; si no,
        // se conserva el hash existente (antes se sobreescribia siempre, incluso con null)
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }

        existente.setNombre(datos.getNombre());
        existente.setEmail(datos.getEmail());
        // VULNERABILIDAD (BAC): cualquiera puede cambiar el rol de otro usuario
        // sin comprobar que sea administrador ni que el id le pertenezca
        existente.setRol(datos.getRol());
        existente.setActivo(datos.getActivo());
        Usuario guardado = repositorio.save(existente);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ACTUALIZAR", "USUARIO", id, datos);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        // VULNERABILIDAD: no se valida que exista ni que el usuario tenga permisos
        repositorio.deleteById(id);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ELIMINAR", "USUARIO", id, Map.of("id", id));
    }

    @Transactional
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = repositorio.findById(id).orElse(null);
        if (usuario == null) {
            return;
        }
        // CORRECCION CRY-03: se hashea la nueva contrasena
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        repositorio.save(usuario);

        // VULNERABILIDAD: la nueva contrasena queda escrita en el log y en la tabla de auditoria
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", id);
        detalle.put("password", nuevaPassword);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CAMBIAR_PASSWORD", "USUARIO", id, detalle);
    }
}