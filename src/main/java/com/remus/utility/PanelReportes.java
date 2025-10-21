package com.remus.utility;

import com.remus.service.ConsultasService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel para visualizar las 5 consultas/reportes
 */
public class PanelReportes extends JPanel {

    private final ConsultasService consultasService = new ConsultasService();
    private static final Logger LOGGER = Logger.getLogger(PanelReportes.class.getName());

    private JTabbedPane tabReportes;
    private JTable tablaClientesReporte;
    private JTable tablaProductosReporte;
    private JTable tablaVentasClienteReporte;
    private JTextArea areaVentasDetalladas;
    private JTextArea areaResumenVentas;

    public PanelReportes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de opciones superior
        add(crearPanelOpciones(), BorderLayout.NORTH);

        // Panel con tabs para cada reporte
        tabReportes = new JTabbedPane();
        tabReportes.addTab("Clientes", crearPanelClientes());
        tabReportes.addTab("Productos", crearPanelProductos());
        tabReportes.addTab("Ventas Detalladas", crearPanelVentasDetalladas());
        tabReportes.addTab("Resumen Ventas", crearPanelResumenVentas());
        tabReportes.addTab("Ventas por Cliente", crearPanelVentasPorCliente());

        add(tabReportes, BorderLayout.CENTER);
    }

    // ==================== PANEL OPCIONES ====================
    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(new TitledBorder("Opciones de Reportes"));

        JButton btnActualizar = new JButton("🔄 Actualizar Todo");
        btnActualizar.addActionListener(e -> actualizarTodosLosReportes());

        panel.add(btnActualizar);

        return panel;
    }

    // ==================== REPORTE 1: CLIENTES ====================
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tablaClientesReporte = new JTable();
        JScrollPane scroll = new JScrollPane(tablaClientesReporte);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCargar = new JButton("Cargar Clientes");
        btnCargar.addActionListener(e -> cargarReporteClientes());
        panel.add(btnCargar, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarReporteClientes() {
        try {
            List<Map<String, Object>> datos = consultasService.consultaCLIENTESOrdenados();
            mostrarEnTabla(tablaClientesReporte, datos);
            mostrarMensaje("Se cargaron " + datos.size() + " clientes");
        } catch (Exception e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en reporte clientes", e);
        }
    }

    // ==================== REPORTE 2: PRODUCTOS ====================
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tablaProductosReporte = new JTable();
        JScrollPane scroll = new JScrollPane(tablaProductosReporte);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCargar = new JButton("Cargar Productos");
        btnCargar.addActionListener(e -> cargarReporteProductos());
        panel.add(btnCargar, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarReporteProductos() {
        try {
            List<Map<String, Object>> datos = consultasService.consultaPRODUCTOSOrdenados();
            mostrarEnTabla(tablaProductosReporte, datos);
            mostrarMensaje("Se cargaron " + datos.size() + " productos");
        } catch (Exception e) {
            mostrarError("Error al cargar productos: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en reporte productos", e);
        }
    }

    // ==================== REPORTE 3: VENTAS DETALLADAS ====================
    private JPanel crearPanelVentasDetalladas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        areaVentasDetalladas = new JTextArea();
        areaVentasDetalladas.setEditable(false);
        areaVentasDetalladas.setFont(new Font("Courier New", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(areaVentasDetalladas);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCargar = new JButton("Cargar Ventas Detalladas");
        btnCargar.addActionListener(e -> cargarReporteVentasDetalladas());
        panel.add(btnCargar, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarReporteVentasDetalladas() {
        try {
            Map<Integer, Map<String, Object>> ventas = consultasService.consultaVENTASConLineas();
            StringBuilder sb = new StringBuilder();
            sb.append("\n════════════════════════════════════════════════════════════\n");
            sb.append("REPORTE: VENTAS CON LÍNEAS DETALLADAS\n");
            sb.append("════════════════════════════════════════════════════════════\n\n");

            if (ventas.isEmpty()) {
                sb.append("Sin ventas registradas.\n");
            } else {
                for (Map<String, Object> venta : ventas.values()) {
                    sb.append("─────────────────────────────────────────────────────────────\n");
                    sb.append(String.format("ID Venta: %s | Fecha: %s | Cliente: %s\n",
                            venta.get("ID Venta"), venta.get("Fecha"), venta.get("Cliente")));
                    sb.append(String.format("Descuento Global: %s | Total: %s\n\n",
                            venta.get("Descuento Global"), venta.get("Importe Total")));

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> lineas = (List<Map<String, Object>>) venta.get("Líneas");

                    sb.append("LÍNEAS DE VENTA:\n");
                    sb.append(String.format("%-15s %-30s %-10s %-12s %-12s %-15s\n",
                            "Código", "Descripción", "Cantidad", "Precio U.", "Desc. %", "Importe"));
                    sb.append("─".repeat(95)).append("\n");

                    if (lineas != null && !lineas.isEmpty()) {
                        for (Map<String, Object> linea : lineas) {
                            sb.append(String.format("%-15s %-30s %-10s %-12s %-12s %-15s\n",
                                    linea.get("Código") != null ? linea.get("Código") : "N/A",
                                    linea.get("Descripción") != null ? linea.get("Descripción") : "N/A",
                                    linea.get("Cantidad") != null ? linea.get("Cantidad") : "0",
                                    linea.get("Precio Unit.") != null ? linea.get("Precio Unit.") : "0.00 €",
                                    linea.get("Descuento") != null ? linea.get("Descuento") : "0%",
                                    linea.get("Importe Línea") != null ? linea.get("Importe Línea") : "0.00 €"));
                        }
                    } else {
                        sb.append("(Sin líneas de venta)\n");
                    }
                    sb.append("\n");
                }
            }

            sb.append("════════════════════════════════════════════════════════════\n");
            sb.append("Total de ventas: ").append(ventas.size()).append("\n");

            areaVentasDetalladas.setText(sb.toString());
            areaVentasDetalladas.setCaretPosition(0);
            mostrarMensaje("Se cargaron " + ventas.size() + " ventas");
        } catch (Exception e) {
            mostrarError("Error al cargar ventas: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en reporte ventas detalladas", e);
        }
    }

    // ==================== REPORTE 4: RESUMEN VENTAS ====================
    private JPanel crearPanelResumenVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de filtros
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBorder(new TitledBorder("Rango de Fechas"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filtros.add(new JLabel("Fecha Inicio:"), gbc);

        gbc.gridx = 1;
        JTextField txtFechaInicio = new JTextField(LocalDate.now().minusMonths(1).toString(), 15);
        filtros.add(txtFechaInicio, gbc);

        gbc.gridx = 2;
        filtros.add(new JLabel("Fecha Fin:"), gbc);

        gbc.gridx = 3;
        JTextField txtFechaFin = new JTextField(LocalDate.now().toString(), 15);
        filtros.add(txtFechaFin, gbc);

        gbc.gridx = 4;
        JButton btnGenerar = new JButton("Generar");
        btnGenerar.addActionListener(e -> {
            try {
                LocalDate inicio = LocalDate.parse(txtFechaInicio.getText().trim());
                LocalDate fin = LocalDate.parse(txtFechaFin.getText().trim());
                cargarReporteResumenVentas(inicio, fin);
            } catch (Exception ex) {
                mostrarError("Formato de fecha inválido (usar YYYY-MM-DD): " + ex.getMessage());
            }
        });
        filtros.add(btnGenerar, gbc);

        panel.add(filtros, BorderLayout.NORTH);

        // Área de texto para resultados
        areaResumenVentas = new JTextArea();
        areaResumenVentas.setEditable(false);
        areaResumenVentas.setFont(new Font("Courier New", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaResumenVentas);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void cargarReporteResumenVentas(LocalDate inicio, LocalDate fin) {
        try {
            Map<String, Object> datos = consultasService.consultaResumenVENTAS(inicio, fin);
            areaResumenVentas.setText(consultasService.formatearResumen("RESUMEN DE VENTAS", datos));
            areaResumenVentas.setCaretPosition(0);
            mostrarMensaje("Reporte generado exitosamente");
        } catch (Exception e) {
            mostrarError("Error al generar resumen: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en resumen ventas", e);
        }
    }

    // ==================== REPORTE 5: VENTAS POR CLIENTE ====================
    private JPanel crearPanelVentasPorCliente() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de filtros
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBorder(new TitledBorder("Rango de Fechas"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filtros.add(new JLabel("Fecha Inicio:"), gbc);

        gbc.gridx = 1;
        JTextField txtFechaInicio = new JTextField(LocalDate.now().minusMonths(1).toString(), 15);
        filtros.add(txtFechaInicio, gbc);

        gbc.gridx = 2;
        filtros.add(new JLabel("Fecha Fin:"), gbc);

        gbc.gridx = 3;
        JTextField txtFechaFin = new JTextField(LocalDate.now().toString(), 15);
        filtros.add(txtFechaFin, gbc);

        gbc.gridx = 4;
        JButton btnGenerar = new JButton("Generar");
        btnGenerar.addActionListener(e -> {
            try {
                LocalDate inicio = LocalDate.parse(txtFechaInicio.getText().trim());
                LocalDate fin = LocalDate.parse(txtFechaFin.getText().trim());
                cargarReporteVentasPorCliente(inicio, fin);
            } catch (Exception ex) {
                mostrarError("Formato de fecha inválido (usar YYYY-MM-DD): " + ex.getMessage());
            }
        });
        filtros.add(btnGenerar, gbc);

        panel.add(filtros, BorderLayout.NORTH);

        // Tabla para resultados
        tablaVentasClienteReporte = new JTable();
        JScrollPane scroll = new JScrollPane(tablaVentasClienteReporte);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void cargarReporteVentasPorCliente(LocalDate inicio, LocalDate fin) {
        try {
            List<Map<String, Object>> datos = consultasService.consultaVENTASPorCliente(inicio, fin);
            mostrarEnTabla(tablaVentasClienteReporte, datos);
            mostrarMensaje("Se cargaron " + datos.size() + " clientes con ventas");
        } catch (Exception e) {
            mostrarError("Error al cargar reporte: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en ventas por cliente", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void mostrarEnTabla(JTable tabla, List<Map<String, Object>> datos) {
        if (datos == null || datos.isEmpty()) {
            tabla.setModel(new DefaultTableModel());
            return;
        }

        Map<String, Object> primeraFila = datos.get(0);
        String[] columnas = primeraFila.keySet().toArray(new String[0]);

        Object[][] filas = new Object[datos.size()][columnas.length];
        for (int i = 0; i < datos.size(); i++) {
            Map<String, Object> fila = datos.get(i);
            for (int j = 0; j < columnas.length; j++) {
                filas[i][j] = fila.get(columnas[j]);
            }
        }

        DefaultTableModel modelo = new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        tabla.setModel(modelo);

        // Ajustar ancho de columnas
        for (int i = 0; i < columnas.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
    }

    private void actualizarTodosLosReportes() {
        try {
            cargarReporteClientes();
            cargarReporteProductos();
            cargarReporteVentasDetalladas();
            cargarReporteResumenVentas(LocalDate.now().minusMonths(1), LocalDate.now());
            cargarReporteVentasPorCliente(LocalDate.now().minusMonths(1), LocalDate.now());
            mostrarMensaje("Todos los reportes actualizados");
        } catch (Exception e) {
            mostrarError("Error al actualizar: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error actualizando reportes", e);
        }
    }

    private void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}