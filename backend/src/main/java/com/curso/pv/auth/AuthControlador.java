package com.curso.pv.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private static final Logger log = LoggerFactory.getLogger(AuthControlador.class);

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextoRepositorio;
    private final AuthServicio servicio;

    public AuthControlador(AuthenticationManager authenticationManager,
                           HttpSessionSecurityContextRepository contextoRepositorio,
                           AuthServicio servicio) {
        this.authenticationManager = authenticationManager;
        this.contextoRepositorio = contextoRepositorio;
        this.servicio = servicio;
    }

    // Autenticacion por cookie de sesion
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales, HttpServletRequest request) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        // VULNERABILIDAD: se escribe la contrasena en el log en texto plano
        log.info("Intento de login username={} password={}", username, password);

        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        contextoRepositorio.saveContext(contexto, request, null);

        // VULNERABILIDAD: se responde con el objeto Authentication completo,
        // que incluye las credenciales y todos los roles del usuario
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("mensaje", "Sesion iniciada");
        cuerpo.put("username", autenticacion.getName());
        cuerpo.put("principal", autenticacion.getPrincipal());
        cuerpo.put("credenciales", autenticacion.getCredentials());
        cuerpo.put("autoridades", autenticacion.getAuthorities());
        cuerpo.put("sessionId", request.getSession(false) != null ? request.getSession(false).getId() : null);
        return ResponseEntity.ok(cuerpo);
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> datos, HttpServletRequest request) {
        return ResponseEntity.ok(servicio.registrar(datos, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // VULNERABILIDAD: no se invalida la sesion en el servidor,
        // la cookie sigue siendo valida despues del cierre de sesion
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.removeAttribute("carrito");
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("mensaje", "Sesion cerrada"));
    }

    @GetMapping("/yo")
    public ResponseEntity<?> yo() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No hay sesion activa"));
        }
        return ResponseEntity.ok(Map.of(
                "username", autenticacion.getName(),
                "autoridades", autenticacion.getAuthorities()
        ));
    }

    // VULNERABILIDAD: expone la informacion de la sesion completa al cliente
    @GetMapping("/sesion")
    public ResponseEntity<?> infoSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", sesion != null ? sesion.getId() : null);
        info.put("creationTime", sesion != null ? sesion.getCreationTime() : null);
        info.put("maxInactiveInterval", sesion != null ? sesion.getMaxInactiveInterval() : null);
        info.put("atributos", sesion != null ? sesion.getAttributeNames().toString() : null);
        info.put("servidor", sesion != null ? sesion.getServletContext().getServerInfo() : null);
        return ResponseEntity.ok(info);
    }

    // VULNERABILIDAD: permite fijar la sesion a un identificador elegido por el atacante
    @PostMapping("/fijar-sesion")
    public ResponseEntity<?> fijarSesion(@RequestParam String sessionId, HttpServletRequest request) {
        HttpSession nueva = request.getSession(true);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("sessionIdActual", nueva.getId());
        respuesta.put("sessionIdSolicitado", sessionId);
        return ResponseEntity.ok(respuesta);
    }
}