# punto-venta-no-seguro
Proyecto de un punto de venta para el curso de cyberseguridad aplicada que tiene todas las vulnerabilidades típicas de una aplicación web. Esta dividido en backend desarrollado con SpringBoot 4.1.0 + Java 21. El frontend con React 19 + Vite 6. La base de datos es MySQL 8.4 y se implementa mediante Docker y Docker compose

# Arquitectura

```
punto-venta-no-seguro/
├─ docker-compose.yml       # mysql + backend + frontend + adminer
├─ .env.example
├─ backend/                 # Spring Boot 4.1.0 + Java 21 (Maven Wrapper)
├─ frontend/                # React 19 + Vite 6
├─ database/                # init.sql (esquema) + seed.sql (datos)
```

## Puesta en marcha

```bash
docker compose up --build
```

| Servicio | URL | Credenciales |
|---|---|---|
| Frontend | http://localhost:5173 | - |
| Backend API | http://localhost:8080/api/salud | - |
| Adminer (BD) | http://localhost:8081 | servidor `mysql`, usuario `root`, password `root123` |
| MySQL (host) | localhost:3307 | `root` / `root123` |

Usuarios sembrados (VULNERABILIDAD: passwords en texto plano):
- `admin` / `admin` (ADMIN)
- `vendedor` / `vendedor` (VENDEDOR)
- `cajero` / `cajero` (VENDEDOR)
- `admin2` / `123456` (ADMIN)

Para reiniciar la base de datos desde cero hay que borrar el volumen:
```bash
docker compose down -v
```

## Módulos

| Módulo | Endpoints | Qué incluye |
|---|---|---|
| Autenticación | `/api/auth/*` | login, registro, logout, sesión, cookie `PVSESSION` |
| Usuarios | `/api/usuarios/*` | alta, edición, borrado, cambio de contraseña, búsqueda SQLi |
| Productos | `/api/productos/*` | catálogo, categorías, carga de imagen, stock |
| Categorías | `/api/categorias/*` | listado, búsqueda y alta |
| Clientes | `/api/clientes/*` | CRUD y búsqueda |
| Punto de venta | `/api/ventas/*` | carrito en sesión, checkout, anulación, facturas |
| Inventario | `/api/ventas/inventario/*` | entradas, salidas y Kardex |
| Pagos | `/api/pagos/*` | cobro simulado, consulta por tarjeta |
| Reportes | `/api/reportes/*` | resumen, ventas, export HTML/CSV, estructura de la BD |
| Integraciones | `/api/integraciones/*` | consulta y envío de datos a URLs arbitrarias |
| Configuración | `/api/configuracion/*`, `/api/diagnostico` | variables y secretos del sistema |

Los movimientos quedan registrados en la tabla `auditoria` (VULNERABILIDAD: se
guarda en texto plano y se inserta directo desde los servicios, sin control de
acceso).

Frontend: React 19 con rutas para login, registro, dashboard, catálogo, clientes,
punto de venta, pagos, facturas, reportes, usuarios y diagnóstico. Nginx sirve la
build y hace proxy de `/api` al backend, de modo que la sesión viaja por cookie.

## Desarrollo local (fuera de Docker)

```bash
# backend (necesita MySQL escuchando en localhost:3307)
cd backend && ./mvnw spring-boot:run

# frontend
cd frontend && npm install && npm run dev
```

El `.env.example` documenta las credenciales. En esta versión el backend las
tiene escritas en `application.yml` (VULNERABILIDAD: configuración en código).

# Vulnerabilidades típicas que el proyecto tiene:
- No sanitiza los datos en el envío desde el frontend
- No sanitiza los datos en la recepción en el backend
- Broken Access Control
- Permite SQL Injection
- Permite XSS (Cross-Site Scripting) 
- No implementa políticas de Seguridad de Contenido como prevención.
- Permite SSRF (Server-Side Request Forgery).
- Expone de datos sensibles.
- No maneja excepciones adecuadamente.
- Expone el Stack Trace a los clientes cuando ocurre una falla.
- No realizar logging seguro.
- No aplica cifrado en reposo.
- No gestiona de forma segura las sesiones.
- Maneja las variables de configuración directamente en código.
- Utiliza el usuario root para conectarse con la bd
- Permite manipular el precio de los productos desde el carrito
- Path traversal en la carga de imágenes de producto
- Contraseñas viajando en la URL

## Actividad para el alumno

El objetivo es que **encuentres, documentes y expliques** las vulnerabilidades por
tu cuenta. La lista de arriba indica *qué tipos* de fallos buscar; dónde se
producen y cómo explotarlos es parte del ejercicio.

Pistas de metodología:

- Revisa el tráfico con las DevTools del navegador y un proxy de interceptación
  (Burp Suite / OWASP ZAP). No todo lo sensible se ve en pantalla: parte de la
  configuración se "cachea" del lado del cliente en distintos almacenes.
- Inspecciona `Application > Local Storage`, `IndexedDB` y `Cookies` en DevTools.
- Observa qué cabeceras viajan en las respuestas (algunas cookies no se pueden
  leer desde JavaScript y solo aparecen interceptando la respuesta HTTP).
- Prueba los endpoints de la API directamente, sin pasar por el frontend.

