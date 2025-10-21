package com.remus.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.remus.modelo.Cliente;
import com.remus.modelo.Producto;
import com.remus.modelo.Venta;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utilidad para exportar datos del negocio a diferentes formatos (JSON/XML).
 */
public class UtilidadExportar {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // Formato JSON legible

    public static boolean exportarAJson(List<?> data, String fileName) {
        if (data == null || data.isEmpty()) {
            System.out.println("No hay datos para exportar.");
            return false;
        }

        try {
            File file = new File(fileName);
            mapper.writeValue(file, data);
            System.out.println(" Datos exportados a JSON: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println(" Error al exportar a JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Métodos específicos (opcionales, pero útiles para la GUI)
    public static boolean exportarClientes(List<Cliente> clientes, String fileName) {
        return exportarAJson(clientes, fileName);
    }

    public static boolean exportarProductos(List<Producto> productos, String fileName) {
        return exportarAJson(productos, fileName);
    }

    public static boolean exportarVentas(List<Venta> ventas, String fileName) {
        return exportarAJson(ventas, fileName);
    }
}