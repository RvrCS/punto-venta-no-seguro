package com.curso.pv.config;

import com.curso.pv.recurso.SaludController;
import com.curso.pv.security.UsuariosDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del hallazgo BAC-01 (API publica) y de la decision ADR-0001.
 *
 * Solo se carga la capa web con la configuracion de seguridad real; los
 * controladores de negocio no se cargan, asi que una peticion que SI pasa el
 * filtro de seguridad termina en el manejador de errores (no 401/403). Lo que
 * se comprueba aqui es exclusivamente la decision de acceso.
 *
 * Sin el fix (todo permitAll) las pruebas de 401 fallan: un anonimo llega a
 * todos los endpoints con 200.
 */
@WebMvcTest(controllers = SaludController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ControlDeAccesoTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuariosDetailsService usuariosDetailsService;

    private static final RequestPostProcessor CON_SESION =
            user("vendedor").authorities(() -> "ROL_VENDEDOR");

    @ParameterizedTest(name = "anonimo {0} {1} -> 401")
    @DisplayName("Sin sesion, los modulos del POS responden 401")
    @CsvSource({
            "GET,    /api/usuarios",
            "GET,    /api/usuarios/1",
            "PUT,    /api/usuarios/1",
            "GET,    /api/pagos",
            "GET,    /api/pagos/tarjeta?numero=4111111111111111",
            "GET,    /api/ventas",
            "POST,   /api/ventas/1/anular",
            "GET,    /api/clientes",
            "GET,    /api/productos",
            "DELETE, /api/productos/1",
            "GET,    /api/categorias",
            "GET,    /api/reportes/resumen",
            "GET,    /api/configuracion",
            "GET,    /api/diagnostico",
            "GET,    /api/integraciones/verificar-host?host=mysql&puerto=3306",
            "GET,    /api/auth/yo",
            "GET,    /api/auth/sesion",
            "GET,    /actuator/env",
    })
    void anonimoRecibe401(String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "anonimo {0} {1} -> permitido")
    @DisplayName("Login, logout y salud siguen siendo publicos")
    @CsvSource({
            "GET,  /api/salud",
            "POST, /api/auth/login",
            "POST, /api/auth/logout",
    })
    void rutasPublicasSiguenAbiertas(String metodo, String ruta) throws Exception {
        int codigo = mvc.perform(request(HttpMethod.valueOf(metodo), ruta))
                .andReturn().getResponse().getStatus();
        assertThat(codigo).isNotIn(401, 403);
    }

    @ParameterizedTest(name = "con sesion {0} {1} -> permitido")
    @DisplayName("Con sesion iniciada, la API sigue funcionando")
    @CsvSource({
            "GET,  /api/auth/yo",
            "GET,  /api/productos",
            "GET,  /api/clientes",
            "GET,  /api/ventas/carrito",
            "POST, /api/ventas/checkout",
            "GET,  /api/ventas",
            "POST, /api/pagos/procesar",
            "GET,  /api/reportes/resumen",
    })
    void conSesionSePermite(String metodo, String ruta) throws Exception {
        int codigo = mvc.perform(request(HttpMethod.valueOf(metodo), ruta).with(CON_SESION))
                .andReturn().getResponse().getStatus();
        assertThat(codigo).isNotIn(401, 403);
    }
}
