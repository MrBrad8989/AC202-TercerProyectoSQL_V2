package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IClienteDAO;
import com.remus.modelo.Cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements IClienteDAO {

        public Cliente obtenerPorCod(int idCli) {
            String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo FROM CLIENTES WHERE id_cliente = ?";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setLong(1, idCli);
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
            String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo FROM CLIENTES ORDER BY id_cliente";

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
            String sql = "INSERT INTO CLIENTES (id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, TRUE)";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setLong(1, Cliente.getIdCliente());
                pstmt.setString(2, Cliente.getNombre());
                pstmt.setString(3, Cliente.getApellidos());
                pstmt.setString(4, Cliente.getDni());
                pstmt.setInt(5, Cliente.getTelefono());
                pstmt.setString(6, Cliente.getDireccionHabitual());
                pstmt.setString(7, Cliente.getDireccionEnvio());

                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar Cliente: " + e.getMessage(), e);
            }
        }

        @Override
        public boolean actualizar(Cliente Cliente) {
            String sql = "UPDATE CLIENTES SET nombre = ?, apellidos = ?, dni = ?, telefono = ?, direccion_habitual = ?, direccion_envio = ?  WHERE id_cliente = ?";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setString(1, Cliente.getNombre());
                pstmt.setString(2, Cliente.getApellidos());
                pstmt.setString(3, Cliente.getDni());
                pstmt.setInt(4, Cliente.getTelefono());
                pstmt.setString(5, Cliente.getDireccionHabitual());
                pstmt.setString(6, Cliente.getDireccionEnvio());
                pstmt.setLong(7, Cliente.getIdCliente());

                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar Cliente: " + e.getMessage(), e);
            }
        }

        @Override
        public boolean eliminar(int idCli) {
            String sql = "DELETE FROM CLIENTES WHERE id_cliente = ?";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setInt(1, idCli);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Error al eliminar Cliente: " + e.getMessage(), e);
            }
        }

        @Override
        public Cliente obtenerPorNombre(String nombreCli) {
            String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo FROM CLIENTES WHERE nombre = ?";

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
        String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo FROM CLIENTES WHERE dni = ?";

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
            List<Cliente> Clientes = new ArrayList<>();
            String sql = "SELECT id_cliente, nombre, apellidos, dni, telefono, direccion_habitual, direccion_envio, fecha_registro, activo FROM CLIENTES WHERE apellidos = ?";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setString(1, apellidos);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Clientes.add(mapearCliente(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al buscar Clientes: " + e.getMessage(), e);
            }
            return Clientes;
        }

        @Override
        public boolean actualizarApellidos(String nombreCli, String nuevaApellidos) {
            String sql = "UPDATE CLIENTES SET apellidos = ? WHERE nombre = ?";

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
            String sql = "SELECT COUNT(*) as total FROM CLIENTES WHERE id_cliente = ?";

            try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
                pstmt.setInt(1, idCli);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException("Error al contar empleados: " + e.getMessage(), e);
            }
        }

        /**
         * Mapea un ResultSet a un objeto Cliente
         */
        private Cliente mapearCliente(ResultSet rs) throws SQLException {
            return new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("dni"),
                    rs.getInt("telefono"),
                    rs.getString("direccion_habitual"),
                    rs.getString("direccion_envio"),
                    rs.getTimestamp("fecha_registro").toLocalDateTime(),
                    rs.getBoolean("activo")
            );
        }
    }