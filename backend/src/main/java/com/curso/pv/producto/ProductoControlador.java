package com.curso.pv.producto;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Producto> listar() {
        return servicio.listar();
    }

    // VULNERABILIDAD (SQLi + XSS reflejado): el texto de busqueda se usa tal cual
    @GetMapping("/buscar")
    public List<?> buscar(@RequestParam String texto) {
        return servicio.buscar(texto);
    }

    @GetMapping("/rango")
    public List<?> porRango(@RequestParam String minimo, @RequestParam String maximo) {
        return servicio.buscarPorRango(minimo, maximo);
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return servicio.stockBajo();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto crear(@RequestPart("producto") Producto producto,
                          @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return servicio.crear(producto, imagen);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return servicio.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}