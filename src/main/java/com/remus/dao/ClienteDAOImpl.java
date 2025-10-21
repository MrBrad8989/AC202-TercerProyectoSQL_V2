package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IClienteDAO;
import com.remus.modelo.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements IClienteDAO {

    private static final String SELECT_FIELDS = "id_cliente, dni, nombre, apellidos, telefono, direccion_habitual, direccion_envio, fecha_registro, activo";

    @Override
    public Cliente obtenerPorCod(int idCli) {
        String sql = "SELECT " + SELECT_FIELDS + " FROM CLIENTES WHERE id_cliente = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idCli);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> obtenerTodos() {
        List<Cliente> Clientes = new ArrayList<>();
        String sql = "SELECT " + SELECT_FIELDS + " FROM CLIENTES ORDER BY id_cliente";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Clientes.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar Clientes: " + e.getMessage(), e);
        }
        return Clientes;
    }

    @Override
    public boolean insertar(Cliente Cliente) {
        // Se añaden 'activo' y 'fecha_registro' a la inserción.
        String sql = "INSERT INTO CLIENTES (dni, nombre, apellidos, telefono, direccion_habitual, direccion_envio, activo, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, Cliente.getDni());
            pstmt.setString(2, Cliente.getNombre());
            pstmt.setString(3, Cliente.getApellidos());

            if (Cliente.getTelefono() != null) {
                pstmt.setInt(4, Cliente.getTelefono());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.setString(5, Cliente.getDireccionHabitual());
            pstmt.setString(6, Cliente.getDireccionEnvio());
            pstmt.setBoolean(7, Cliente.getActivo());
            pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        // Se añaden 'activo' y 'fecha_registro' a la actualización (aunque fecha_registro no se modifica, se debe considerar si fuera editable)
        String sql = "UPDATE CLIENTES SET NOMBRE = ?, APELLIDOS = ?, DNI = ?, TELEFONO = ?, DIRECCION_HABITUAL = ?, DIRECCION_ENVIO = ?, ACTIVO = ? WHERE ID_CLIENTE = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidos());
            pstmt.setString(3, cliente.getDni());

            // CORRECCIÓN: Manejar NULL para telefono
            if (cliente.getTelefono() != null) {
                pstmt.setInt(4, cliente.getTelefono());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.setString(5, cliente.getDireccionHabitual());
            pstmt.setString(6, cliente.getDireccionEnvio());
            pstmt.setBoolean(7, cliente.getActivo());
            pstmt.setInt(8, cliente.getIdCliente());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idCli) {
        String sql = "DELETE FROM CLIENTES WHERE ID_CLIENTE = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idCli);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar Cliente: " + e.getMessage(), e);
        }
    }

    // ... otros métodos (obtenerPorNombre, obtenerPorDNI, etc.) deberían usar SELECT_FIELDS ...

    @Override
    public Cliente obtenerPorNombre(String nombreCli) {
        String sql = "SELECT " + SELECT_FIELDS + " FROM CLIENTES WHERE NOMBRE = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, nombreCli);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Cliente obtenerPorDNI(String dniCli) {
        String sql = "SELECT " + SELECT_FIELDS + " FROM CLIENTES WHERE DNI = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, dniCli);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCliente(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar Cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> obtenerPorApellidos(String apellidos) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT " + SELECT_FIELDS + " FROM CLIENTES WHERE APELLIDOS = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, apellidos);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar Clientes: " + e.getMessage(), e);
        }
        return clientes;
    }

    @Override
    public boolean actualizarApellidos(String nombreCli, String nuevaApellidos) {
        String sql = "UPDATE CLIENTES SET APELLIDOS = ? WHERE NOMBRE = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, nuevaApellidos);
            pstmt.setString(2, nombreCli);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar apellidos: " + e.getMessage(), e);
        }
    }

    @Override
    public int contarClientes(int idCli) {
        String sql = "SELECT COUNT(*) as total FROM CLIENTES WHERE ID_CLIENTE = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idCli);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar clientes: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        // CORRECCIÓN: Se añaden los campos fecha_registro y activo.
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getObject("telefono", Integer.class), // Usar getObject para manejar NULL
                rs.getString("direccion_habitual"),
                rs.getString("direccion_envio"),
                // Mapeo de fecha_registro y activo
                rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null,
                rs.getBoolean("activo")
        );
    }
}