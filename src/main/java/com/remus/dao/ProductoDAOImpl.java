package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IProductoDAO;
import com.remus.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements IProductoDAO {

    // Se asume el esquema MySQL con la columna 'stock' (existencias) añadida
    // Columnas: id_producto, codigo, descripcion, precio_recomendado, stock, stock_minimc, activo, fecha_creacion
    private static final String SELECT_FIELDS = "id_producto, codigo, descripcion, precio_recomendado, stock, stock_minimc, activo, fecha_creacion";

    @Override
    public Producto obtenerPorId(int idProducto) {
        String sql = "SELECT " + SELECT_FIELDS + " FROM PRODUCTOS WHERE id_producto = ?";

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
        String sql = "SELECT " + SELECT_FIELDS + " FROM PRODUCTOS WHERE codigo = ?";

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
        String sql = "SELECT " + SELECT_FIELDS + " FROM PRODUCTOS ORDER BY id_producto";

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
        String sql = "SELECT " + SELECT_FIELDS + " FROM PRODUCTOS WHERE activo = 1 ORDER BY descripcion";

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
        // Se añade la columna 'stock' a la inserción
        String sql = "INSERT INTO PRODUCTOS (codigo, descripcion, precio_recomendado, stock, stock_minimc, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioRecomendado());
            pstmt.setInt(4, producto.getStock()); // Nuevo campo
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setBoolean(6, producto.getActivo());
            pstmt.setString(7, producto.getFechaCreacion()); // Si es null, MySQL lo gestionará si tiene DEFAULT

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        // Se añade la columna 'stock' a la actualización
        String sql = "UPDATE PRODUCTOS SET codigo = ?, descripcion = ?, precio_recomendado = ?, stock = ?, stock_minimc = ?, activo = ?, fecha_creacion = ? " +
                "WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioRecomendado());
            pstmt.setInt(4, producto.getStock());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setBoolean(6, producto.getActivo());
            pstmt.setString(7, producto.getFechaCreacion());
            pstmt.setInt(8, producto.getIdProducto());

            boolean actualizado = pstmt.executeUpdate() > 0;

            if (actualizado) {
                // Actualizar ventas relacionadas
                String sqlVentas = "UPDATE VENTAS v " +
                        "JOIN LINEAS_VENTA lv ON v.id_venta = lv.id_venta " +
                        "SET v.importe_total = (SELECT SUM(lv.cantidad * lv.precio_venta) FROM LINEAS_VENTA lv WHERE lv.id_venta = v.id_venta) " +
                        "WHERE lv.id_producto = ?";

                try (PreparedStatement pstmtVentas = ConexionBD.getConexion().prepareStatement(sqlVentas)) {
                    pstmtVentas.setInt(1, producto.getIdProducto());
                    pstmtVentas.executeUpdate();
                }
            }

            return actualizado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto y ventas relacionadas: " + e.getMessage(), e);
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
        // CORRECCIÓN CRÍTICA: Se actualiza la columna 'stock' real, no 'stock_minimc'
        String sql = "UPDATE PRODUCTOS SET stock = ? WHERE id_producto = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar stock: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un registro de la tabla productos a un objeto Producto
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioRecomendado(rs.getDouble("precio_recomendado"));
        p.setStock(rs.getInt("stock")); // Nuevo campo a mapear
        p.setStockMinimo(rs.getInt("stock_minimc"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaCreacion(rs.getString("fecha_creacion"));
        return p;
    }
}