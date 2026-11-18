# Checklist de seguridad · Unidad 2 — aplicada al POS

Leyenda: ✅ cumple · ❌ no cumple · 🔧 corregido en este avance · ⏭️ U3 / fuera de alcance

> Plantilla base: marcar el estado tras verificar cada control y anotar el hallazgo del reporte.

## 1. Broken Access Control

| #   | Control                                                             | Estado | Hallazgo | OWASP | STRIDE |
|-----|---------------------------------------------------------------------|--------|----------|-------|--------|
| 1.1 | Toda ruta exige autenticación salvo las públicas explícitas         |        |          |       |        |
| 1.2 | Deny by default                                                     |        |          |       |        |
| 1.3 | Funciones administrativas restringidas por rol en el backend (BFLA) |        |          |       |        |
| 1.4 | El cliente no puede asignarse rol ni campos privilegiados           |        |          |       |        |
| 1.5 | Cada acceso por id verifica la propiedad del objeto (BOLA/IDOR)     |        |          |       |        |
| 1.6 | La identidad se toma de la sesión, no del body/URL                  |        |          |       |        |
| 1.7 | El control del frontend no es la única barrera                      |        |          |       |        |
| 1.8 | CORS restringido a orígenes conocidos                               |        |          |       |        |
| 1.9 | Protección CSRF activa                                              |        |          |       |        |

## 2. XSS y CSP

| #   | Control                                              | Estado | Hallazgo | OWASP | STRIDE |
|-----|------------------------------------------------------|--------|----------|-------|--------|
| 2.1 | Sin `dangerouslySetInnerHTML` con datos del usuario  |        |          |       |        |
| 2.2 | HTML generado en el servidor codificado en la salida |        |          |       |        |
| 2.3 | Validación de entrada en el backend                  |        |          |       |        |
| 2.4 | `Content-Security-Policy` configurada                |        |          |       |        |
| 2.5 | `X-Content-Type-Options`, `frame-ancestors`          |        |          |       |        |

## 3. SSRF

| #   | Control                                        | Estado | Hallazgo | OWASP | STRIDE |
|-----|------------------------------------------------|--------|----------|-------|--------|
| 3.1 | Lista blanca de hosts/esquemas                 |        |          |       |        |
| 3.2 | Bloqueo de IPs privadas, loopback y link-local |        |          |       |        |
| 3.3 | Sin seguir redirecciones                       |        |          |       |        |
| 3.4 | No se devuelve el contenido/error crudo        |        |          |       |        |
| 3.5 | Timeouts en el cliente HTTP                    |        |          |       |        |

## 4. Exposición de datos sensibles

| #   | Control                                                      | Estado | Hallazgo | OWASP | STRIDE |
|-----|--------------------------------------------------------------|--------|----------|-------|--------|
| 4.1 | La API devuelve DTOs, no entidades completas                 |        |          |       |        |
| 4.2 | Contraseñas nunca salen de la API                            |        |          |       |        |
| 4.3 | PAN enmascarado, CVV nunca devuelto                          |        |          |       |        |
| 4.4 | Sin endpoints de configuración/diagnóstico públicos          |        |          |       |        |
| 4.5 | Actuator restringido                                         |        |          |       |        |
| 4.6 | Errores sin stack trace                                      |        |          |       |        |
| 4.7 | Nada sensible en localStorage / IndexedDB / cookies legibles |        |          |       |        |

## 5. Criptografía y cifrado en reposo

| #   | Control                                        | Estado | Hallazgo | OWASP | STRIDE |
|-----|------------------------------------------------|--------|----------|-------|--------|
| 5.1 | Contraseñas con hash adaptativo y sal          |        |          |       |        |
| 5.2 | CVV nunca almacenado                           |        |          |       |        |
| 5.3 | PAN cifrado o tokenizado                       |        |          |       |        |
| 5.4 | Sin secretos en el repositorio                 |        |          |       |        |
| 5.5 | Codificación (base64) no usada como protección |        |          |       |        |
| 5.6 | TLS hacia la BD y HTTPS                        |        |          |       |        |
| 5.1 | Contraseñas con hash adaptativo y sal          | 🔧     | CRY-03   | A02   | Information disclosure, Spoofing |
## 6. Gestión de sesiones

| #   | Control                                    | Estado | Hallazgo | OWASP | STRIDE |
|-----|--------------------------------------------|--------|----------|-------|--------|
| 6.1 | Cookie `HttpOnly`                          |        |          |       |        |
| 6.2 | Cookie `Secure`                            |        |          |       |        |
| 6.3 | `SameSite` adecuado                        |        |          |       |        |
| 6.4 | Expiración corta y no persistente          |        |          |       |        |
| 6.5 | Nuevo id de sesión tras el login           |        |          |       |        |
| 6.6 | Logout invalida la sesión                  |        |          |       |        |
| 6.7 | El id de sesión no se expone en cuerpo/URL |        |          |       |        |
| 6.8 | Sin HTTP Basic                             |        |          |       |        |

