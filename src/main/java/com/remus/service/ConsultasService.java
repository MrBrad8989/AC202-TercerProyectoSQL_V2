package com.remus.service;

import com.remus.connection.ConexionBD;
import com.remus.modelo.Cliente;
import com.remus.modelo.Producto;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio con las 5 consultas/reportes requeridas
 */
public class ConsultasService {

    // ========== CONSULTA 1: Clientes Ordenados por Apellidos ==========
    public List<Map<String, Object>> consultaClientesOrdenados() {
        List<Map<String, Object>> resultados = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, " +
                "direccion_habitual, direccion_envio FROM CLIENTES " +
                "ORDER BY apellidos, nombre";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("ID", rs.getInt("id_cliente"));
                fila.put("Nombre", rs.getString("nombre"));
                fila.put("Apellidos", rs.getString("apellidos"));
                fila.put("DNI", rs.getString("dni"));
                fila.put("Teléfono", rs.getInt("telefono"));
                fila.put("Dir. Habitual", rs.getString("direccion_habitual"));
                fila.put("Dir. Envío", rs.getString("direccion_envio"));
                resultados.add(fila);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en Consulta 1: " + e.getMessage(), e);
        }
        return resultados;
    }

    // ========== CONSULTA 2: Productos Ordenados por Descripción ==========
    public List<Map<String, Object>> consultaProductosOrdenados() {
        List<Map<String, Object>> resultados = new ArrayList<>();
        String sql = "SELECT id_producto, codigo, descripcion, precio_recomendado, stock " +
                "FROM PRODUCTOS WHERE activo = 1 ORDER BY descripcion";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("ID", rs.getInt("id_producto"));
                fila.put("Código", rs.getString("codigo"));
                fila.put("Descripción", rs.getString("descripcion"));
                fila.put("Precio", String.format("%.2f €", rs.getDouble("precio_recomendado")));
                fila.put("Stock", rs.getInt("stock"));
                resultados.add(fila);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en Consulta 2: " + e.getMessage(), e);
        }
        return resultados;
    }

    // ========== CONSULTA 3: Ventas con Subreporte de Líneas ==========
    public Map<Integer, Map<String, Object>> consultaVentasConLineas() {
        Map<Integer, Map<String, Object>> ventas = new LinkedHashMap<>();
        String sql = "SELECT v.id_venta, v.fecha_venta, c.id_cliente, c.nombre, c.apellidos, " +
                "v.descuento_global, v.importe_total, lv.id_linea, p.codigo, p.descripcion, " +
                "lv.cantidad, lv.precio_venta, lv.descuento, lv.importe_linea " +
                "FROM VENTAS v " +
                "LEFT JOIN CLIENTES c ON v.id_cliente = c.id_cliente " +
                "LEFT JOIN LINEAS_VENTA lv ON v.id_venta = lv.id_venta " +
                "LEFT JOIN PRODUCTOS p ON lv.id_producto = p.id_producto " +
                "ORDER BY v.id_venta DESC, lv.id_linea";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idVenta = rs.getInt("id_venta");

                // Crear entrada principal de venta si no existe
                if (!ventas.containsKey(idVenta)) {
                    Map<String, Object> venta = new LinkedHashMap<>();
                    venta.put("ID Venta", idVenta);
                    venta.put("Fecha", rs.getDate("fecha_venta"));
                    venta.put("Cliente", rs.getString("nombre") + " " + rs.getString("apellidos"));
                    venta.put("Descuento Global", rs.getDouble("descuento_global") + "%");
                    venta.put("Importe Total", String.format("%.2f €", rs.getDouble("importe_total")));
                    venta.put("Líneas", new ArrayList<>());
                    ventas.put(idVenta, venta);
                }

                // Agregar línea si existe
                if (rs.getInt("id_linea") > 0) {
                    Map<String, Object> linea = new LinkedHashMap<>();
                    linea.put("Código", rs.getString("codigo"));
                    linea.put("Descripción", rs.getString("descripcion"));
                    linea.put("Cantidad", rs.getInt("cantidad"));
                    linea.put("Precio Unit.", String.format("%.2f €", rs.getDouble("precio_venta")));
                    linea.put("Descuento", rs.getInt("descuento") + "%");
                    linea.put("Importe Línea", String.format("%.2f €", rs.getDouble("importe_linea")));

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> lineas =
                            (List<Map<String, Object>>) ventas.get(idVenta).get("Líneas");
                    lineas.add(linea);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en Consulta 3: " + e.getMessage(), e);
        }
        return ventas;
    }

    // ========== CONSULTA 4: Resumen de Ventas por Rango de Fechas ==========
    public Map<String, Object> consultaResumenVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> resumen = new LinkedHashMap<>();
        String sql = "SELECT " +
                "COUNT(DISTINCT id_venta) AS num_ventas, " +
                "SUM(importe_total) AS volumen_total, " +
                "AVG(importe_total) AS promedio_venta, " +
                "MAX(importe_total) AS venta_maxima, " +
                "MIN(importe_total) AS venta_minima " +
                "FROM VENTAS " +
                "WHERE fecha_venta BETWEEN ? AND ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                resumen.put("Período", fechaInicio + " a " + fechaFin);
                resumen.put("Número de Ventas", rs.getInt("num_ventas"));
                resumen.put("Volumen Total", String.format("%.2f €", rs.getDouble("volumen_total")));
                resumen.put("Promedio por Venta", String.format("%.2f €", rs.getDouble("promedio_venta")));
                resumen.put("Venta Máxima", String.format("%.2f €", rs.getDouble("venta_maxima")));
                resumen.put("Venta Mínima", String.format("%.2f €", rs.getDouble("venta_minima")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en Consulta 4: " + e.getMessage(), e);
        }
        return resumen;
    }

    // ========== CONSULTA 5: Ventas por Cliente en Rango de Fechas ==========
    public List<Map<String, Object>> consultaVentasPorCliente(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        String sql = "SELECT " +
                "c.id_cliente, " +
                "c.nombre || ' ' || c.apellidos AS cliente, " +
                "c.dni, " +
                "COUNT(v.id_venta) AS num_ventas, " +
                "SUM(v.importe_total) AS volumen_total, " +
                "AVG(v.importe_total) AS promedio_venta, " +
                "MAX(v.importe_total) AS venta_maxima, " +
                "MIN(v.importe_total) AS venta_minima " +
                "FROM CLIENTES c " +
                "LEFT JOIN VENTAS v ON c.id_cliente = v.id_cliente " +
                "AND v.fecha_venta BETWEEN ? AND ? " +
                "WHERE c.activo = 1 " +
                "GROUP BY c.id_cliente " +
                "HAVING COUNT(v.id_venta) > 0 " +
                "ORDER BY volumen_total DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("Cliente", rs.getString("cliente"));
                fila.put("DNI", rs.getString("dni"));
                fila.put("Num. Ventas", rs.getInt("num_ventas"));
                fila.put("Volumen Total", String.format("%.2f €", rs.getDouble("volumen_total")));
                fila.put("Promedio", String.format("%.2f €", rs.getDouble("promedio_venta")));
                fila.put("Venta Máx.", String.format("%.2f €", rs.getDouble("venta_maxima")));
                fila.put("Venta Mín.", String.format("%.2f €", rs.getDouble("venta_minima")));
                resultados.add(fila);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en Consulta 5: " + e.getMessage(), e);
        }
        return resultados;
    }

    // ========== MÉTODO AUXILIAR: Convertir resultados a String formateado ==========
    public String formatearResultados(String titulo, List<Map<String, Object>> datos) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════╗\n");
        sb.append("║ ").append(String.format("%-40s", titulo)).append(" ║\n");
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