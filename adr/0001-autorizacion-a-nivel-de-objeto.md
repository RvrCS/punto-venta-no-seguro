# ADR-0001 · La API exige sesión iniciada

- **Estado:** Aceptado
- **Fecha:** 2026-11-16
- **Autor(es):** Victoria Isabel Villalba Beltrán
- **Hallazgos relacionados:** BAC-01

## Contexto

La API es pública (`SecurityConfig.java:40-43`): todas las rutas tienen `permitAll()`.

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/**").permitAll()
        .requestMatchers("/**").permitAll()
        .anyRequest().permitAll())
```

Cualquier persona que conozca la URL de la API (visible en el bundle del frontend) puede usar todos los módulos del POS sin iniciar sesión: leer usuarios con su contraseña en claro, leer pagos con PAN y CVV, consultar y anular ventas, y usar los endpoints de configuración, diagnóstico e integraciones. Se verificó contra el sistema en ejecución: `GET /api/usuarios` y `GET /api/pagos` responden 200 sin cookie.

El login existe (`AuthControlador.login` guarda el `SecurityContext` en la sesión), pero ninguna regla lo exige, así que iniciar sesión no cambia lo que se puede hacer.

## Decisión

Se reemplaza el `permitAll()` global por una regla que exige sesión en toda la API, con una lista corta y explícita de excepciones públicas:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/salud", "/actuator/health").permitAll()
        .requestMatchers("/error").permitAll()
        .anyRequest().authenticated())
.exceptionHandling(errores -> errores
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
```

| Acceso | Rutas | Por qué |
|---|---|---|
| **Público** | `POST /api/auth/login`, `POST /api/auth/logout` | Sin ellas no se puede iniciar ni cerrar sesión |
| **Público** | `GET /api/salud`, `GET /actuator/health` | Monitoreo del contenedor; no devuelven datos del negocio |
| **Público** | `/error` | Para que una respuesta de error no se convierta en un 401 |
| **Requiere sesión** | Todo lo demás | Cualquier módulo del POS opera con datos del negocio |

- **Lo público se lista, no lo protegido.** `anyRequest().authenticated()` va al final, así que un endpoint nuevo nace protegido. Con el enfoque contrario (listar lo que se protege), un endpoint que se olvide queda público.
- **Identidad:** se toma del `SecurityContext` que `AuthControlador.login` guarda en la sesión del servidor, no de un parámetro del body o la URL.
- **Respuesta sin sesión: 401.** Se configura `HttpStatusEntryPoint(UNAUTHORIZED)` para responder un 401 limpio. Sin esto, como HTTP Basic sigue habilitado, la respuesta incluiría el reto `WWW-Authenticate: Basic` y el navegador mostraría su ventana de usuario y contraseña.
- **El registro (`/api/auth/registro`) deja de ser público.** No está en la lista de excepciones, así que pide sesión como el resto.

## Alternativas consideradas

| Opción | Pros | Contras |
|---|---|---|
| Dejarlo como está y confiar en que el frontend redirige al login | Sin cambios | No protege nada: la API se llama directo con curl, sin pasar por el frontend |
| Listar las rutas a proteger y dejar el resto público | Cambio mínimo | Un endpoint olvidado o nuevo queda público; es el mismo error que se está corrigiendo |
| `@PreAuthorize("isAuthenticated()")` en cada controlador | La regla queda junto al código | Repartido en muchos archivos; un controlador nuevo sin la anotación queda abierto |
| **Excepciones públicas explícitas + `anyRequest().authenticated()` en `SecurityConfig` (elegida)** | Un solo lugar, revisable de un vistazo; se aplica antes de llegar al controlador; protegido por defecto | Cada endpoint que deba ser público hay que agregarlo a mano |

## Consecuencias

- **Positivas:**
  - Ningún endpoint de negocio responde sin sesión. Usuarios, pagos, ventas, clientes, reportes, configuración, diagnóstico e integraciones dejan de estar disponibles para cualquiera que conozca la URL.
  - Las demás vulnerabilidades del sistema dejan de ser explotables de forma anónima: ahora requieren una cuenta.
  - Un endpoint nuevo queda protegido por defecto.
- **Negativas / costos:**
  - La pantalla de **Registro** del frontend deja de funcionar para usuarios sin sesión (responde 401).
  - Esta corrección solo distingue entre "con sesión" y "sin sesión". **Cualquier usuario autenticado sigue pudiendo usar todos los módulos**, sin importar su rol. Eso se documenta y corrige por separado.
  - **CSRF sigue deshabilitado** y CORS sigue aceptando cualquier origen con credenciales (BAC-10). Ahora que el acceso depende de la cookie de sesión, eso permite que otro sitio haga peticiones en nombre de un usuario con sesión abierta. Se corrige por separado.
- **Prueba que demuestra el control:** `backend/src/test/java/com/curso/pv/config/ControlDeAccesoTest.java` (29 casos, `@WebMvcTest` con la `SecurityConfig` real):
  - sin sesión, en 18 rutas de los distintos módulos → **401**;
  - login, logout y salud sin sesión → **no** 401/403 (siguen públicos);
  - con sesión, en 8 rutas de uso normal → **no** 401/403 (el control no rompe el sistema).

  Resultado:

  | Versión de `SecurityConfig` | Resultado |
  |---|---|
  | Original (`permitAll`) | 🔴 `Tests run: 29, Failures: 18` (p. ej. `anonimoRecibe401: Status expected:<401> but was:<200>`) |
  | Con el fix | 🟢 `Tests run: 29, Failures: 0` |

  Comando: `cd backend && sh mvnw test -Dtest=ControlDeAccesoTest`
