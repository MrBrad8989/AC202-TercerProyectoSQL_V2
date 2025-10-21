package com.remus.vista;

import com.remus.dao.ClienteDAOImpl;
import com.remus.dao.EmpresaDAOImpl;
import com.remus.dao.ProductoDAOImpl;
import com.remus.dao.VentaDAOImpl;
import com.remus.dao.interfaces.IClienteDAO;
import com.remus.dao.interfaces.IEmpresaDAO;
import com.remus.dao.interfaces.IProductoDAO;
import com.remus.dao.interfaces.IVentaDAO;
import com.remus.modelo.Cliente;
import com.remus.modelo.Empresa;
import com.remus.modelo.Producto;
import com.remus.modelo.Venta;
import com.remus.utility.PanelGestionVentas;
import com.remus.utility.PanelReportes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class muestraGestionEmpresa extends JFrame {

    private final IClienteDAO clienteDAO = new ClienteDAOImpl();
    private final IEmpresaDAO empresaDAO = new EmpresaDAOImpl();
    private final IProductoDAO productoDAO = new ProductoDAOImpl();
    private final IVentaDAO ventaDAO = new VentaDAOImpl();

    // 'tabs' se usa solo en el constructor, lo creamos como variable local allí

    // Componentes clientes
    private final DefaultListModel<Cliente> clientesListModel = new DefaultListModel<>();
    private final JList<Cliente> listClientes = new JList<>(clientesListModel);

    // Componentes empresas
    private final DefaultListModel<Empresa> empresasListModel = new DefaultListModel<>();
    private final JList<Empresa> listEmpresas = new JList<>(empresasListModel);

    // Componentes productos
    private final DefaultListModel<Producto> productosListModel = new DefaultListModel<>();
    private final JList<Producto> listProductos = new JList<>(productosListModel);

    // Componentes ventas
    private final DefaultListModel<Venta> ventasListModel = new DefaultListModel<>();
    private final JList<Venta> listVentas = new JList<>(ventasListModel);

    private static final Logger LOGGER = Logger.getLogger(muestraGestionEmpresa.class.getName());

    public muestraGestionEmpresa() {
        setTitle("Sistema de Gestión - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", crearPanelClientes());
        tabs.addTab("Empresas", crearPanelEmpresas());
        tabs.addTab("Productos", crearPanelProductos());
        tabs.addTab("Ventas", crearPanelVentas());
        tabs.addTab("Reportes", new PanelReportes());

        add(tabs);

        // Cargar datos iniciales
        recargarClientes();
        recargarEmpresas();
        recargarProductos();
        recargarVentas();
    }

    // ---------------- PANEL CLIENTES ----------------
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel izquierda = new JPanel(new BorderLayout(5,5));
        izquierda.add(new JLabel("Lista de Clientes:"), BorderLayout.NORTH);
        listClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        izquierda.add(new JScrollPane(listClientes), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> recargarClientes());
        izquierda.add(btnRefrescar, BorderLayout.SOUTH);

        panel.add(izquierda, BorderLayout.WEST);

        // Formulario insertar/modificar/eliminar
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; JTextField txtNombre = new JTextField(20); form.add(txtNombre, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Apellidos:"), gbc);
        gbc.gridx = 1; JTextField txtApellidos = new JTextField(20); form.add(txtApellidos, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; JTextField txtDni = new JTextField(20); form.add(txtDni, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; JTextField txtTelefono = new JTextField(20); form.add(txtTelefono, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Dirección habitual:"), gbc);
        gbc.gridx = 1; JTextField txtDirHab = new JTextField(20); form.add(txtDirHab, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Dirección envío:"), gbc);
        gbc.gridx = 1; JTextField txtDirEnv = new JTextField(20); form.add(txtDirEnv, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnInsertar = new JButton("Insertar Cliente");
        JButton btnModificar = new JButton("Modificar Cliente");
        JButton btnEliminar = new JButton("Eliminar Cliente");
        btnPanel.add(btnInsertar);
        btnPanel.add(btnModificar);
        btnPanel.add(btnEliminar);
        form.add(btnPanel, gbc);

        // Al seleccionar un cliente, rellenar los campos
        listClientes.addListSelectionListener(e -> {
            Cliente c = listClientes.getSelectedValue();
            if (c != null) {
                txtNombre.setText(c.getNombre());
                txtApellidos.setText(c.getApellidos());
                txtDni.setText(c.getDni());
                txtTelefono.setText(c.getTelefono() != null ? c.getTelefono().toString() : "");
                txtDirHab.setText(c.getDireccionHabitual());
                txtDirEnv.setText(c.getDireccionEnvio());
            }
        });

        btnInsertar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String apellidos = txtApellidos.getText().trim();
                String dni = txtDni.getText().trim();
                String tel = txtTelefono.getText().trim();
                Integer telefono = null;
                if (!tel.isEmpty()) telefono = Integer.parseInt(tel);

                Cliente c = new Cliente();
                c.setNombre(nombre);
                c.setApellidos(apellidos);
                c.setDni(dni);
                c.setTelefono(telefono);
                c.setDireccionHabitual(txtDirHab.getText().trim());
                c.setDireccionEnvio(txtDirEnv.getText().trim());

                boolean ok = clienteDAO.insertar(c);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cliente insertado correctamente");
                    recargarClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo insertar el cliente", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Teléfono inválido", "Error de entrada", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al insertar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al insertar cliente", ex);
            }
        });

        btnModificar.addActionListener(e -> {
            Cliente c = listClientes.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente para modificar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                c.setNombre(txtNombre.getText().trim());
                c.setApellidos(txtApellidos.getText().trim());
                c.setDni(txtDni.getText().trim());
                String tel = txtTelefono.getText().trim();
                c.setTelefono(!tel.isEmpty() ? Integer.parseInt(tel) : null);
                c.setDireccionHabitual(txtDirHab.getText().trim());
                c.setDireccionEnvio(txtDirEnv.getText().trim());
                boolean ok = clienteDAO.actualizar(c);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cliente modificado correctamente");
                    recargarClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo modificar el cliente", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al modificar cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al modificar cliente", ex);
            }
        });

        btnEliminar.addActionListener(e -> {
            Cliente c = listClientes.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar el cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = clienteDAO.eliminar(c.getIdCliente());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente");
                    recargarClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el cliente", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- PANEL EMPRESAS ----------------
    private JPanel crearPanelEmpresas() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));

        JPanel left = new JPanel(new BorderLayout(5,5));
        left.add(new JLabel("Lista de Empresas:"), BorderLayout.NORTH);
        listEmpresas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listEmpresas.setVisibleRowCount(10);
        JScrollPane empresasScroll = new JScrollPane(listEmpresas);
        empresasScroll.setPreferredSize(new Dimension(260, 300));
        left.add(empresasScroll, BorderLayout.CENTER);
        JButton btnRef = new JButton("Refrescar"); btnRef.addActionListener(e -> recargarEmpresas());
        left.add(btnRef, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("CIF:"), gbc);
        gbc.gridx = 1; JTextField txtCif = new JTextField(20); form.add(txtCif, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; JTextField txtNombre = new JTextField(20); form.add(txtNombre, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Domicilio:"), gbc);
        gbc.gridx = 1; JTextField txtDomicilio = new JTextField(20); form.add(txtDomicilio, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Localidad:"), gbc);
        gbc.gridx = 1; JTextField txtLocalidad = new JTextField(20); form.add(txtLocalidad, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnInsert = new JButton("Insertar Empresa");
        JButton btnModificar = new JButton("Modificar Empresa");
        JButton btnEliminar = new JButton("Eliminar Empresa");
        btnPanel.add(btnInsert);
        btnPanel.add(btnModificar);
        btnPanel.add(btnEliminar);
        form.add(btnPanel, gbc);

        // Al seleccionar una empresa, rellenar los campos
        listEmpresas.addListSelectionListener(e -> {
            Empresa emp = listEmpresas.getSelectedValue();
            if (emp != null) {
                txtCif.setText(emp.getCif());
                txtNombre.setText(emp.getNombre());
                txtDomicilio.setText(emp.getDomicilio());
                txtLocalidad.setText(emp.getLocalidad());
            }
        });

        btnInsert.addActionListener(e -> {
            try {
                Empresa emp = new Empresa();
                emp.setCif(txtCif.getText().trim());
                emp.setNombre(txtNombre.getText().trim());
                emp.setDomicilio(txtDomicilio.getText().trim());
                emp.setLocalidad(txtLocalidad.getText().trim());

                if (empresaDAO.insertar(emp)) {
                    JOptionPane.showMessageDialog(this, "Empresa insertada correctamente");
                    recargarEmpresas();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo insertar la empresa", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al insertar empresa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al insertar empresa", ex);
            }
        });

        btnModificar.addActionListener(e -> {
            Empresa emp = listEmpresas.getSelectedValue();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una empresa para modificar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                emp.setCif(txtCif.getText().trim());
                emp.setNombre(txtNombre.getText().trim());
                emp.setDomicilio(txtDomicilio.getText().trim());
                emp.setLocalidad(txtLocalidad.getText().trim());
                boolean ok = empresaDAO.actualizar(emp);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Empresa modificada correctamente");
                    recargarEmpresas();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo modificar la empresa", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al modificar empresa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al modificar empresa", ex);
            }
        });

        btnEliminar.addActionListener(e -> {
            Empresa emp = listEmpresas.getSelectedValue();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una empresa para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar la empresa?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = empresaDAO.eliminar(emp.getId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Empresa eliminada correctamente");
                    recargarEmpresas();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la empresa", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- PANEL PRODUCTOS ----------------
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));

        JPanel left = new JPanel(new BorderLayout(5,5));
        left.add(new JLabel("Lista de Productos:"), BorderLayout.NORTH);
        listProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(new JScrollPane(listProductos), BorderLayout.CENTER);
        JButton btnRef = new JButton("Refrescar"); btnRef.addActionListener(e -> recargarProductos());
        left.add(btnRef, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1; JTextField txtCodigo = new JTextField(20); form.add(txtCodigo, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; JTextField txtDesc = new JTextField(20); form.add(txtDesc, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Precio recomendado:"), gbc);
        gbc.gridx = 1; JTextField txtPrecio = new JTextField(20); form.add(txtPrecio, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Stock inicial:"), gbc);
        gbc.gridx = 1; JTextField txtStock = new JTextField(20); form.add(txtStock, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Stock mínimo:"), gbc);
        gbc.gridx = 1; JTextField txtStockMin = new JTextField(20); form.add(txtStockMin, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnInsert = new JButton("Insertar Producto");
        JButton btnModificar = new JButton("Modificar Producto");
        JButton btnEliminar = new JButton("Eliminar Producto");
        btnPanel.add(btnInsert);
        btnPanel.add(btnModificar);
        btnPanel.add(btnEliminar);
        form.add(btnPanel, gbc);

        // Al seleccionar un producto, rellenar los campos
        listProductos.addListSelectionListener(e -> {
            Producto p = listProductos.getSelectedValue();
            if (p != null) {
                txtCodigo.setText(p.getCodigo());
                txtDesc.setText(p.getDescripcion());
                txtPrecio.setText(String.valueOf(p.getPrecioRecomendado()));
                txtStock.setText(String.valueOf(p.getStock()));
                txtStockMin.setText(String.valueOf(p.getStockMinimo()));
            }
        });

        btnInsert.addActionListener(e -> {
            try {
                String codigo = txtCodigo.getText().trim();
                String desc = txtDesc.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());
                int stockMin = Integer.parseInt(txtStockMin.getText().trim());

                Producto p = new Producto(codigo, desc, precio, stock, stockMin);
                if (productoDAO.insertar(p)) {
                    JOptionPane.showMessageDialog(this, "Producto insertado correctamente");
                    recargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo insertar el producto", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Campos numéricos inválidos", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al insertar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al insertar producto", ex);
            }
        });

        btnModificar.addActionListener(e -> {
            Producto p = listProductos.getSelectedValue();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto para modificar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                p.setCodigo(txtCodigo.getText().trim());
                p.setDescripcion(txtDesc.getText().trim());
                p.setPrecioRecomendado(Double.parseDouble(txtPrecio.getText().trim()));
                p.setStock(Integer.parseInt(txtStock.getText().trim()));
                p.setStockMinimo(Integer.parseInt(txtStockMin.getText().trim()));
                boolean ok = productoDAO.actualizar(p);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Producto modificado correctamente");
                    recargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo modificar el producto", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al modificar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Error al modificar producto", ex);
            }
        });

        btnEliminar.addActionListener(e -> {
            Producto p = listProductos.getSelectedValue();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar el producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = productoDAO.eliminar(p.getIdProducto());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Producto eliminado correctamente");
                    recargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- PANEL VENTAS ----------------
    private JPanel crearPanelVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Usar el panel de gestión de ventas personalizado
        JPanel panelGestion = new PanelGestionVentas(this);
        panel.add(panelGestion, BorderLayout.CENTER);

        return panel;
    }

    // ---------------- RECARGA DATOS ----------------
    private void recargarClientes() {
        try {
            clientesListModel.clear();
            List<Cliente> list = clienteDAO.obtenerTodos();
            if (list != null) list.forEach(clientesListModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al cargar clientes", e);
        }
    }

    private void recargarEmpresas() {
        try {
            empresasListModel.clear();
            List<Empresa> list = empresaDAO.obtenerTodas();
            if (list != null) list.forEach(empresasListModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar empresas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al cargar empresas", e);
        }
    }

    private void recargarProductos() {
        try {
            productosListModel.clear();
            List<Producto> list = productoDAO.obtenerTodos();
            if (list != null) list.forEach(productosListModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al cargar productos", e);
        }
    }

    private void recargarVentas() {
        try {
            ventasListModel.clear();
            List<Venta> list = ventaDAO.obtenerTodas();
            if (list != null) list.forEach(ventasListModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar ventas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error al cargar ventas", e);
        }
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new muestraGestionEmpresa().setVisible(true));
    }
}
