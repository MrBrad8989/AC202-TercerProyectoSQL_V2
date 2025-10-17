package com.remus.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    private Integer id;
    private String cif;
    private String nombre;
    private String domicilio;
    private String localidad;
    private String logoPath;
    private String colorPrincipal;
    private LocalDateTime fechaCreacion;

    public Empresa(String cif, String nombre, String domicilio, String localidad) {
        this.cif = cif;
        this.nombre = nombre;
        this.domicilio = domicilio;
        this.localidad = localidad;
    }
}
