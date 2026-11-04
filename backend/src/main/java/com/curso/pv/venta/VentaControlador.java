package com.curso.pv.venta;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaControlador {

    private final VentaServicio servicio;

    public VentaControlador(VentaServicio servicio) {
        this.servicio = servicio;
    }

    // ---------------- Carrito ----------------

    @GetMapping("/carrito")
    public List<Map<String, Object>> verCarrito(HttpServletRequest request) {
        return servicio.verCarrito(request);
    }

    // VULNERABILIDAD: el precio unitario lo envia el cliente
    @PostMapping("/carrito")
    public List<Map<String, Object>> agregar(@RequestParam Long productoId,
                                             @RequestParam Integer cantidad,
                                             @RequestParam BigDecimal precio,
                                             HttpServletRequest request) {
        return servicio.agregarAlCarrito(productoId, cantidad, precio, request);
    }

    @DeleteMapping("/carrito/{indice}")
    public List<Map<String, Object>> quitar(@PathVariable int indice, HttpServletRequest request) {
        servicio.quitarDelCarrito(indice, request);
        return servicio.verCarrito(request);
    }

    @DeleteMapping("/carrito")
    public void vaciar(HttpServletRequest request) {
        servicio.vaciarCarrito(request);
    }

    @PutMapping("/carrito/{indice}")
    public ResponseEntity<List<Map<String, Object>>> actualizarCantidad(
            @PathVariable int indice,
            @RequestParam Integer cantidad,
            HttpServletRequest request) {
        List<Map<String, Object>> carrito = servicio.actualizarCantidad(indice, cantidad, request);
        return ResponseEntity.ok(carrito);
    }

    // ---------------- Checkout ----------------

    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody Map<String, Object> datos, HttpServletRequest request) {
        return servicio.checkout(datos, request);
    }

    // ---------------- Consulta ----------------

    @GetMapping
    public List<Venta> listar() {
        return servicio.listar();
    }

    // VULNERABILIDAD (SQLi): estado y fecha se interpolan en la consulta
    @GetMapping("/buscar")
    public List<?> buscar(@RequestParam(required = false) String estado,
                              @RequestParam(required = false) String desde) {
        return servicio.buscar(estado, desde);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Venta> porCliente(@PathVariable Long clienteId) {
        return servicio.porCliente(clienteId);
    }

    // VULNERABILIDAD (BAC): no se comprueba que la venta pertenezca al usuario
    @GetMapping("/{id}")
    public Venta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/items")
    public List<VentaItem> items(@PathVariable Long id) {
        return servicio.itemsDe(id);
    }

    // VULNERABILIDAD (BAC): cualquiera puede anular una venta
    @PostMapping("/{id}/anular")
    public Venta anular(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        return servicio.anular(id, motivo);
    }

    // ---------------- Inventario ----------------

    @GetMapping("/inventario/producto/{productoId}")
    public List<MovimientoInventario> movimientos(@PathVariable Long productoId) {
        return servicio.movimientosDe(productoId);
    }

    @GetMapping("/inventario/movimientos")
    public List<?> buscarMovimientos(@RequestParam(required = false) String tipo,
                                                        @RequestParam(required = false) String desde) {
        return servicio.buscarMovimientos(tipo, desde);
    }
}