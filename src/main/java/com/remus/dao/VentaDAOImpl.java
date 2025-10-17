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
        String sql = "SELECT * FROM VENTAS WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);
                venta.setLineasVenta(lineaVentaDAO.obtenerPorVenta(idVenta)); // obtener las líneas asociadas
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
        String sql = "SELECT * FROM VENTAS ORDER BY fecha_venta DESC";

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
        String sql = "SELECT * FROM VENTAS WHERE id_cliente = ? ORDER BY fecha_venta DESC";

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
        String sql = "INSERT INTO VENTAS (id_cliente, fecha_venta, descuento_global, importe_total, observaciones, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, venta.getCliente().getIdCliente());
            pstmt.setString(2, venta.getFechaVenta().toString());
            pstmt.setDouble(3, venta.getDescuentoGlobal());
            pstmt.setDouble(4, venta.getImporteTotal());
            pstmt.setString(5, venta.getObservaciones());
            pstmt.setString(6, venta.getEstado());

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
        String sql = "UPDATE VENTAS SET id_cliente = ?, fecha_venta = ?, descuento_global = ?, importe_total = ?, observaciones = ?, estado = ? " +
                "WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, venta.getCliente().getIdCliente());
            pstmt.setString(2, venta.getFechaVenta().toString());
            pstmt.setDouble(3, venta.getDescuentoGlobal());
            pstmt.setDouble(4, venta.getImporteTotal());
            pstmt.setString(5, venta.getObservaciones());
            pstmt.setString(6, venta.getEstado());
            pstmt.setInt(7, venta.getIdVenta());

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

    @Override
    public boolean actualizarEstado(int idVenta, String nuevoEstado) {
        String sql = "UPDATE VENTAS SET estado = ? WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idVenta);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de la venta: " + e.getMessage(), e);
        }
    }

    @Override
    public double calcularImporteTotal(int idVenta) {
        String sql = "SELECT SUM(importe_linea) as total FROM LINEAS_VENTA WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                // actualizar en la tabla ventas
                String update = "UPDATE VENTAS SET importe_total = ? WHERE id_venta = ?";
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

    /**
     * Mapea un ResultSet a un objeto Venta
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));

        int idCliente = rs.getInt("id_cliente");
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente);
        v.setCliente(cliente);

        v.setFechaVenta(LocalDate.parse(rs.getString("fecha_venta")));
        v.setDescuentoGlobal(rs.getDouble("descuento_global"));
        v.setImporteTotal(rs.getDouble("importe_total"));
        v.setObservaciones(rs.getString("observaciones"));
        v.setEstado(rs.getString("estado"));

        return v;
    }
}
