package com.curso.pv.usuario;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
// VULNERABILIDAD (BAC): el controlador no exige rol ADMIN, cualquier usuario
// autenticado (o incluso anonimo) puede administrar usuarios
public class UsuarioControlador {

    private final UsuarioServicio servicio;

    public UsuarioControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Usuario> listar() {
        return servicio.listar();
    }

    @GetMapping("/buscar")
    public List<?> buscar(@RequestParam String texto) {
        // VULNERABILIDAD (SQLi): el texto se concatena en la consulta
        return servicio.buscar(texto);
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        // VULNERABILIDAD: cualquier id es valido, no se comprueba pertenencia
        return servicio.obtener(id);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return servicio.crear(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return servicio.actualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    @PostMapping("/{id}/password")
    public void cambiarPassword(@PathVariable Long id, @RequestParam String password) {
        // VULNERABILIDAD: la contrasena viaja por la URL y queda en los access logs
        servicio.cambiarPassword(id, password);
    }
}