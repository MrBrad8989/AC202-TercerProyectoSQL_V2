package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IProductoDAO;
import com.remus.modelo.Producto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements IProductoDAO {

    @Override
    public Producto obtenerPorId(int idProducto) {
        String sql = "SELECT * FROM PRODUCTOS WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener producto por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Producto obtenerPorCodigo(String codigo) {
        String sql = "SELECT * FROM PRODUCTOS WHERE codigo = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener producto por código: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS ORDER BY id_producto";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return productos;
    }

    @Override
    public List<Producto> obtenerActivos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS WHERE activo = 1 ORDER BY descripcion";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos activos: " + e.getMessage(), e);
        }
        return productos;
    }

    @Override
    public boolean insertar(Producto producto) {
        String sql = "INSERT INTO PRODUCTOS (codigo, descripcion, precio_recomendado, stock, stock_minimo, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioRecomendado());
            pstmt.setInt(4, producto.getStock());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setBoolean(6, producto.getActivo() != null ? producto.getActivo() : true);
            pstmt.setString(7, LocalDateTime.now().toString());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE PRODUCTOS SET codigo = ?, descripcion = ?, precio_recomendado = ?, stock = ?, stock_minimo = ?, activo = ? " +
                "WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioRecomendado());
            pstmt.setInt(4, producto.getStock());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setBoolean(6, producto.getActivo());
            pstmt.setInt(7, producto.getIdProducto());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        String sql = "DELETE FROM PRODUCTOS WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        String sql = "UPDATE PRODUCTOS SET stock = ? WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar stock: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> obtenerBajoStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS WHERE stock < stock_minimo ORDER BY stock ASC";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener productos con stock bajo: " + e.getMessage(), e);
        }
        return productos;
    }

    /**
     * Mapea un registro de la tabla PRODUCTOS a un objeto Producto
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioRecomendado(rs.getDouble("precio_recomendado"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setActivo(rs.getBoolean("activo"));

        String fecha = rs.getString("fecha_creacion");
        if (fecha != null) {
            p.setFechaCreacion(fecha);
        }

        return p;
    }
}
