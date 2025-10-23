package com.remus.service;

import com.remus.dao.ConsultasDAOImpl;
import com.remus.dao.interfaces.IConsultasDAO;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio con las 5 consultas/reportes requeridas para MySQL
 * Ahora delega el acceso a datos a IConsultasDAO/ConsultasDAOImpl y solo mantiene
 * la lógica de formateo y orquestación cuando sea necesario.
 */
public class ConsultasService {

    private final IConsultasDAO consultasDAO = new ConsultasDAOImpl();

    // ========== CONSULTA 1: CLIENTES Ordenados por Apellidos ==========
    public List<Map<String, Object>> consultaCLIENTESOrdenados() {
        return consultasDAO.obtenerClientesOrdenados();
    }

    // ========== CONSULTA 2: PRODUCTOS Ordenados por Descripción ==========
    public List<Map<String, Object>> consultaPRODUCTOSOrdenados() {
        return consultasDAO.obtenerProductosOrdenados();
    }

    // ========== CONSULTA 3: VENTAS con Subreporte de Líneas ==========
    public Map<Integer, Map<String, Object>> consultaVENTASConLineas() {
        return consultasDAO.obtenerVentasConLineas();
    }

    // ========== CONSULTA 4: Resumen de VENTAS por Rango de Fechas ==========
    public Map<String, Object> consultaResumenVENTAS(LocalDate fechaInicio, LocalDate fechaFin) {
        return consultasDAO.obtenerResumenVentas(fechaInicio, fechaFin);
    }

    // ========== CONSULTA 5: VENTAS por Cliente en Rango de Fechas ==========
    public List<Map<String, Object>> consultaVENTASPorCliente(LocalDate fechaInicio, LocalDate fechaFin) {
        return consultasDAO.obtenerVentasPorCliente(fechaInicio, fechaFin);
    }

    // ========== METODO AUXILIAR: Convertir resultados a String formateado ==========
    public String formatearResultados(String titulo, List<Map<String, Object>> datos) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════╗\n");
        sb.append("║ ").append(String.format("%-40s", titulo)).append("                     ║\n");
        sb.append("╚════════════════════════════════════════════╝\n");

        if (datos.isEmpty()) {
            sb.append("Sin resultados.\n");
            return sb.toString();
        }

        // Encabezados
        Map<String, Object> primeraFila = datos.get(0);
        for (String clave : primeraFila.keySet()) {
            sb.append(String.format("%-20s | ", clave));
        }
        sb.append("\n");
        sb.append("─".repeat(primeraFila.size() * 22)).append("\n");

        // Datos
        for (Map<String, Object> fila : datos) {
            for (Object valor : fila.values()) {
                sb.append(String.format("%-20s | ", valor != null ? valor.toString() : "N/A"));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String formatearResumen(String titulo, Map<String, Object> datos) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════╗\n");
        sb.append("║ ").append(String.format("%-40s", titulo)).append(" ║\n");
        sb.append("╚════════════════════════════════════════════╝\n");

        if (datos.isEmpty()) {
            sb.append("Sin datos.\n");
            return sb.toString();
        }

        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            sb.append(String.format("%-30s: %s\n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }
}
