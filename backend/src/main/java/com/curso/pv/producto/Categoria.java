package com.curso.pv.producto;

import com.curso.pv.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria extends BaseEntity {

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    // VULNERABILIDAD: la descripcion se renderiza sin sanear en el frontend
    @Column(name = "descripcion")
    private String descripcion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}