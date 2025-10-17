package com.remus.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineaVenta {
    private Integer idLinea;
    private Integer idVenta;
    private Integer idProducto;
    private Integer cantidad;
    private Double precioVenta;
    private Integer descuento;
    private Double importeLinea;

    /**
     * Calcula el importe de la línea (cantidad * precio unitario)
     */
    public double getImporteLinea() {
        int cant = (cantidad != null) ? cantidad : 0;
        double precio = (importeLinea != null) ? importeLinea : 0.0;
        return cant * precio;
    }

    public void mostrar() {
        System.out.println("└─────────────────────────────────────────");
        System.out.printf("│ Cantidad: %d%n", cantidad != null ? cantidad : 0);
        System.out.printf("│ Precio Unitario: %.2f €%n", importeLinea != null ? importeLinea : 0.0);
        System.out.printf("│ Importe Línea: %.2f €%n", getImporteLinea());
        System.out.println("└─────────────────────────────────────────\n");
    }
}
