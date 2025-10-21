package com.remus.utility;

import com.remus.dao.*;
import com.remus.dao.interfaces.*;
import com.remus.modelo.*;
import com.remus.service.VentaService;

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
 * Panel GUI para gestionar el flujo completo de ventas
 */
public class PanelGestionVentas extends JPanel {

    private final IClienteDAO clienteDAO = new ClienteDAOImpl();
    private final IProductoDAO productoDAO = new ProductoDAOImpl();
    private final IVentaDAO ventaDAO = new VentaDAOImpl();
    private final VentaService ventaService = new VentaService();

    private static final Logger LOGGER = Logger.getLogger(PanelGestionVentas.class.getName());

    // Componentes principales
    private JComboBox<Cliente> cmbCliente;
    private JComboBox<Producto> cmbProducto;
    private JSpinner spnCantidad;
    private JTextField txtPrecioVenta;
    private JSpinner spnDescuento;
    private JButton btnAgregarLinea;
    private JButton btnEliminarLinea;
    private JButton btnConfirmarVenta;
    private JButton btnCancelar;

    // Tabla de líneas de venta
    private JTable tablaLineas;
    private DefaultTableModel modeloTabla;
    private List<LineaVenta> lineasActuales = new ArrayList<>();

    // Información de la venta
    private JLabel lblTotalBruto;
    private JLabel lblDescuentoGlobal;
    private JLabel lblTotalFinal;
    private JSpinner spnDescuentoGlobal;

    // Información del producto seleccionado
    private JLabel lblPrecioRecomendado;
    private JLabel lblStockDisponible;
    private JLabel lblRangoPrecio;

    public PanelGestionVentas(JFrame parent) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: Selección de cliente
        add(crearPanelCliente(), BorderLayout.NORTH);

        // Panel central: Captura de líneas
        add(crearPanelLineas(), BorderLayout.CENTER);

        // Panel inferior: Totales y botones
        add(crearPanelTotales(), BorderLayout.SOUTH);

        cargarClientes();
        cargarProductos();
    }

    // ==================== PANEL CLIENTE ====================
    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Seleccionar Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cmbCliente = new JComboBox<>();
        cmbCliente.addActionListener(e -> actualizarInfoCliente());
        panel.add(cmbCliente, gbc);

        return panel;
    }

    // ==================== PANEL LÍNEAS ====================
    private JPanel crearPanelLineas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Parte superior: Captura de datos
        JPanel captura = crearPanelCaptura();
        panel.add(captura, BorderLayout.NORTH);

        // Tabla de líneas
        modeloTabla = new DefaultTableModel(
                new String[]{"#", "Producto", "Cantidad", "Precio Unit.", "Descuento %", "Importe"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };

        tablaLineas = new JTable(modeloTabla);
        tablaLineas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaLineas), BorderLayout.CENTER);

        // Botones de líneas
        JPanel botonesLineas = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnAgregarLinea = new JButton("Agregar Línea");
        btnAgregarLinea.addActionListener(e -> agregarLinea());
        btnEliminarLinea = new JButton("Eliminar Línea");
        btnEliminarLinea.addActionListener(e -> eliminarLinea());

        botonesLineas.add(btnAgregarLinea);
        botonesLineas.add(btnEliminarLinea);
        panel.add(botonesLineas, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== PANEL CAPTURA ====================
    private JPanel crearPanelCaptura() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Datos de Línea de Venta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Producto
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Producto:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        cmbProducto = new JComboBox<>();
        cmbProducto.addActionListener(e -> actualizarInfoProducto());
        panel.add(cmbProducto, gbc);

        // Cantidad
        gbc.gridx = 2;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.15;
        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(spnCantidad, gbc);

        y++;

        // Precio venta
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(new JLabel("Precio Venta:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        txtPrecioVenta = new JTextField(10);
        panel.add(txtPrecioVenta, gbc);

        // Descuento línea
        gbc.gridx = 2;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(new JLabel("Descuento %:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.15;
        spnDescuento = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        panel.add(spnDescuento, gbc);

        y++;

        // Info producto
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 4;
        JPanel infoProd = crearPanelInfoProducto();
        panel.add(infoProd, gbc);

        return panel;
    }

    // ==================== PANEL INFO PRODUCTO ====================
    private JPanel crearPanelInfoProducto() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        lblPrecioRecomendado = new JLabel("Precio Recomendado: --");
        lblStockDisponible = new JLabel("Stock: --");
        lblRangoPrecio = new JLabel("Rango: --");

        panel.add(lblPrecioRecomendado);
        panel.add(lblStockDisponible);
        panel.add(lblRangoPrecio);

        return panel;
    }

    // ==================== PANEL TOTALES ====================
    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Resumen de totales
        JPanel resumen = new JPanel(new GridBagLayout());
        resumen.setBorder(new TitledBorder("Resumen de Venta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;

        int y = 0;

        gbc.gridx = 0;
        gbc.gridy = y;
        resumen.add(new JLabel("Total Bruto:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        lblTotalBruto = new JLabel("0.00 €");
        lblTotalBruto.setFont(new Font("Arial", Font.BOLD, 12));
        resumen.add(lblTotalBruto, gbc);

        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.WEST;
        resumen.add(new JLabel("Descuento Global %:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.3;
        spnDescuentoGlobal = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        spnDescuentoGlobal.addChangeListener(e -> actualizarTotales());
        resumen.add(spnDescuentoGlobal, gbc);

        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.WEST;
        resumen.add(new JLabel("Descuento aplicado:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        lblDescuentoGlobal = new JLabel("0.00 €");
        resumen.add(lblDescuentoGlobal, gbc);

        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.WEST;
        resumen.add(new JLabel("TOTAL FINAL:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        lblTotalFinal = new JLabel("0.00 €");
        lblTotalFinal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalFinal.setForeground(Color.BLUE);
        resumen.add(lblTotalFinal, gbc);

        panel.add(resumen, BorderLayout.CENTER);

        // Botones de acción
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        btnConfirmarVenta = new JButton("Confirmar Venta");
        btnConfirmarVenta.setForeground(new Color(0, 100, 0));
        btnConfirmarVenta.setFont(new Font("Arial", Font.BOLD, 12));
        btnConfirmarVenta.addActionListener(e -> confirmarVenta());

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setForeground(new Color(200, 0, 0));
        btnCancelar.addActionListener(e -> cancelarVenta());

        botones.add(btnConfirmarVenta);
        botones.add(btnCancelar);
        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== MÉTODOS DE LÓGICA ====================

    private void cargarClientes() {
        try {
            cmbCliente.removeAllItems();
            List<Cliente> clientes = clienteDAO.obtenerTodos();
            if (clientes != null) {
                for (Cliente c : clientes) {
                    cmbCliente.addItem(c);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error cargando clientes", e);
        }
    }

    private void cargarProductos() {
        try {
            cmbProducto.removeAllItems();
            List<Producto> productos = productoDAO.obtenerActivos();
            if (productos != null) {
                for (Producto p : productos) {
                    cmbProducto.addItem(p);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error cargando productos", e);
        }
    }

    /**
     * Método público para permitir que otros componentes soliciten la recarga de productos en este panel.
     */
    public void recargarProductos() {
        cargarProductos();
    }

    private void actualizarInfoCliente() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        if (c != null) {
            System.out.println("Cliente seleccionado: " + c.getNombreCompleto());
        }
    }

    private void actualizarInfoProducto() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p != null) {
            double precioRec = p.getPrecioRecomendado();
            int stock = p.getStock();

            lblPrecioRecomendado.setText(String.format("Precio Recomendado: %.2f €", precioRec));
            lblStockDisponible.setText(String.format("Stock: %d unidades", stock));

            double[] rango = ventaService.obtenerRangoPrecio(p.getIdProducto());
            lblRangoPrecio.setText(String.format("Rango: %.2f € - %.2f €", rango[0], rango[1]));

            txtPrecioVenta.setText(String.format("%.2f", precioRec));
        }
    }

    private void agregarLinea() {
        try {
            // Validaciones
            if (cmbProducto.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto producto = (Producto) cmbProducto.getSelectedItem();
            int cantidad = (Integer) spnCantidad.getValue();
            String precioStr = txtPrecioVenta.getText().trim();
            int descuento = (Integer) spnDescuento.getValue();

            if (precioStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa un precio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Normalizar y parsear precios que usen coma o punto y que puedan incluir separador de miles
            double precioVenta = NumberParser.parsePrecio(precioStr);

            // Validaciones de negocio
            ventaService.validarCantidad(cantidad);
            ventaService.validarStock(producto.getIdProducto(), cantidad);
            ventaService.validarPrecioVenta(producto.getIdProducto(), precioVenta);
            ventaService.validarDescuento(descuento);

            // Crear línea
            LineaVenta linea = new LineaVenta();
            linea.setIdProducto(producto.getIdProducto());
            linea.setCantidad(cantidad);
            linea.setPrecioVenta(precioVenta);
            linea.setDescuento(descuento);
            double importe = ventaService.calcularImporteLinea(cantidad, precioVenta, descuento);
            linea.setImporteLinea(importe);

            lineasActuales.add(linea);

            // Agregar a tabla
            modeloTabla.addRow(new Object[]{
                    lineasActuales.size(),
                    producto.getCodigo() + " - " + producto.getDescripcion(),
                    cantidad,
                    String.format("%.2f €", precioVenta),
                    descuento + "%",
                    String.format("%.2f €", importe)
            });

            actualizarTotales();

            // Limpiar formulario
            spnCantidad.setValue(1);
            txtPrecioVenta.setText("");
            spnDescuento.setValue(0);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio inválido: " + e.getMessage(), "Error de entrada", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al agregar línea", e);
        }
    }

    private void eliminarLinea() {
        int fila = tablaLineas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una línea para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lineasActuales.remove(fila);
        modeloTabla.removeRow(fila);

        // Renumerar
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            modeloTabla.setValueAt(i + 1, i, 0);
        }

        actualizarTotales();
    }

    private void actualizarTotales() {
        double totalBruto = 0;
        for (LineaVenta linea : lineasActuales) {
            totalBruto += linea.getImporteLinea();
        }

        double descuentoGlobal = (Double) spnDescuentoGlobal.getValue();
        double descuentoAplicado = totalBruto * (descuentoGlobal / 100.0);
        double totalFinal = totalBruto - descuentoAplicado;

        lblTotalBruto.setText(String.format("%.2f €", totalBruto));
        lblDescuentoGlobal.setText(String.format("%.2f €", descuentoAplicado));
        lblTotalFinal.setText(String.format("%.2f €", totalFinal));
    }

    private void confirmarVenta() {
        try {
            if (cmbCliente.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (lineasActuales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Agrega al menos una línea de venta", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear venta
            Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
            Venta venta = new Venta();
            venta.setCliente(cliente);
            venta.setFechaVenta(LocalDate.now());
            venta.setDescuentoGlobal((Double) spnDescuentoGlobal.getValue());
            venta.setEstado("COMPLETADA");
            venta.setLineasVenta(new ArrayList<>(lineasActuales));

            // Insertar con transacción
            int idVenta = ventaService.insertarVentaConTransaccion(venta);

            JOptionPane.showMessageDialog(this,
                    "✓ Venta #" + idVenta + " registrada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al registrar venta:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al confirmar venta", e);
        }
    }

    private void cancelarVenta() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas cancelar la venta?",
                "Confirmar cancelación",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            limpiarFormulario();
        }
    }

    private void limpiarFormulario() {
        lineasActuales.clear();
        modeloTabla.setRowCount(0);
        spnCantidad.setValue(1);
        txtPrecioVenta.setText("");
        spnDescuento.setValue(0);
        spnDescuentoGlobal.setValue(0.0);
        actualizarTotales();
        cargarClientes();
        cargarProductos();
    }
}
