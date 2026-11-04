package com.curso.pv.venta;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.producto.Producto;
import com.curso.pv.producto.ProductoRepositorio;
import com.curso.pv.security.UsuarioActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VentaServicio {

    private static final BigDecimal IMPUESTO = new BigDecimal("0.19");

private final VentaRepositorio ventas;
    private final VentaConsultasInseguras consultasVenta;
    private final VentaItemRepositorio items;
    private final ProductoRepositorio productos;
    private final MovimientoInventarioRepositorio movimientos;
    private final MovimientoConsultasInseguras consultasMovimiento;
    private final ServicioAuditoria auditoria;
    private final UsuarioActual usuarioActual;

    public VentaServicio(VentaRepositorio ventas,
                         VentaConsultasInseguras consultasVenta,
                         VentaItemRepositorio items,
                         ProductoRepositorio productos,
                         MovimientoInventarioRepositorio movimientos,
                         MovimientoConsultasInseguras consultasMovimiento,
                         ServicioAuditoria auditoria,
                         UsuarioActual usuarioActual) {
        this.ventas = ventas;
        this.consultasVenta = consultasVenta;
        this.items = items;
        this.productos = productos;
        this.movimientos = movimientos;
        this.consultasMovimiento = consultasMovimiento;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    // ------------------------------------------------------------------
    // Carrito almacenado en la sesion HTTP
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> carritoDe(HttpServletRequest request) {
        HttpSession sesion = request.getSession(true);
        Object existente = sesion.getAttribute("carrito");
        if (existente instanceof List<?> lista && !lista.isEmpty()) {
            return (List<Map<String, Object>>) lista;
        }
        List<Map<String, Object>> carrito = new ArrayList<>();
        sesion.setAttribute("carrito", carrito);
        return carrito;
    }

    public List<Map<String, Object>> verCarrito(HttpServletRequest request) {
        return carritoDe(request);
    }

    public void vaciarCarrito(HttpServletRequest request) {
        request.getSession(true).setAttribute("carrito", new ArrayList<>());
    }

    /**
     * VULNERABILIDAD: al agregar al carrito se guarda el precio que envia el
     * cliente, sin contrastarlo con el precio real del producto en la base.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> agregarAlCarrito(Long productoId, Integer cantidad,
                                                       BigDecimal precioCliente, HttpServletRequest request) {
        List<Map<String, Object>> carrito = carritoDe(request);

        Map<String, Object> linea = new HashMap<>();
        linea.put("productoId", productoId);
        linea.put("cantidad", cantidad);
        linea.put("precioUnit", precioCliente);
        linea.put("subtotal", precioCliente.multiply(BigDecimal.valueOf(cantidad)));
        carrito.add(linea);

        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "AGREGAR_CARRITO", "PRODUCTO",
                productoId, linea, peticionActual());
        return carrito;
    }

    public void quitarDelCarrito(int indice, HttpServletRequest request) {
        List<Map<String, Object>> carrito = carritoDe(request);
        if (indice >= 0 && indice < carrito.size()) {
            carrito.remove(indice);
        }
    }

    public List<Map<String, Object>> actualizarCantidad(int indice, Integer cantidad, HttpServletRequest request) {
        List<Map<String, Object>> carrito = carritoDe(request);
        if (indice < 0 || indice >= carrito.size()) {
            return carrito;
        }
        if (cantidad == null || cantidad <= 0) {
            carrito.remove(indice);
            return carrito;
        }
        Map<String, Object> linea = carrito.get(indice);
        linea.put("cantidad", cantidad);
        Object precioObj = linea.get("precioUnit");
        BigDecimal precioUnit = BigDecimal.ZERO;
        if (precioObj instanceof BigDecimal) {
            precioUnit = (BigDecimal) precioObj;
        } else if (precioObj != null) {
            try {
                precioUnit = new BigDecimal(String.valueOf(precioObj));
            } catch (Exception ignored) {
                precioUnit = BigDecimal.ZERO;
            }
        }
        linea.put("subtotal", precioUnit.multiply(BigDecimal.valueOf(cantidad.longValue())));
        return carrito;
    }

    // ------------------------------------------------------------------
    // Checkout
    // ------------------------------------------------------------------

    /**
     * VULNERABILIDAD (manipulacion de precios): el total se recalcula usando el
     * precio y el descuento que llegan en el cuerpo de la peticion, por lo que
     * un cliente puede comprar productos a cualquier precio o con total 0.
     * Tampoco se valida el stock disponible.
     */
    @Transactional
    public Map<String, Object> checkout(Map<String, Object> datos, HttpServletRequest request) {
        List<Map<String, Object>> carrito = carritoDe(request);
        Map<String, Object> resultado = new HashMap<>();

        if (carrito.isEmpty()) {
            resultado.put("error", "El carrito esta vacio");
            return resultado;
        }

        Venta venta = new Venta();
        venta.setFolio(generarFolio());

        // VULNERABILIDAD (BAC): el clienteId y el usuarioId los elige el atacante
        Object clienteId = datos.get("clienteId");
        if (clienteId != null) {
            try {
                venta.setClienteId(Long.valueOf(clienteId.toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        Object usuarioId = datos.get("usuarioId");
        if (usuarioId != null) {
            try {
                venta.setUsuarioId(Long.valueOf(usuarioId.toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (venta.getUsuarioId() == null) {
            venta.setUsuarioId(usuarioActual.id());
        }
        venta.setObservaciones((String) datos.get("observaciones"));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<VentaItem> detalles = new ArrayList<>();

        for (Map<String, Object> linea : carrito) {
            Object precio = linea.get("precioUnit");
            BigDecimal precioUnit = precio instanceof BigDecimal bd
                    ? bd
                    : new BigDecimal(String.valueOf(precio));
            Object cantidad = linea.get("cantidad");
            int cant = cantidad instanceof Integer i ? i : Integer.parseInt(String.valueOf(cantidad));
            Long productoId = Long.valueOf(String.valueOf(linea.get("productoId")));

            VentaItem item = new VentaItem();
            item.setProductoId(productoId);
            item.setCantidad(cant);
            item.setPrecioUnit(precioUnit);
            item.setSubtotal(precioUnit.multiply(BigDecimal.valueOf(cant)));

            Producto producto = productos.findById(productoId).orElse(null);
            item.setDescripcion(producto != null ? producto.getNombre() : "Producto " + productoId);

            subtotal = subtotal.add(item.getSubtotal());
            detalles.add(item);
        }

        // VULNERABILIDAD: el descuento lo aplica el cliente sin limite
        Object descuentoEnviado = datos.get("descuento");
        BigDecimal descuento = descuentoEnviado != null
                ? new BigDecimal(String.valueOf(descuentoEnviado))
                : BigDecimal.ZERO;

        BigDecimal impuesto = subtotal.subtract(descuento).multiply(IMPUESTO);
        BigDecimal total = subtotal.subtract(descuento).add(impuesto);

        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setEstado((String) datos.getOrDefault("estado", "COMPLETADA"));
        venta.setCreadaEn(LocalDateTime.now());

        Venta guardada = ventas.save(venta);

        for (VentaItem item : detalles) {
            item.setVentaId(guardada.getId());
            items.save(item);
            descontarStock(item, guardada.getId());
        }

        vaciarCarrito(request);

        resultado.put("venta", guardada);
        resultado.put("items", detalles);
        // VULNERABILIDAD: se registra el payload completo del checkout en la auditoria
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "CHECKOUT", "VENTA",
                guardada.getId(), datos, peticionActual());
        return resultado;
    }

    private void descontarStock(VentaItem item, Long ventaId) {
        Producto producto = productos.findById(item.getProductoId()).orElse(null);
        if (producto == null) {
            return;
        }
        int anterior = producto.getStock();
        // VULNERABILIDAD: el stock puede quedar negativo sin ninguna validacion
        int nuevo = anterior - item.getCantidad();
        producto.setStock(nuevo);
        productos.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(item.getProductoId());
        movimiento.setTipo("SALIDA");
        movimiento.setCantidad(item.getCantidad());
        movimiento.setStockAnterior(anterior);
        movimiento.setStockNuevo(nuevo);
        movimiento.setUsuarioId(usuarioActual.id());
        movimiento.setVentaId(ventaId);
        movimiento.setMotivo("Venta automatica");
        movimientos.save(movimiento);
    }

    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventas.findAll();
    }

    // VULNERABILIDAD (SQLi + BAC): cualquiera puede consultar cualquier venta por id
    @Transactional(readOnly = true)
    public Venta obtener(Long id) {
        return ventas.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<VentaItem> itemsDe(Long ventaId) {
        return items.porVenta(ventaId);
    }

    @Transactional(readOnly = true)
    public List<?> buscar(String estado, String desde) {
        // VULNERABILIDAD: los filtros se concatenan en la consulta nativa
        return consultasVenta.buscar(estado, desde != null && !desde.isBlank() ? desde : "2000-01-01 00:00:00");
    }

    @Transactional(readOnly = true)
    public List<Venta> porCliente(Long clienteId) {
        return ventas.findByClienteId(clienteId);
    }

    // VULNERABILIDAD (BAC): cualquier usuario puede anular una venta ajena
    @Transactional
    public Venta anular(Long id, String motivo) {
        Venta venta = ventas.findById(id).orElse(null);
        if (venta == null) {
            return null;
        }
        venta.setEstado("ANULADA");
        venta.setObservaciones(motivo);
        Venta guardada = ventas.save(venta);
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "ANULAR", "VENTA", id, motivo);
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> movimientosDe(Long productoId) {
        return movimientos.findByProductoId(productoId);
    }

    @Transactional(readOnly = true)
    public List<?> buscarMovimientos(String tipo, String desde) {
        // VULNERABILIDAD: los filtros se concatenan en la consulta nativa
        return consultasMovimiento.buscar(tipo, desde != null && !desde.isBlank() ? desde : "2000-01-01 00:00:00");
    }

    private String generarFolio() {
        return "V-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private HttpServletRequest peticionActual() {
        var atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest();
        }
        return null;
    }
}