package com.remus.utility;

import com.remus.connection.ConexionBD;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel para ejecutar consultas SQL dinámicas (Requisito 7.1)
 */
public class PanelConsultasSQL extends JPanel {

    private final JTextArea txtQuery;
    private final JTextArea txtResultado;
    private final JTable tablaResultado;
    private static final Logger LOGGER = Logger.getLogger(PanelConsultasSQL.class.getName());

    public PanelConsultasSQL() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Área de entrada de consulta (Query Input)
        JPanel panelInput = new JPanel(new BorderLayout(5, 5));
        panelInput.add(new JLabel("Consulta SQL a ejecutar (SELECT, UPDATE, DELETE...):"), BorderLayout.NORTH);
        txtQuery = new JTextArea(5, 60);
        JScrollPane scrollQuery = new JScrollPane(txtQuery);
        panelInput.add(scrollQuery, BorderLayout.CENTER);

        JButton btnEjecutar = new JButton("▶ Ejecutar SQL");
        btnEjecutar.addActionListener(e -> ejecutarConsulta());

        // Área de exportación
        JButton btnExportar = new JButton("Exportar Datos Visibles (JSON)");
        btnExportar.addActionListener(e -> exportarDatosVisibles());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnEjecutar);
        btnPanel.add(btnExportar);

        panelInput.add(btnPanel, BorderLayout.SOUTH);
        add(panelInput, BorderLayout.NORTH);

        // Área de salida de texto y tabla (Output)
        JSplitPane splitOutput = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitOutput.setResizeWeight(0.5); // Dividir 50/50

        // Salida de texto (mensajes DML)
        txtResultado = new JTextArea(5, 60);
        txtResultado.setEditable(false);
        txtResultado.setText("Esperando consulta...");
        splitOutput.setTopComponent(new JScrollPane(txtResultado));

        // Salida de tabla (resultados DQL)
        tablaResultado = new JTable();
        splitOutput.setBottomComponent(new JScrollPane(tablaResultado));

        add(splitOutput, BorderLayout.CENTER);
    }

    private void ejecutarConsulta() {
        String sql = txtQuery.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduce una consulta SQL.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarResultados();

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement()) {

            if (sql.toUpperCase().startsWith("SELECT")) {
                // Ejecutar consulta (DQL)
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    mostrarResultadoTabla(rs);
                }
            } else {
                // Ejecutar actualización (DML/DDL)
                int filasAfectadas = stmt.executeUpdate(sql);
                txtResultado.setText(" Comando ejecutado correctamente.\nFilas afectadas: " + filasAfectadas);
            }
        } catch (SQLException e) {
            txtResultado.setText(" ERROR SQL: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al ejecutar consulta SQL", e);
        } catch (Exception e) {
            txtResultado.setText(" ERROR JAVA: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error en la aplicación", e);
        }
    }

    private void mostrarResultadoTabla(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Nombres de las columnas
        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        // Datos de las filas
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }

        DefaultTableModel modelo = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaResultado.setModel(modelo);
        txtResultado.setText(" Consulta SELECT ejecutada correctamente.\nFilas obtenidas: " + data.size());
    }

    private void exportarDatosVisibles() {
        DefaultTableModel model = (DefaultTableModel) tablaResultado.getModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simular un objeto que contenga la tabla para exportar
        List<List<Object>> datos = new ArrayList<>();
        List<String> columnas = new ArrayList<>();

        // Obtener nombres de columnas
        for (int i = 0; i < model.getColumnCount(); i++) {
            columnas.add(model.getColumnName(i));
        }
        datos.add((List<Object>) (List<?>) columnas); // La primera fila son las columnas

        // Obtener datos de las filas
        for (int i = 0; i < model.getRowCount(); i++) {
            List<Object> fila = new ArrayList<>();
            for (int j = 0; j < model.getColumnCount(); j++) {
                fila.add(model.getValueAt(i, j));
            }
            datos.add(fila);
        }

        // Usar ExportUtility para exportar
        String fileName = JOptionPane.showInputDialog(this, "Introduce el nombre del archivo JSON (sin extensión):", "reporte_sql");
        if (fileName != null && !fileName.trim().isEmpty()) {
            if (UtilidadExportar.exportarAJson(datos, fileName.trim() + ".json")) {
                JOptionPane.showMessageDialog(this, "Datos de la tabla exportados a " + fileName.trim() + ".json", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo exportar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarResultados() {
        txtResultado.setText("");
        tablaResultado.setModel(new DefaultTableModel());
    }
}