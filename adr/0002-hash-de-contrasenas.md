# ADR-0002 · Las contraseñas se almacenan con hash BCrypt

- **Estado:** Aceptado
- **Fecha:** <fecha de su commit>
- **Autor(es):** <su nombre>
- **Hallazgos relacionados:** CRY-03

## Contexto

`CodificadorEnClaro` implementaba `PasswordEncoder` sin cifrar nada:

```java
public String encode(CharSequence p) { return p.toString(); }
public boolean matches(CharSequence p, String almacenado) { return p.toString().equals(almacenado); }
```

Se verificó contra la base en ejecución (Adminer, `SELECT username, password, rol FROM usuarios`):
las cuatro cuentas sembradas (`admin/admin`, `admin2/123456`, `vendedor/vendedor`,
`cajero/cajero`) tienen la columna `password` idéntica a la contraseña real, en claro.

Una fuga de la base, un respaldo expuesto o la inyección SQL ya documentada en
`UsuarioConsultasInseguras` entregan credenciales listas para usar, de todas las
cuentas, sin ningún esfuerzo de cómputo.

## Decisión

Se reemplaza `CodificadorEnClaro` por `BCryptPasswordEncoder` (ya incluido en
`spring-boot-starter-security`, sin dependencia nueva):

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Se hashea la contraseña en los tres puntos donde se escribe: `AuthServicio.registrar`,
`UsuarioServicio.crear` y `UsuarioServicio.cambiarPassword`. En `UsuarioServicio.actualizar`
solo se re-hashea si llega un valor nuevo y no vacío; antes se sobrescribía siempre,
incluso con `null`, lo que habría invalidado la contraseña de cualquier usuario editado
sin querer cambiarla.

`UsuariosDetailsService` no cambia: sigue leyendo `usuario.getPassword()` y se lo pasa a
Spring Security, que ahora compara con `BCryptPasswordEncoder.matches()` en vez de una
igualdad de strings.

### Migración de los datos existentes

`database/seed.sql` solo corre una vez, al crear el volumen de MySQL. Para no perder el
acceso a las cuentas ya sembradas (ni exigir borrar el volumen), se agrega
`MigracionPasswordsRunner`, un `ApplicationRunner` que al arrancar re-hashea cualquier
contraseña que no tenga el prefijo `$2a$/$2b$/$2y$`. Es idempotente: una vez migrada, no
se vuelve a tocar.

## Alternativas consideradas

| Opción | Pros | Contras |
|---|---|---|
| Dejarlo en claro y confiar en TLS / control de acceso | Sin cambios | No protege nada si hay fuga de la base, backup robado o SQLi (ya documentado) |
| MD5/SHA-256 sin sal | Más simple que BCrypt | Rápido de romper por fuerza bruta/rainbow tables; no es un hash adaptativo |
| **BCrypt vía `BCryptPasswordEncoder` (elegida)** | Hash adaptativo con sal integrada, estándar de Spring Security, sin dependencia nueva | Costo de CPU algo mayor por login (aceptable para este volumen) |

## Consecuencias

- **Positivas:**
    - Una fuga de la base ya no entrega contraseñas utilizables directamente.
    - El login sigue funcionando igual para el usuario final; el cambio es transparente.
- **Negativas / costos:**
    - Sin `MigracionPasswordsRunner`, las cuentas sembradas antes del fix quedarían
      inutilizables. Se documenta ese componente como parte de esta misma corrección.
    - Esto no corrige que la API siga devolviendo el campo `password` (aunque ahora
      como hash) en las respuestas; eso se corrige por separado en ADR-0003 (DATA-03).
    - `UsuarioConsultasInseguras.porUsername` sigue siendo vulnerable a SQLi; no se
      tocó porque es un hallazgo de otra familia.

- **Prueba que demuestra el control:**
    - Antes: `SELECT password FROM usuarios` muestra el valor en claro.
    - Después: el mismo `SELECT` muestra `$2a$10$...`; el login con la misma
      contraseña original sigue respondiendo 200.
    - (Agreguen aquí su captura de Adminer antes/después y el resultado de su prueba JUnit si la hicieron.)