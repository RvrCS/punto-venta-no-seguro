package com.curso.pv.recurso;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class SaludController {

    @GetMapping("/api/salud")
    public Map<String, Object> salud() {
        return Map.of(
                "estado", "ok",
                "servicio", "punto-venta-backend",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}