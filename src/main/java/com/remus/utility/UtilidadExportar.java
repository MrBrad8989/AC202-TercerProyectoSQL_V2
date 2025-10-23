package com.remus.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Necesario para LocalDateTime
import com.remus.modelo.Cliente;
import com.remus.modelo.Producto;
import com.remus.modelo.Venta;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para exportar datos del negocio a formato JSON (Requisito 7.3).
 */
public class UtilidadExportar {

    private static final Logger LOGGER = Logger.getLogger(UtilidadExportar.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper()
            // Habilitar formato legible
            .enable(SerializationFeature.INDENT_OUTPUT)
            // Necesario para que Jackson maneje LocalDate, LocalDateTime, etc.
            .registerModule(new JavaTimeModule())
            // Ignorar errores al manejar fechas/propiedades
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Exporta una lista de objetos a un archivo JSON.
     * @param data Lista de objetos.
     * @param fileName Nombre del archivo de destino (ej: clientes.json).
     * @return true si la exportación fue exitosa, false en caso contrario.
     */
    public static boolean exportarAJson(List<?> data, String fileName) {
        if (data == null || data.isEmpty()) {
            LOGGER.info("No hay datos para exportar.");
            return false;
        }

        try {
            File file = new File(fileName);

            // Asegurar que la carpeta de exportación existe si usa un path relativo
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            mapper.writeValue(file, data);
            LOGGER.log(Level.INFO, "Datos exportados a JSON: {0}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al exportar a JSON: " + fileName, e);
            return false;
        }
    }

    // Métodos específicos (AHORA ACEPTAN EL NOMBRE DEL ARCHIVO)
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