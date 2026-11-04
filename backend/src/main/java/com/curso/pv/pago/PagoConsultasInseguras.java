package com.curso.pv.pago;

import com.curso.pv.common.ConsultasInseguras;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VULNERABILIDAD (SQLi): permite buscar pagos por el numero de tarjeta completo,
 * lo que expone los PAN guardados en claro.
 */
@Repository
public class PagoConsultasInseguras {

    private final ConsultasInseguras consultas;

    public PagoConsultasInseguras(ConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    public List<?> porTarjeta(String numero) {
        String sql = "SELECT * FROM pagos WHERE numero_tarjeta = '" + ConsultasInseguras.crudo(numero) + "'";
        return consultas.ejecutar(sql, Pago.class);
    }
}