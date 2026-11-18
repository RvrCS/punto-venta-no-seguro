package com.curso.pv.config;

import com.curso.pv.reporte.ReporteControlador;
import com.curso.pv.reporte.ReporteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReporteControlador.class)
public class XssTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteServicio reporteServicio;

    @BeforeEach
    void setUp() {
        when(reporteServicio.exportarHtml(anyString(), anyString()))
                .thenAnswer(invocation -> "<html><body><h1>Reporte</h1><p>Cliente: " + invocation.getArgument(0) + "</p></body></html>");
    }

    @Test
    public void dadoPayloadXss() throws Exception {
        String payloadXss = "<script>alert('XSS-Reportes')</script>";

        mockMvc.perform(get("/api/reportes/exportar/html")
                        .param("nombreCliente", payloadXss)
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isOk())
                // Verifica que las etiquetas peligrosas hayan sido escapadas correctamente
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")))
                // Verifica que la etiqueta cruda y ejecutable NO esté presente en la respuesta
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))));
    }

    @Test
    public void dadoNombreNormal() throws Exception {
        String nombreLegitimo = "Empresa Comercializadora SA";

        mockMvc.perform(get("/api/reportes/exportar/html")
                        .param("nombreCliente", nombreLegitimo)
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isOk())
                // Verifica que el texto legítimo se renderice con normalidad
                .andExpect(content().string(org.hamcrest.Matchers.containsString(nombreLegitimo)));
    }

    @Test
    public void dadoParametroNulo() throws Exception {
        mockMvc.perform(get("/api/reportes/exportar/html")
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isOk());
    }
}