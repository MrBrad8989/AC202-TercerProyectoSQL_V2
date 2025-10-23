package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IVentaDAO;
import com.remus.modelo.Cliente;
import com.remus.modelo.LineaVenta;
import com.remus.modelo.Venta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentaDAOImpl implements IVentaDAO {

    private final ClienteDAOImpl clienteDAO = new ClienteDAOImpl();
    private final LineaVentaDAOImpl lineaVentaDAO = new LineaVentaDAOImpl();

    @Override
    public Venta obtenerPorId(int idVenta) {
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM VENTAS WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);
                venta.setLineasVenta(lineaVentaDAO.obtenerPorVenta(idVenta));
                return venta;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener venta por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Venta> obtenerTodas() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM VENTAS ORDER BY fecha_venta DESC";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Venta venta = mapearVenta(rs);
                venta.setLineasVenta(lineaVentaDAO.obtenerPorVenta(venta.getIdVenta()));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas: " + e.getMessage(), e);
        }
        return ventas;
    }

    @Override
    public List<Venta> obtenerPorCliente(int idCliente) {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM VENTAS WHERE id_cliente = ? ORDER BY fecha_venta DESC";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Venta venta = mapearVenta(rs);
                venta.setLineasVenta(lineaVentaDAO.obtenerPorVenta(venta.getIdVenta()));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas por cliente: " + e.getMessage(), e);
        }
        return ventas;
    }

    @Override
    public boolean insertar(Venta venta) {
        String sql = "INSERT INTO VENTAS (id_cliente, fecha_venta, importe_total) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, venta.getCliente().getIdCliente());
            pstmt.setString(2, venta.getFechaVenta().toString());
            pstmt.setDouble(3, venta.getImporteTotal());

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    venta.setIdVenta(idGenerado);

                    // Insertar líneas de venta asociadas
                    if (venta.getLineasVenta() != null) {
                        for (LineaVenta linea : venta.getLineasVenta()) {
                            linea.setIdVenta(idGenerado);
                            lineaVentaDAO.insertar(linea);
                        }
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar venta: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Venta venta) {
        String sql = "UPDATE VENTAS SET id_cliente = ?, fecha_venta = ?, importe_total = ? WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, venta.getCliente().getIdCliente());
            pstmt.setString(2, venta.getFechaVenta().toString());
            pstmt.setDouble(3, venta.getImporteTotal());
            pstmt.setInt(4, venta.getIdVenta());

            boolean ok = pstmt.executeUpdate() > 0;

            // actualizar líneas si es necesario
            if (ok && venta.getLineasVenta() != null) {
                for (LineaVenta lv : venta.getLineasVenta()) {
                    if (lv.getIdLinea() == null)
                        lineaVentaDAO.insertar(lv);
                    else
                        lineaVentaDAO.actualizar(lv);
                }
            }

            return ok;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar venta: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idVenta) {
        String sql = "DELETE FROM VENTAS WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);

            // eliminar líneas asociadas primero
            lineaVentaDAO.eliminarPorVenta(idVenta);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar venta: " + e.getMessage(), e);
        }
    }

    public boolean actualizarEstado(int idVenta, String nuevoEstado) {
        String sql = "UPDATE VENTAS SET ESTADO = ? WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idVenta);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de la venta: " + e.getMessage(), e);
        }
    }

    public double calcularImporteTotal(int idVenta) {
        String sql = "SELECT SUM(IMPORTE_LINEA) as total FROM LINEAS_VENTA WHERE ID_VENTA = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                // actualizar en la tabla ventas
                String update = "UPDATE VENTAS SET IMPORTE_TOTAL = ? WHERE id_venta = ?";
                try (PreparedStatement up = ConexionBD.getConexion().prepareStatement(update)) {
                    up.setDouble(1, total);
                    up.setInt(2, idVenta);
                    up.executeUpdate();
                }
                return total;
            }
            return 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular importe total de la venta: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertarConLineas(Venta venta) throws Exception {
        String sqlVenta = "INSERT INTO VENTAS (id_cliente, fecha_venta, descuento_global, importe_total, observaciones, estado) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlLinea = "INSERT INTO LINEAS_VENTA (id_venta, id_producto, cantidad, precio_venta, descuento_linea, importe_linea) VALUES (?, ?, ?, ?, ?, ?)";
        // SQL para actualizar el total final
        String sqlUpdateTotal = "UPDATE VENTAS SET importe_total = ? WHERE id_venta = ?";

        Connection con = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtLinea = null;
        PreparedStatement pstmtUpdate = null; // Para actualizar total
        ResultSet rsKeys = null;

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar la cabecera de la VENTA (con importe_total temporal a 0)
            //    (Los campos descuento_global, observaciones, estado se usarán)
            pstmtVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstmtVenta.setInt(1, venta.getCliente().getIdCliente());
            pstmtVenta.setString(2, venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : LocalDate.now().toString());
            pstmtVenta.setDouble(3, venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0);
            pstmtVenta.setDouble(4, 0.0); // Importe total temporal
            pstmtVenta.setString(5, venta.getObservaciones() != null ? venta.getObservaciones() : "");
            pstmtVenta.setString(6, venta.getEstado() != null ? venta.getEstado() : "COMPLETADA");

            int filasVenta = pstmtVenta.executeUpdate();
            if (filasVenta == 0) {
                throw new SQLException("No se pudo insertar la cabecera de la venta.");
            }

            // Obtener el ID generado para la venta
            rsKeys = pstmtVenta.getGeneratedKeys();
            if (!rsKeys.next()) {
                throw new SQLException("No se pudo obtener el ID generado de la venta.");
            }
            int idVentaGenerada = rsKeys.getInt(1);
            venta.setIdVenta(idVentaGenerada); // Actualizar el objeto Venta con su ID

            // 2. Insertar todas las LINEAS_VENTA asociadas y acumular total de líneas
            double totalLineasInsertadas = 0.0;
            if (venta.getLineasVenta() != null && !venta.getLineasVenta().isEmpty()) {
                pstmtLinea = con.prepareStatement(sqlLinea);
                for (LineaVenta linea : venta.getLineasVenta()) {
                    // Calcular importe_linea
                    double subtotal = (linea.getCantidad() != null ? linea.getCantidad() : 0) *
                            (linea.getPrecioVenta() != null ? linea.getPrecioVenta() : 0.0);
                    double importeLineaCalculado = subtotal - (subtotal * ((linea.getDescuento() != null ? linea.getDescuento() : 0) / 100.0));
                    linea.setImporteLinea(importeLineaCalculado); // asegurar que el objeto línea tenga el importe correcto

                    pstmtLinea.setInt(1, idVentaGenerada);
                    pstmtLinea.setInt(2, linea.getIdProducto());
                    pstmtLinea.setInt(3, linea.getCantidad());
                    pstmtLinea.setDouble(4, linea.getPrecioVenta());
                    pstmtLinea.setInt(5, linea.getDescuento());
                    pstmtLinea.setDouble(6, linea.getImporteLinea()); // usar el importe calculado

                    int filasLinea = pstmtLinea.executeUpdate();
                    if (filasLinea == 0) {
                        // si alguna inserción de línea falla, revertimos toda la transacción
                        con.rollback();
                        throw new SQLException("No se pudo insertar una línea de la venta.");
                    }
                    // getImporteLinea() devuelve un primitivo (o fue inicializado arriba), así que simplemente sumamos
                    totalLineasInsertadas += linea.getImporteLinea();
                }
            } else {
                // Si no hay líneas, la transacción debería fallar según la lógica de negocio
                con.rollback();
                throw new IllegalArgumentException("La venta debe contener al menos una línea.");
            }

            // 3. Calcular el importe final en Java (evitar depender de función en BD)
            double descuentoGlobal = venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0;
            double importeTotalFinal = totalLineasInsertadas - (totalLineasInsertadas * descuentoGlobal / 100.0);

            // 4. Actualizar el campo importe_total de la VENTA
            pstmtUpdate = con.prepareStatement(sqlUpdateTotal);
            pstmtUpdate.setDouble(1, importeTotalFinal);
            pstmtUpdate.setInt(2, idVentaGenerada);

            int filasUpdate = pstmtUpdate.executeUpdate();
            if (filasUpdate == 0) {
                con.rollback();
                throw new SQLException("No se pudo actualizar el importe total de la venta.");
            }

            // Si todo fue bien, confirmar la transacción
            con.commit();
            System.out.println("✓ Venta #" + idVentaGenerada + " registrada y total calculado y actualizado en BD.");
            return idVentaGenerada; // Devolver el ID de la venta creada

        } catch (Exception e) { // Captura SQLException y otras como IllegalArgumentException
            if (con != null) {
                try {
                    System.err.println("✗ Ocurrió un error, revirtiendo transacción...");
                    con.rollback(); // Revertir cambios si algo falló
                } catch (SQLException exRollback) {
                    System.err.println("✗ Error al intentar revertir la transacción: " + exRollback.getMessage());
                }
            }
            // Relanzar la excepción original para que la capa superior (Service/UI) se entere
            throw new Exception("Error en la transacción de inserción de venta: " + e.getMessage(), e);
        } finally {
            // Cerrar todos los recursos en orden inverso a su apertura
            try {
                if (rsKeys != null) rsKeys.close();
            } catch (SQLException ignored) {
            }
            try {
                if (pstmtUpdate != null) pstmtUpdate.close();
            } catch (SQLException ignored) {
            }
            try {
                if (pstmtLinea != null) pstmtLinea.close();
            } catch (SQLException ignored) {
            }
            try {
                if (pstmtVenta != null) pstmtVenta.close();
            } catch (SQLException ignored) {
            }
            try {
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar autocommit
                    ConexionBD.cerrarConexion(con);
                }
            } catch (SQLException ignored) {
            }
        }
    }


    /**
     * Mapea un ResultSet a un objeto Venta
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));

        int idCliente = rs.getInt("id_cliente");
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente); // Asume que clienteDAO está inicializado
        v.setCliente(cliente);

        // Asegurarse de manejar NULLs en fecha si es posible en BD
        Date fechaSql = rs.getDate("fecha_venta");
        v.setFechaVenta(fechaSql != null ? fechaSql.toLocalDate() : null);

        // Usar getObject para manejar posibles NULLs en importe_total
        v.setImporteTotal(rs.getObject("importe_total", Double.class));


        return v;
    }
}