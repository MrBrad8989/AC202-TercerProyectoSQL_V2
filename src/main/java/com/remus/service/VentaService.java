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

    private static final double RANGO_PRECIO = 0.20;

    public void validarClienteExistente(int idCliente) throws IllegalArgumentException {
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente con ID " + idCliente + " no existe");
        }
    }

    public void validarProductoExistente(int idProducto) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
        }
    }

    public void validarCantidad(int cantidad) throws IllegalArgumentException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }

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

    public void validarPrecioVenta(int idProducto, double precioVenta) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        if (producto.getPrecioRecomendado() == null) {
            throw new IllegalArgumentException("El producto no tiene un precio recomendado definido");
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

    public void validarDescuento(int descuento) throws IllegalArgumentException {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
    }

    public double calcularImporteLinea(int cantidad, double precioVenta, int descuento) {
        double subtotal = cantidad * precioVenta;
        double descuentoAplicado = subtotal * (descuento / 100.0);
        return subtotal - descuentoAplicado;
    }

    /**
     * Inserta una venta completa con sus líneas (TRANSACCIÓN)
     */
    public int insertarVentaConTransaccion(Venta venta) throws Exception {
        Connection conn = ConexionBD.getConexion();
        conn.setAutoCommit(false);

        try {
            validarClienteExistente(venta.getCliente().getIdCliente());

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

            String sqlVenta = "INSERT INTO VENTAS (id_cliente, fecha_venta, descuento_global, importe_total, observaciones, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVenta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, venta.getCliente().getIdCliente());
                pstmt.setString(2, venta.getFechaVenta().toString());
                pstmt.setDouble(3, venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0);
                pstmt.setDouble(4, 0.0);
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

            String sqlLinea = "INSERT INTO LINEAS_VENTA (id_venta, id_producto, cantidad, precio_venta, descuento_linea, importe_linea) " +
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

                /*
                 * NOTA: Se elimina la llamada a productoDAO.actualizarStock(linea.getIdProducto(), nuevoStock);
                 * para delegar esta tarea al TRIGGER de MySQL (trg_actualizar_stock_insert)
                 * y garantizar la integridad de la transacción en la BD.
                 */
            }

            // *** CÁLCULO EN JAVA: SUMAR IMPORTES DE LÍNEAS Y APLICAR DESCUENTO GLOBAL ***
            double totalCalculado = 0.0;
            for (LineaVenta linea : venta.getLineasVenta()) {
                double importeLinea = calcularImporteLinea(linea.getCantidad(), linea.getPrecioVenta(), linea.getDescuento());
                totalCalculado += importeLinea;
            }

            double descuentoGlobal = venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0;
            double totalConDescuento = totalCalculado - (totalCalculado * descuentoGlobal / 100.0);

            // Actualizar importe_total en la base de datos
            String sqlUpdate = "UPDATE VENTAS SET importe_total = ? WHERE id_venta = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                pstmtUpdate.setDouble(1, totalConDescuento);
                pstmtUpdate.setInt(2, venta.getIdVenta());
                pstmtUpdate.executeUpdate();
            }

            // Asignar el importe al objeto venta
            venta.setImporteTotal(totalConDescuento);

            conn.commit();
            System.out.println("✓ Venta #" + venta.getIdVenta() + " registrada exitosamente");
            return venta.getIdVenta();

        } catch (Exception e) {
            try {
                conn.rollback();
                System.err.println("✗ Transacción revertida: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("✗ Error al revertir: " + rollbackEx.getMessage());
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }

    public double obtenerPrecioRecomendado(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getPrecioRecomendado() : 0.0;
    }

    public int obtenerStockProducto(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getStock() : 0;
    }

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

