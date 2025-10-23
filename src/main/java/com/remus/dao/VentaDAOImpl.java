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

        Connection con = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtLinea = null;
        ResultSet rs = null;

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            // Calcular importes de líneas y total
            double totalLineas = 0.0;
            if (venta.getLineasVenta() != null) {
                for (LineaVenta linea : venta.getLineasVenta()) {
                    int cantidad = linea.getCantidad() != null ? linea.getCantidad() : 0;
                    double precio = linea.getPrecioVenta() != null ? linea.getPrecioVenta() : 0.0;
                    int desc = linea.getDescuento() != null ? linea.getDescuento() : 0;
                    double subtotal = cantidad * precio;
                    double importeLinea = subtotal - (subtotal * desc / 100.0);
                    linea.setImporteLinea(importeLinea);
                    totalLineas += importeLinea;
                }
            }

            double descuentoGlobal = venta.getDescuentoGlobal() != null ? venta.getDescuentoGlobal() : 0.0;
            double totalConDescuento = totalLineas - (totalLineas * descuentoGlobal / 100.0);

            // Insertar venta con el importe total calculado
            pstmtVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstmtVenta.setInt(1, venta.getCliente().getIdCliente());
            pstmtVenta.setString(2, venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : null);
            pstmtVenta.setDouble(3, descuentoGlobal);
            pstmtVenta.setDouble(4, totalConDescuento);
            pstmtVenta.setString(5, venta.getObservaciones() != null ? venta.getObservaciones() : "");
            pstmtVenta.setString(6, venta.getEstado() != null ? venta.getEstado() : "COMPLETADA");

            int filas = pstmtVenta.executeUpdate();
            if (filas == 0) {
                con.rollback();
                throw new SQLException("No se pudo insertar la venta");
            }

            rs = pstmtVenta.getGeneratedKeys();
            if (!rs.next()) {
                con.rollback();
                throw new SQLException("No se pudo obtener el ID generado de la venta");
            }
            int idVentaGenerada = rs.getInt(1);
            venta.setIdVenta(idVentaGenerada);

            // Insertar líneas
            if (venta.getLineasVenta() != null) {
                pstmtLinea = con.prepareStatement(sqlLinea);
                for (LineaVenta linea : venta.getLineasVenta()) {
                    pstmtLinea.setInt(1, idVentaGenerada);
                    pstmtLinea.setInt(2, linea.getIdProducto());
                    pstmtLinea.setInt(3, linea.getCantidad());
                    pstmtLinea.setDouble(4, linea.getPrecioVenta());
                    pstmtLinea.setInt(5, linea.getDescuento());
                    pstmtLinea.setDouble(6, linea.getImporteLinea());
                    pstmtLinea.executeUpdate();
                }
            }

            con.commit();
            return idVentaGenerada;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    // ignorar
                }
            }
            throw new Exception("Error al insertar venta con líneas: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (pstmtLinea != null) pstmtLinea.close(); } catch (SQLException ignored) {}
            try { if (pstmtVenta != null) pstmtVenta.close(); } catch (SQLException ignored) {}
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Mapea un ResultSet a un objeto Venta
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));

        int idCliente = rs.getInt("id_cliente");
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente);
        v.setCliente(cliente);

        v.setFechaVenta(rs.getDate("fecha_venta").toLocalDate());
        v.setImporteTotal(rs.getDouble("importe_total"));

        return v;
    }
}

