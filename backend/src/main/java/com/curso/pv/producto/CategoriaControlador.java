package com.curso.pv.producto;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaControlador {

    private final ProductoServicio servicio;

    public CategoriaControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Categoria> listar() {
        return servicio.listarCategorias();
    }

    @GetMapping("/buscar")
    public List<?> buscar(@RequestParam String texto) {
        // VULNERABILIDAD (SQLi)
        return servicio.buscarCategorias(texto);
    }

    @GetMapping("/{id}")
    public Categoria obtener(@PathVariable Long id) {
        return servicio.listarCategorias().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria) {
        return servicio.crearCategoria(categoria);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        // VULNERABILIDAD (BAC): no se valida rol ni existencia
    }
}