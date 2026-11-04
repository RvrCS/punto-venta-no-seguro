package com.curso.pv.cliente;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.security.UsuarioActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ClienteServicio {

    private final ClienteRepositorio repositorio;
    private final ClienteConsultasInseguras consultas;
    private final ServicioAuditoria auditoria;
    private final UsuarioActual usuarioActual;

    public ClienteServicio(ClienteRepositorio repositorio,
                           ClienteConsultasInseguras consultas,
                           ServicioAuditoria auditoria,
                           UsuarioActual usuarioActual) {
        this.repositorio = repositorio;
        this.consultas = consultas;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repositorio.findAll();
    }

    // VULNERABILIDAD (SQLi): el texto se concatena en la consulta nativa
    @Transactional(readOnly = true)
    public List<?> buscar(String texto) {
        return consultas.buscar(texto);
    }

    @Transactional(readOnly = true)
    public Cliente obtener(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Transactional
    public Cliente crear(Cliente cliente) {
        // VULNERABILIDAD: no hay validacion de formato de email, documento ni telefono
        Cliente guardado = repositorio.save(cliente);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CREAR", "CLIENTE",
                guardado.getId(), cliente);
        return guardado;
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = repositorio.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }
        existente.setDocumento(datos.getDocumento());
        existente.setNombre(datos.getNombre());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setDireccion(datos.getDireccion());
        existente.setActivo(datos.getActivo());
        Cliente guardado = repositorio.save(existente);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ACTUALIZAR", "CLIENTE", id, datos);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        repositorio.deleteById(id);
        auditoria.registrar(usuarioActual.id(), usernameActual(), "ELIMINAR", "CLIENTE", id,
                Map.of("id", id));
    }

    private String usernameActual() {
        return usuarioActual.username();
    }
}