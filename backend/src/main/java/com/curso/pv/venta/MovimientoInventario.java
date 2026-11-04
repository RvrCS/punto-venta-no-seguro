package com.curso.pv.venta;

import com.curso.pv.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario extends BaseEntity {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 0;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior = 0;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo = 0;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "venta_id")
    private Long ventaId;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "registrado_en")
    private LocalDateTime registradoEn = LocalDateTime.now();

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getRegistradoEn() {
        return registradoEn;
    }

    public void setRegistradoEn(LocalDateTime registradoEn) {
        this.registradoEn = registradoEn;
    }
}