package com.remus.dao;

import com.remus.connection.ConexionBD;
import com.remus.dao.interfaces.IEmpresaDAO;
import com.remus.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAOImpl implements IEmpresaDAO {

    @Override
    public Empresa obtenerPorId(int idEmpresa) {
        String sql = "SELECT id_empresa, cif, nombre, domicilio, localidad, logo, color_principal FROM empresa WHERE id_empresa = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idEmpresa);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearEmpresa(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener Empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Empresa> obtenerTodas() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT id_empresa, cif, nombre, domicilio, localidad, logo, color_principal FROM empresa ORDER BY id_empresa";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                empresas.add(mapearEmpresa(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar Empresas: " + e.getMessage(), e);
        }
        return empresas;
    }

    @Override
    public boolean insertar(Empresa empresa) {
        String sql = "INSERT INTO empresa (cif, nombre, domicilio, localidad, logo, color_principal) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, empresa.getCif());
            pstmt.setString(2, empresa.getNombre());
            pstmt.setString(3, empresa.getDomicilio());
            pstmt.setString(4, empresa.getLocalidad());
            pstmt.setString(5, empresa.getLogoPath());
            pstmt.setString(6, empresa.getColorPrincipal());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar Empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET cif = ?, nombre = ?, domicilio = ?, localidad = ?, logo = ?, color_principal = ? WHERE id_empresa = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, empresa.getCif());
            pstmt.setString(2, empresa.getNombre());
            pstmt.setString(3, empresa.getDomicilio());
            pstmt.setString(4, empresa.getLocalidad());
            pstmt.setString(5, empresa.getLogoPath());
            pstmt.setString(6, empresa.getColorPrincipal());
            pstmt.setInt(7, empresa.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar Empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idEmpresa) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setInt(1, idEmpresa);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar Empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public Empresa obtenerPorCif(String cif) {
        String sql = "SELECT id_empresa, cif, nombre, domicilio, localidad, logo, color_principal FROM empresa WHERE cif = ?";

        try (PreparedStatement pstmt = ConexionBD.getConexion().prepareStatement(sql)) {
            pstmt.setString(1, cif);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearEmpresa(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener Empresa por CIF: " + e.getMessage(), e);
        }
    }

    @Override
    public int contarEmpresas() {
        String sql = "SELECT COUNT(*) AS total FROM empresa";

        try (Statement stmt = ConexionBD.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar Empresas: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto Empresa
     */
    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        return new Empresa(
            rs.getInt("id_empresa"),
            rs.getString("cif"),
            rs.getString("nombre"),
            rs.getString("domicilio"),
            rs.getString("localidad"),
            rs.getString("logo"),
            rs.getString("color_principal")
        );
    }
}
