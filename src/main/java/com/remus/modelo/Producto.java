package com.remus.modelo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Integer idProducto;
    private String codigo;
    private String descripcion;
    private Double precioRecomendado;
    private Integer stock;
    private Integer stockMinimo;
    private Boolean activo;
    private String fechaCreacion;

    public Producto(String codigo, String descripcion, Double precioRecomendado,
                    Integer stock, Integer stockMinimo) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioRecomendado = precioRecomendado;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.activo = true;
    }

    /**
     * Verifica si el precio está dentro del rango permitido (±20%)
     */
    public boolean validarPrecioVenta(Double precioVenta) {
        double min = precioRecomendado * 0.80;
        double max = precioRecomendado * 1.20;
        return precioVenta >= min && precioVenta <= max;
    }

    /**
     * Obtiene el precio mínimo permitido
     */
    public Double getPrecioMinimo() {
        return precioRecomendado * 0.80;
    }

    /**
     * Obtiene el precio máximo permitido
     */
    public Double getPrecioMaximo() {
        return precioRecomendado * 1.20;
    }

    /**
     * Verifica si hay stock disponible
     */
    public boolean hayStock(Integer cantidad) {
        return stock >= cantidad;
    }

    /**
     * Verifica si está por debajo del stock mínimo
     */
    public boolean bajosEnStock() {
        return stock < stockMinimo;
    }

    @Override
    public String toString() {
        return idProducto + " - " + codigo + " | " + descripcion +
                " (Stock: " + stock + ")";
    }

    public void mostrar() {
        System.out.println("┌─────────────────────────────────────────");
        System.out.printf("│ ID Producto: %d%n", idProducto);
        System.out.printf("│ Código: %s%n", codigo);
        System.out.printf("│ Descripción: %s%n", descripcion);
        System.out.printf("│ Precio Recomendado: %.2f €%n", precioRecomendado);
        System.out.printf("│ Stock Actual: %d unidades%n", stock);
        System.out.printf("│ Stock Mínimo: %d unidades%n", stockMinimo);
        System.out.printf("│ Estado: %s%n", activo ? "Activo" : "Inactivo");
        if (bajosEnStock()) {
            System.out.println("│  ALERTA: Stock bajo!");
        }
        System.out.println("└─────────────────────────────────────────\n");
    }
}
