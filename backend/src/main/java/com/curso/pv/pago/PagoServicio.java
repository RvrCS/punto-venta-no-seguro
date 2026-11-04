package com.curso.pv.pago;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.security.UsuarioActual;
import com.curso.pv.venta.VentaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PagoServicio {

    private static final Logger log = LoggerFactory.getLogger(PagoServicio.class);

    // VULNERABILIDAD: credenciales de la pasarela escritas directamente en el codigo
    private final String apiKey = "sk_live_9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c";
    private final String apiSecret = "s3cr3t-pasarela-que-nunca-deberia-estar-aqui";

    private final PagoRepositorio pagos;
    private final PagoConsultasInseguras consultas;
    private final VentaRepositorio ventas;
    private final ServicioAuditoria auditoria;
    private final UsuarioActual usuarioActual;

    public PagoServicio(PagoRepositorio pagos,
                        PagoConsultasInseguras consultas,
                        VentaRepositorio ventas,
                        ServicioAuditoria auditoria,
                        UsuarioActual usuarioActual) {
        this.pagos = pagos;
        this.consultas = consultas;
        this.ventas = ventas;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    /**
     * VULNERABILIDAD: la pasarela es simulada, pero el backend guarda el PAN y el
     * CVV completos sin cifrar y devuelve la respuesta con toda la informacion.
     * Tampoco hay validacion de tarjeta ni verificacion de firma de la peticion.
     */
    @Transactional
    public Map<String, Object> procesar(Map<String, Object> datos) {
        Map<String, Object> respuesta = new HashMap<>();

        Long ventaId = Long.valueOf(String.valueOf(datos.get("ventaId")));
        String metodo = (String) datos.getOrDefault("metodo", "TARJETA");
        String titular = (String) datos.get("titular");
        String numeroTarjeta = (String) datos.get("numeroTarjeta");
        String cvv = (String) datos.get("cvv");
        String vencimiento = (String) datos.get("fechaVencimiento");

        // VULNERABILIDAD: datos de tarjeta escritos en el log del contenedor
        log.info("Procesando pago con la pasarela apiKey={} apiSecret={} tarjeta={} cvv={} titular={}",
                apiKey, apiSecret, numeroTarjeta, cvv, titular);

        // VULNERABILIDAD: no se valida que la venta exista ni que el monto coincida
        BigDecimal monto = datos.get("monto") != null
                ? new BigDecimal(String.valueOf(datos.get("monto")))
                : BigDecimal.ZERO;

        Pago pago = new Pago();
        pago.setVentaId(ventaId);
        pago.setMetodo(metodo);
        pago.setTitular(titular);
        pago.setNumeroTarjeta(numeroTarjeta);
        pago.setCvv(cvv);
        pago.setFechaVencimiento(vencimiento);
        pago.setMonto(monto);
        pago.setEstado("APROBADO");
        pago.setReferencia("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Pago guardado = pagos.save(pago);

        // VULNERABILIDAD: la auditoria almacena el payload con los datos de la tarjeta
        auditoria.registrar(usuarioActual.id(), usuarioActual.username(), "PAGO", "VENTA",
                ventaId, datos);

        respuesta.put("estado", guardado.getEstado());
        respuesta.put("referencia", guardado.getReferencia());
        respuesta.put("monto", guardado.getMonto());
        // VULNERABILIDAD: se devuelve el numero completo de la tarjeta al cliente
        respuesta.put("numeroTarjeta", guardado.getNumeroTarjeta());
        respuesta.put("cvv", guardado.getCvv());
        respuesta.put("apiKeyUtilizada", apiKey);
        return respuesta;
    }

    // VULNERABILIDAD: expone todos los pagos con PAN y CVV a cualquier solicitante
    @Transactional(readOnly = true)
    public List<Pago> listar() {
        return pagos.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pago> porVenta(Long ventaId) {
        return pagos.findByVentaId(ventaId);
    }

    // VULNERABILIDAD: permite enumerar las tarjetas guardadas en la base
    @Transactional(readOnly = true)
    public List<?> porTarjeta(String numero) {
        return consultas.porTarjeta(numero);
    }

    // VULNERABILIDAD: expone la configuracion de credenciales de la pasarela
    public Map<String, Object> configuracionPasarela() {
        Map<String, Object> configuracion = new HashMap<>();
        configuracion.put("apiKey", apiKey);
        configuracion.put("apiSecret", apiSecret);
        configuracion.put("endpoint", "https://api.pasarela-simulada.local/v1/cobros");
        configuracion.put("modo", "produccion");
        return configuracion;
    }
}