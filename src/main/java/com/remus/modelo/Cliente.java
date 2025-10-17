package com.remus.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de Cliente para el sistema GVS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private Integer idCliente;
    private String nombre;
    private String apellidos;
    private String dni;
    private Integer telefono;
    private String direccionHabitual;
    private String direccionEnvio;
    private LocalDateTime fechaRegistro;
    private Boolean activo;

    // Constructor sin ID (para inserciones)
    public Cliente(String nombre, String apellidos, String dni, int telefono,
                   String direccionHabitual, String direccionEnvio) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.direccionHabitual = direccionHabitual;
        this.direccionEnvio = direccionEnvio;
        this.activo = true;
    }

    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Representación en String para combo boxes
     */
    @Override
    public String toString() {
        return idCliente + " - " + getNombreCompleto() + " (" + dni + ")";
    }

    /**
     * Muestra la información del cliente en formato visual
     */
    public void mostrar() {
        System.out.println("┌─────────────────────────────────────────");
        System.out.printf("│ ID Cliente: %d%n", idCliente);
        System.out.printf("│ Nombre: %s %s%n", nombre, apellidos);
        System.out.printf("│ DNI: %s%n", dni);
        System.out.printf("│ Teléfono: %s%n", telefono);
        System.out.printf("│ Dirección Habitual: %s%n", direccionHabitual);
        System.out.printf("│ Dirección Envío: %s%n", direccionEnvio);
        System.out.printf("│ Estado: %s%n", activo ? "Activo" : "Inactivo");
        System.out.println("└─────────────────────────────────────────\n");
    }
}
