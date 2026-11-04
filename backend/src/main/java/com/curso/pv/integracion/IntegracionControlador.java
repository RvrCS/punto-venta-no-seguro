package com.curso.pv.integracion;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/integraciones")
public class IntegracionControlador {

    private final IntegracionServicio servicio;

    public IntegracionControlador(IntegracionServicio servicio) {
        this.servicio = servicio;
    }

    // VULNERABILIDAD (SSRF): el usuario decide a que URL accede el servidor
    @PostMapping("/consultar-url")
    public Map<String, Object> consultar(@RequestParam String url) {
        return servicio.consultarUrl(url);
    }

    @PostMapping("/enviar")
    public Map<String, Object> enviar(@RequestParam String url, @RequestBody Map<String, Object> cuerpo) {
        return servicio.enviarPost(url, cuerpo);
    }

    // VULNERABILIDAD: escaneo de puertos internos
    @GetMapping("/verificar-host")
    public Map<String, Object> verificar(@RequestParam String host, @RequestParam(defaultValue = "80") Integer puerto) {
        return servicio.verificarHost(host, puerto);
    }
}