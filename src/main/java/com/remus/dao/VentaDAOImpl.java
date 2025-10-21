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
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM ventas WHERE id_venta = ?";

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
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM ventas ORDER BY fecha_venta DESC";

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
        String sql = "SELECT id_venta, id_cliente, fecha_venta, importe_total FROM ventas WHERE id_cliente = ? ORDER BY fecha_venta DESC";

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
        String sql = "INSERT INTO ventas (id_cliente, fecha_venta, importe_total) VALUES (?, ?, ?)";

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
        String sql = "UPDATE ventas SET id_cliente = ?, fecha_venta = ?, importe_total = ? WHERE id_venta = ?";

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
        String sql = "DELETE FROM ventas WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);

            // eliminar líneas asociadas primero
            lineaVentaDAO.eliminarPorVenta(idVenta);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar venta: " + e.getMessage(), e);
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
