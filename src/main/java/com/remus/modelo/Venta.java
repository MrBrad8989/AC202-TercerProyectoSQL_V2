package com.remus.modelo;

import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {
    private Integer idVenta;
    private Cliente cliente;
    private LocalDate fechaVenta;
    private Double descuentoGlobal;
    private Double importeTotal;
    private String observaciones;
    private String estado;
    private List<LineaVenta> lineasVenta;

    public Venta(Cliente cliente, LocalDate fechaVenta) {
        this.cliente = cliente;
        this.fechaVenta = fechaVenta;
        this.descuentoGlobal = 0.0;
        this.importeTotal = 0.0;
        this.estado = "COMPLETADA";
        this.lineasVenta = new ArrayList<>();
    }

    /**
     * Añade una línea de venta
     */
    public void agregarLinea(LineaVenta linea) {
        if (lineasVenta == null) {
            lineasVenta = new ArrayList<>();
        }
        lineasVenta.add(linea);
    }

    /**
     * Calcula el total de la venta sumando todas las líneas
     */
    public Double calcularTotal() {
        if (lineasVenta == null || lineasVenta.isEmpty()) {
            return 0.0;
        }
        return lineasVenta.stream()
                .mapToDouble(LineaVenta::getImporteLinea)
                .sum();
    }

    /**
     * Obtiene el número de líneas de venta
     */
    public int getCantidadLineas() {
        return lineasVenta != null ? lineasVenta.size() : 0;
    }

    @Override
    public String toString() {
        return String.format("Venta #%d - %s - Cliente: %s - Total: %.2f €",
                idVenta, fechaVenta,
                cliente != null ? cliente.getNombreCompleto() : "N/A",
                importeTotal);
    }

    public void mostrar() {
        System.out.println("╔═════════════════════════════════════════");
        System.out.printf("║ VENTA #%d%n", idVenta);
        System.out.printf("║ Fecha: %s%n", fechaVenta != null ? fechaVenta.toString() : "N/A");
        System.out.printf("║ Cliente: %s%n", cliente != null ? cliente.getNombreCompleto() : "N/A");
        System.out.printf("║ Estado: %s%n", estado != null ? estado : "N/A");
        System.out.printf("║ Descuento global: %.2f %% %n", descuentoGlobal != null ? descuentoGlobal : 0.0);
        System.out.println("╠═════════════════════════════════════════");

        if (lineasVenta == null || lineasVenta.isEmpty()) {
            System.out.println("║ (Sin líneas de venta)");
        } else {
            System.out.println("║ LÍNEAS:");
            for (int i = 0; i < lineasVenta.size(); i++) {
                LineaVenta lv = lineasVenta.get(i);
                // Protección si la clase LineaVenta no está completamente implementada
                String detalle = lv != null ? lv.toString() : "Línea vacía";
                System.out.printf("║  %d) %s%n", i + 1, detalle);
            }
        }

        // Recalcular importe total si es necesario
        double totalCalculado = calcularTotal();
        // aplicar descuento global entendido como porcentaje (por ejemplo 10.0 => 10%)
        double descuento = (descuentoGlobal != null) ? descuentoGlobal : 0.0;
        double totalConDescuento = totalCalculado - (totalCalculado * descuento / 100.0);
        // guardar en el campo importeTotal para mantener consistencia
        this.importeTotal = totalConDescuento;

        System.out.println("╠═════════════════════════════════════════");
        System.out.printf("║ Total bruto: %.2f €%n", totalCalculado);
        System.out.printf("║ Total con descuento: %.2f €%n", totalConDescuento);
        if (observaciones != null && !observaciones.isEmpty()) {
            System.out.println("╠═════════════════════════════════════════");
            System.out.printf("║ Observaciones: %s%n", observaciones);
        }
        System.out.println("╚═════════════════════════════════════════\n");
    }

}
