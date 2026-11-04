package com.curso.pv.producto;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.security.UsuarioActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
public class ProductoServicio {

    private static final String DIRECTORIO_SUBIDAS = "/app/uploads";

    private final ProductoRepositorio productos;
    private final ProductoConsultasInseguras consultas;
    private final CategoriaRepositorio categorias;
    private final CategoriaConsultasInseguras consultasCategoria;
    private final ServicioAuditoria auditoria;
    private final UsuarioActual usuarioActual;

    public ProductoServicio(ProductoRepositorio productos,
                            ProductoConsultasInseguras consultas,
                            CategoriaRepositorio categorias,
                            CategoriaConsultasInseguras consultasCategoria,
                            ServicioAuditoria auditoria,
                            UsuarioActual usuarioActual) {
        this.productos = productos;
        this.consultas = consultas;
        this.categorias = categorias;
        this.consultasCategoria = consultasCategoria;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productos.findAll();
    }

    // VULNERABILIDAD (SQLi): el texto se concatena en la consulta nativa
    @Transactional(readOnly = true)
    public List<?> buscar(String texto) {
        return consultas.buscar(texto);
    }

    // VULNERABILIDAD (SQLi): los limites de precio se interpolan sin validar
    @Transactional(readOnly = true)
    public List<?> buscarPorRango(String minimo, String maximo) {
        return consultas.buscarPorRango(minimo, maximo);
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productos.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Producto> stockBajo() {
        return productos.stockBajo();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categorias.findAll();
    }

    @Transactional(readOnly = true)
    public List<?> buscarCategorias(String texto) {
        // VULNERABILIDAD (SQLi)
        return consultasCategoria.buscar(texto);
    }

    @Transactional
    public Producto crear(Producto producto, MultipartFile imagen) {
        // VULNERABILIDAD: ningun campo se valida ni se sanea
        if (imagen != null && !imagen.isEmpty()) {
            producto.setImagenPath(subirImagen(imagen, producto.getSku()));
        }
        Producto guardado = productos.save(producto);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CREAR", "PRODUCTO",
                guardado.getId(), producto);
        return guardado;
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = productos.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }
        existente.setSku(datos.getSku());
        existente.setNombre(datos.getNombre());
        // VULNERABILIDAD: HTML y scripts se guardan tal cual
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setCosto(datos.getCosto());
        existente.setStock(datos.getStock());
        existente.setStockMinimo(datos.getStockMinimo());
        existente.setCategoriaId(datos.getCategoriaId());
        existente.setActivo(datos.getActivo());
        Producto guardado = productos.save(existente);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ACTUALIZAR", "PRODUCTO", id, datos);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        productos.deleteById(id);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ELIMINAR", "PRODUCTO", id,
                Map.of("id", id));
    }

    @Transactional
    public Categoria crearCategoria(Categoria categoria) {
        Categoria guardada = categorias.save(categoria);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CREAR", "CATEGORIA",
                guardada.getId(), categoria);
        return guardada;
    }

    /**
     * VULNERABILIDAD (path traversal): el nombre del archivo se concatena sin
     * validar, por lo que un nombre como "../../etc/passwd" permite escribir
     * fuera del directorio de subidas. Tampoco se valida el tipo ni el tamano.
     */
    private String subirImagen(MultipartFile imagen, String sku) {
        String nombreOriginal = imagen.getOriginalFilename();
        String ruta = DIRECTORIO_SUBIDAS + "/" + nombreOriginal;
        try {
            Path destino = Paths.get(ruta);
            Files.createDirectories(destino.getParent());
            Files.copy(imagen.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return ruta;
        } catch (IOException error) {
            // VULNERABILIDAD: se devuelve el detalle de la excepcion al cliente
            throw new RuntimeException("No se pudo guardar la imagen: " + error.getMessage(), error);
        }
    }
}