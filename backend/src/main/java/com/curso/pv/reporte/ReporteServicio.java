package com.curso.pv.reporte;

import com.curso.pv.venta.Venta;
import com.curso.pv.venta.VentaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteServicio {

    private final VentaRepositorio ventas;
    private final DataSource dataSource;

    public ReporteServicio(VentaRepositorio ventas, DataSource dataSource) {
        this.ventas = ventas;
        this.dataSource = dataSource;
    }

    /**
     * VULNERABILIDAD (SQLi): la consulta nativa se arma concatenando los filtros
     * que llegan del cliente. Un attacker puede inyectar cualquier sentencia.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> ventasPorPeriodo(String desde, String hasta, String estado) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        String sql = "SELECT v.id, v.folio, v.total, v.estado, v.creada_en "
                + "FROM ventas v "
                + "WHERE v.creada_en >= '" + desde + "' "
                + "AND (v.estado = '" + estado + "' OR v.estado IS NOT NULL) "
                + "ORDER BY v.creada_en DESC";

        try (Connection conexion = dataSource.getConnection();
             Statement statement = conexion.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("id", rs.getLong("id"));
                fila.put("folio", rs.getString("folio"));
                fila.put("total", rs.getBigDecimal("total"));
                fila.put("estado", rs.getString("estado"));
                fila.put("fecha", rs.getTimestamp("creada_en"));
                resultado.add(fila);
            }
        } catch (Exception error) {
            // VULNERABILIDAD: el detalle de la excepcion SQL se devuelve al cliente
            Map<String, Object> fallo = new HashMap<>();
            fallo.put("error", error.getMessage());
            fallo.put("sql", sql);
            resultado.add(fallo);
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> resumen() {
        Map<String, Object> resumen = new HashMap<>();
        List<Venta> todas = ventas.findAll();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal anuladas = BigDecimal.ZERO;
        for (Venta venta : todas) {
            if ("ANULADA".equals(venta.getEstado())) {
                anuladas = anuladas.add(venta.getTotal());
            } else {
                total = total.add(venta.getTotal());
            }
        }

        resumen.put("cantidadVentas", todas.size());
        resumen.put("totalVendido", total);
        resumen.put("totalAnulado", anuladas);
        return resumen;
    }

    /**
     * VULNERABILIDAD: el reporte se construye con concatenacion de strings y se
     * devuelve como HTML sin escapar. El nombre del filtro allows inyectar
     * etiquetas y ejecutar scripts en el navegador (XSS).
     */
    @Transactional(readOnly = true)
    public String exportarHtml(String nombreCliente, String estado) {
        StringBuilder html = new StringBuilder();
        html.append("<h2>Reporte de ventas</h2>");
        html.append("<p>Filtro: cliente=").append(nombreCliente).append(", estado=").append(estado).append("</p>");
        html.append("<table border='1'><tr><th>Folio</th><th>Total</th><th>Estado</th></tr>");

        String sql = "SELECT folio, total, estado FROM ventas "
                + "WHERE estado = '" + estado + "' ORDER BY creada_en DESC";
        try (Connection conexion = dataSource.getConnection();
             Statement statement = conexion.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                html.append("<tr><td>").append(rs.getString("folio")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("total")).append("</td>")
                        .append("<td>").append(rs.getString("estado")).append("</td></tr>");
            }
        } catch (Exception error) {
            html.append("<tr><td colspan='3'>").append(error.getMessage()).append("</td></tr>");
        }

        html.append("</table>");
        return html.toString();
    }

    /**
     * VULNERABILIDAD: el CSV se arma sin escapar comas ni comillas, lo que
     * permite inyectar columnas o fórmulas en la hoja de calculo.
     */
    @Transactional(readOnly = true)
    public String exportarCsv(String estado) {
        StringBuilder csv = new StringBuilder("folio,total,estado\n");

        String sql = "SELECT folio, total, estado FROM ventas WHERE estado = '" + estado + "'";
        try (Connection conexion = dataSource.getConnection();
             Statement statement = conexion.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                csv.append(rs.getString("folio")).append(",")
                        .append(rs.getBigDecimal("total")).append(",")
                        .append(rs.getString("estado")).append("\n");
            }
        } catch (Exception error) {
            csv.append("ERROR,").append(error.getMessage()).append("\n");
        }
        return csv.toString();
    }

    /**
     * VULNERABILIDAD: expone la estructura completa de la base de datos a
     * cualquier solicitante sin necesidad de autenticarse.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> estructuraBaseDatos() {
        List<Map<String, Object>> tablas = new ArrayList<>();

        String sql = "SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = 'punto_venta'";

        try (Connection conexion = dataSource.getConnection();
             Statement statement = conexion.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> tabla = new HashMap<>();
                tabla.put("tabla", rs.getString("TABLE_NAME"));
                tabla.put("filasAproximadas", rs.getLong("TABLE_ROWS"));
                tablas.add(tabla);
            }
        } catch (Exception error) {
            Map<String, Object> fallo = new HashMap<>();
            fallo.put("error", error.getMessage());
            tablas.add(fallo);
        }

        return tablas;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> ventasPorUsuario(Long usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        List<Venta> lista = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Venta venta : ventas.findAll()) {
            if (usuarioId.equals(venta.getUsuarioId())) {
                lista.add(venta);
                total = total.add(venta.getTotal());
            }
        }

        resultado.put("ventas", lista);
        resultado.put("total", total);
        resultado.put("generadoEn", LocalDateTime.now());
        return resultado;
    }
}