package com.curso.pv.pago;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
// VULNERABILIDAD (BAC): sin control de acceso, cualquiera consulta o ejecuta pagos
public class PagoControlador {

    private final PagoServicio servicio;

    public PagoControlador(PagoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/procesar")
    public Map<String, Object> procesar(@RequestBody Map<String, Object> datos) {
        return servicio.procesar(datos);
    }

    // VULNERABILIDAD: devuelve PAN y CVV de todas las tarjetas
    @GetMapping
    public List<Pago> listar() {
        return servicio.listar();
    }

    @GetMapping("/venta/{ventaId}")
    public List<Pago> porVenta(@PathVariable Long ventaId) {
        return servicio.porVenta(ventaId);
    }

    // VULNERABILIDAD: permite enumerar tarjetas por numero completo
    @GetMapping("/tarjeta")
    public List<?> porTarjeta(@RequestParam String numero) {
        return servicio.porTarjeta(numero);
    }

    // VULNERABILIDAD: la credencial de la pasarela se "cachea" en una cookie
    // HttpOnly ("protegida"). No se puede leer con document.cookie desde la pantalla,
    // pero viaja en claro en la cabecera Set-Cookie: hay que interceptar la respuesta
    // con un proxy (Burp Suite / ZAP) para capturarla. El cuerpo ya no la devuelve.
    @GetMapping("/configuracion")
    public Map<String, Object> configuracion(HttpServletResponse respuesta) {
        Map<String, Object> cfg = servicio.configuracionPasarela();

        String apiKey = String.valueOf(cfg.remove("apiKey"));
        // Cookie "protegida": HttpOnly (invisible a JS), sin Secure (viaja en claro).
        respuesta.addHeader("Set-Cookie",
                "pv_gw=" + apiKey + "; Path=/; HttpOnly; SameSite=Lax");

        return cfg;
    }
}