package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.ILineaVentaDAO;
import com.remus.modelo.LineaVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LineaVentaDAOImpl implements ILineaVentaDAO {

    @Override
    public LineaVenta obtenerPorId(int idLinea) {
        String sql = "SELECT * FROM LINEAS_VENTA WHERE id_linea = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idLinea);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearLineaVenta(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener línea de venta por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LineaVenta> obtenerPorVenta(int idVenta) {
        List<LineaVenta> lineas = new ArrayList<>();
        String sql = "SELECT * FROM LINEAS_VENTA WHERE id_venta = ? ORDER BY id_linea";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lineas.add(mapearLineaVenta(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener líneas de venta por venta: " + e.getMessage(), e);
        }
        return lineas;
    }

    @Override
    public boolean insertar(LineaVenta lineaVenta) {
        // Usar la columna correcta descuento_linea
        String sql = "INSERT INTO LINEAS_VENTA (id_venta, id_producto, cantidad, precio_venta, descuento_linea, importe_linea) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, lineaVenta.getIdVenta());
            pstmt.setInt(2, lineaVenta.getIdProducto());
            pstmt.setInt(3, lineaVenta.getCantidad());
            pstmt.setDouble(4, lineaVenta.getPrecioVenta());
            pstmt.setInt(5, lineaVenta.getDescuento());
            pstmt.setDouble(6, lineaVenta.getImporteLinea());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar línea de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(LineaVenta lineaVenta) {
        // Usar la columna correcta descuento_linea
        String sql = "UPDATE LINEAS_VENTA SET id_venta = ?, id_producto = ?, cantidad = ?, precio_venta = ?, descuento_linea = ?, importe_linea = ? " +
                "WHERE id_linea = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, lineaVenta.getIdVenta());
            pstmt.setInt(2, lineaVenta.getIdProducto());
            pstmt.setInt(3, lineaVenta.getCantidad());
            pstmt.setDouble(4, lineaVenta.getPrecioVenta());
            pstmt.setInt(5, lineaVenta.getDescuento());
            pstmt.setDouble(6, lineaVenta.getImporteLinea());
            pstmt.setInt(7, lineaVenta.getIdLinea());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar línea de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idLinea) {
        String sql = "DELETE FROM LINEAS_VENTA WHERE id_linea = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idLinea);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar línea de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminarPorVenta(int idVenta) {
        String sql = "DELETE FROM LINEAS_VENTA WHERE id_venta = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            int filas = pstmt.executeUpdate();
            return filas >= 0; // true incluso si no hay líneas
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar líneas de la venta: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un registro de la tabla LINEAS_VENTA a un objeto LineaVenta
     */
    private LineaVenta mapearLineaVenta(ResultSet rs) throws SQLException {
        LineaVenta lv = new LineaVenta();
        lv.setIdLinea(rs.getInt("id_linea"));
        lv.setIdVenta(rs.getInt("id_venta"));
        lv.setIdProducto(rs.getInt("id_producto"));
        lv.setCantidad(rs.getInt("cantidad"));
        lv.setPrecioVenta(rs.getDouble("precio_venta"));
        lv.setDescuento(rs.getInt("descuento_linea"));
        lv.setImporteLinea(rs.getDouble("importe_linea"));
        return lv;
    }
}
