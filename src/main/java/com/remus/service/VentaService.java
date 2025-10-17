package com.remus.service;

import com.remus.connection.ConexionBD;
import com.remus.dao.*;
import com.remus.dao.interfaces.*;
import com.remus.modelo.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servicio de negocio para gestionar ventas con validaciones y transacciones
 */
public class VentaService {

    private final IVentaDAO ventaDAO = new VentaDAOImpl();
    private final IProductoDAO productoDAO = new ProductoDAOImpl();
    private final IClienteDAO clienteDAO = new ClienteDAOImpl();
    private final ILineaVentaDAO lineaVentaDAO = new LineaVentaDAOImpl();

    // Constante: rango permitido de variación de precio (±20%)
    private static final double RANGO_PRECIO = 0.20;

    /**
     * Valida un cliente existente
     */
    public void validarClienteExistente(int idCliente) throws IllegalArgumentException {
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente con ID " + idCliente + " no existe");
        }
    }

    /**
     * Valida que un producto exista
     */
    public void validarProductoExistente(int idProducto) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
        }
    }

    /**
     * Valida que la cantidad sea válida (> 0)
     */
    public void validarCantidad(int cantidad) throws IllegalArgumentException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }

    /**
     * Valida que hay stock suficiente
     */
    public void validarStock(int idProducto, int cantidad) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        if (!producto.hayStock(cantidad)) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Stock disponible: " + producto.getStock() +
                            ", cantidad solicitada: " + cantidad);
        }
    }

    /**
     * Valida que el precio esté dentro del rango permitido (±20% del recomendado)
     */
    public void validarPrecioVenta(int idProducto, double precioVenta) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        double precioRecomendado = producto.getPrecioRecomendado();
        double minimo = precioRecomendado * (1 - RANGO_PRECIO);
        double maximo = precioRecomendado * (1 + RANGO_PRECIO);

        if (precioVenta < minimo || precioVenta > maximo) {
            throw new IllegalArgumentException(
                    String.format("Precio fuera de rango. Rango permitido: %.2f€ - %.2f€ (±20%% de %.2f€)",
                            minimo, maximo, precioRecomendado));
        }
    }

    /**
     * Valida el descuento de una línea (0-100)
     */
    public void validarDescuento(int descuento) throws IllegalArgumentException {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
    }

    /**
     * Calcula el importe de una línea: cantidad * precio - (cantidad * precio * descuento / 100)
     */
    public double calcularImporteLinea(int cantidad, double precioVenta, int descuento) {
        double subtotal = cantidad * precioVenta;
        double descuentoAplicado = subtotal * (descuento / 100.0);
        return subtotal - descuentoAplicado;
    }

    /**
     * Inserta una venta completa con sus líneas (TRANSACCIÓN)
     * Incluye validaciones y actualización automática de stock
     */
    public int insertarVentaConTransaccion(Venta venta) throws Exception {
        Connection conn = ConexionBD.getConexion();
        conn.setAutoCommit(false); // Desactivar auto-commit para controlar transacción

        try {
            // 1. Validar cliente
            validarClienteExistente(venta.getCliente().getIdCliente());

            // 2. Validar líneas de venta
            if (venta.getLineasVenta() == null || venta.getLineasVenta().isEmpty()) {
                throw new IllegalArgumentException("La venta debe contener al menos una línea");
            }

            for (LineaVenta linea : venta.getLineasVenta()) {
                validarProductoExistente(linea.getIdProducto());
                validarCantidad(linea.getCantidad());
                validarStock(linea.getIdProducto(), linea.getCantidad());
                validarPrecioVenta(linea.getIdProducto(), linea.getPrecioVenta());
                validarDescuento(linea.getDescuento());
            }

            // 3. Insertar venta (sin líneas aún)
            String sqlVenta = "INSERT INTO VENTAS (id_cliente, fecha_venta, descuento_global, importe_total, observaciones, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVenta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, venta.getCliente().getIdCliente());
                pstmt.setString(2, venta.getFechaVenta().toString());
                pstmt.setDouble(3, venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0);
                pstmt.setDouble(4, 0.0); // Se actualizará después
                pstmt.setString(5, venta.getObservaciones() != null ? venta.getObservaciones() : "");
                pstmt.setString(6, venta.getEstado() != null ? venta.getEstado() : "COMPLETADA");

                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID de la venta");
                }
                int idVentaGenerado = rs.getInt(1);
                venta.setIdVenta(idVentaGenerado);
            }

            // 4. Insertar líneas de venta y actualizar stock
            String sqlLinea = "INSERT INTO LINEAS_VENTA (id_venta, id_producto, cantidad, precio_venta, descuento, importe_linea) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            for (LineaVenta linea : venta.getLineasVenta()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlLinea)) {
                    double importe = calcularImporteLinea(linea.getCantidad(), linea.getPrecioVenta(), linea.getDescuento());

                    pstmt.setInt(1, venta.getIdVenta());
                    pstmt.setInt(2, linea.getIdProducto());
                    pstmt.setInt(3, linea.getCantidad());
                    pstmt.setDouble(4, linea.getPrecioVenta());
                    pstmt.setInt(5, linea.getDescuento());
                    pstmt.setDouble(6, importe);

                    pstmt.executeUpdate();
                }

                // Actualizar stock (restar cantidad vendida)
                Producto p = productoDAO.obtenerPorId(linea.getIdProducto());
                int nuevoStock = p.getStock() - linea.getCantidad();
                if (nuevoStock < 0) {
                    throw new IllegalArgumentException("Stock insuficiente para producto ID: " + linea.getIdProducto());
                }
                productoDAO.actualizarStock(linea.getIdProducto(), nuevoStock);
            }

            // 5. Llamar a función CALCULAR_TOTAL_LINEAS_VENTA
            double totalCalculado = calcularTotalLineasVenta(conn, venta.getIdVenta());

            // Aplicar descuento global si existe
            double descuentoGlobal = venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0;
            double totalConDescuento = totalCalculado - (totalCalculado * descuentoGlobal / 100.0);

            // 6. Actualizar importe_total en VENTAS
            String sqlUpdate = "UPDATE VENTAS SET importe_total = ? WHERE id_venta = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, totalConDescuento);
                pstmt.setInt(2, venta.getIdVenta());
                pstmt.executeUpdate();
            }

            // 7. COMMIT - confirmar transacción
            conn.commit();
            System.out.println("✓ Venta #" + venta.getIdVenta() + " registrada exitosamente");
            return venta.getIdVenta();

        } catch (Exception e) {
            // ROLLBACK - revertir cambios en caso de error
            try {
                conn.rollback();
                System.err.println("✗ Transacción revertida: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("✗ Error al revertir: " + rollbackEx.getMessage());
            }
            throw e;
        } finally {
            // Restaurar auto-commit
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }

    /**
     * Calcula el total de líneas de una venta
     */
    private double calcularTotalLineasVenta(Connection conn, int idVenta) throws SQLException {
        String sql = "SELECT SUM(importe_linea) as total FROM LINEAS_VENTA WHERE id_venta = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                return Double.isNaN(total) ? 0.0 : total;
            }
            return 0.0;
        }
    }

    /**
     * Obtiene el precio recomendado de un producto
     */
    public double obtenerPrecioRecomendado(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getPrecioRecomendado() : 0.0;
    }

    /**
     * Obtiene el stock actual de un producto
     */
    public int obtenerStockProducto(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getStock() : 0;
    }

    /**
     * Obtiene el rango de precio permitido para un producto
     */
    public double[] obtenerRangoPrecio(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        if (p != null) {
            double recomendado = p.getPrecioRecomendado();
            return new double[]{
                    recomendado * (1 - RANGO_PRECIO),
                    recomendado * (1 + RANGO_PRECIO)
            };
        }
        return new double[]{0, 0};
    }
}