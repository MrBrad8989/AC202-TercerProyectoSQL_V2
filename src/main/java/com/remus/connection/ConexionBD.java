package com.remus.connection;

import java.sql.*;

/**
 * Clase para gestionar la conexión a la base de datos SQLite
 */
public class ConexionBD {

    // Ruta de la base de datos SQLite
    private static final String DB_PATH = "AC202.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    // Pool simple de conexión (singleton)
    private static Connection conexionActual = null;

    /**
     * Obtiene una conexión a la base de datos SQLite
     * @return Connection objeto de conexión
     * @throws SQLException si hay error en la conexión
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Cargar el driver de SQLite (opcional en versiones nuevas de JDBC)
            Class.forName("org.sqlite.JDBC");

            // Si no hay conexión o está cerrada, crear nueva
            if (conexionActual == null || conexionActual.isClosed()) {
                conexionActual = DriverManager.getConnection(URL);
                // Habilitar foreign keys en SQLite
                try (Statement stmt = conexionActual.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                System.out.println("✓ Conexión establecida a: " + DB_PATH);
            }

            return conexionActual;

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite no encontrado: " + e.getMessage(), e);
        }
    }

    /**
     * Cierra una conexión de forma segura
     * @param conn Conexión a cerrar
     */
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("✓ Conexión cerrada");
                }
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra un ResultSet de forma segura
     * @param rs ResultSet a cerrar
     */
    public static void cerrarResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar ResultSet: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra un Statement de forma segura
     * @param stmt Statement a cerrar
     */
    public static void cerrarStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar Statement: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra un PreparedStatement de forma segura
     * @param pstmt PreparedStatement a cerrar
     */
    public static void cerrarPreparedStatement(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar PreparedStatement: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si la conexión está activa
     * @return true si está activa, false en caso contrario
     */
    public static boolean isConexionActiva() {
        try {
            return conexionActual != null && !conexionActual.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Obtiene información sobre la base de datos
     */
    public static void mostrarInfoBD() {
        try (Connection conn = getConexion();
             Statement stmt = conn.createStatement()) {

            System.out.println("\n=== INFORMACIÓN DE LA BASE DE DATOS ===");
            System.out.println("Base de datos: " + DB_PATH);

            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Driver: " + metaData.getDriverName());
            System.out.println("Versión: " + metaData.getDriverVersion());

            // Contar registros en las tablas principales
            String[] tablas = {"EMPRESA", "CLIENTES", "PRODUCTOS", "VENTAS", "LINEAS_VENTA"};
            for (String tabla : tablas) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM " + tabla)) {
                    if (rs.next()) {
                        System.out.printf("Total en %s: %d%n", tabla, rs.getInt("total"));
                    }
                }
            }
            System.out.println("======================================\n");

        } catch (SQLException e) {
            System.err.println("Error al obtener información de BD: " + e.getMessage());
        }
    }

    /**
     * Ejecuta el script de creación de la base de datos
     * NOTA: Este método es útil para inicializar la BD desde Java
     */
    public static void inicializarBD(String rutaScript) {
        try (Connection conn = getConexion();
             Statement stmt = conn.createStatement()) {

            // Leer y ejecutar el script SQL
            java.nio.file.Path path = java.nio.file.Paths.get(rutaScript);
            String script = new String(java.nio.file.Files.readAllBytes(path));

            // Dividir en sentencias individuales (básico)
            String[] sentencias = script.split(";");

            for (String sentencia : sentencias) {
                sentencia = sentencia.trim();
                if (!sentencia.isEmpty() && !sentencia.startsWith("--")) {
                    stmt.execute(sentencia);
                }
            }

            System.out.println("✓ Base de datos inicializada correctamente");

        } catch (Exception e) {
            System.err.println("✗ Error al inicializar BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test de conexión
     */
    public static void main(String[] args) {
        try {
            System.out.println("Probando conexión a SQLite...");
            Connection conn = ConexionBD.getConexion();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Conexión exitosa!");

                // Mostrar info
                mostrarInfoBD();

                // Cerrar
                cerrarConexion(conn);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }
}