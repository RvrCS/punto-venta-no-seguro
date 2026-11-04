package com.curso.pv.reporte;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
// VULNERABILIDAD (BAC): los reportes son accesibles sin autenticacion
public class ReporteControlador {

    private final ReporteServicio servicio;

    public ReporteControlador(ReporteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return servicio.resumen();
    }

    // VULNERABILIDAD (SQLi): los filtros se concatenan en la consulta nativa
    @GetMapping("/ventas")
    public List<Map<String, Object>> ventas(@RequestParam(defaultValue = "2000-01-01") String desde,
                                            @RequestParam(defaultValue = "2099-12-31") String hasta,
                                            @RequestParam(defaultValue = "COMPLETADA") String estado) {
        return servicio.ventasPorPeriodo(desde, hasta, estado);
    }

    // VULNERABILIDAD: se devuelve HTML sin escapar -> XSS almacenado/reflejado
    @GetMapping(value = "/exportar/html", produces = MediaType.TEXT_HTML_VALUE)
    public String exportarHtml(@RequestParam(required = false) String nombreCliente,
                               @RequestParam(defaultValue = "COMPLETADA") String estado) {
        return servicio.exportarHtml(nombreCliente, estado);
    }

    @GetMapping(value = "/exportar/csv", produces = "text/csv")
    public String exportarCsv(@RequestParam(defaultValue = "COMPLETADA") String estado) {
        return servicio.exportarCsv(estado);
    }

    // VULNERABILIDAD: expone la estructura de la base de datos
    @GetMapping("/base-datos")
    public List<Map<String, Object>> estructura() {
        return servicio.estructuraBaseDatos();
    }

    @GetMapping("/vendedor/{usuarioId}")
    public Map<String, Object> porVendedor(@PathVariable Long usuarioId) {
        return servicio.ventasPorUsuario(usuarioId);
    }
}