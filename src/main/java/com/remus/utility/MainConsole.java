package com.remus.utility;

import com.remus.dao.ClienteDAOImpl;
import com.remus.dao.EmpresaDAOImpl;
import com.remus.dao.ProductoDAOImpl;
import com.remus.dao.VentaDAOImpl;
import com.remus.dao.interfaces.IClienteDAO;
import com.remus.dao.interfaces.IEmpresaDAO;
import com.remus.dao.interfaces.IProductoDAO;
import com.remus.dao.interfaces.IVentaDAO;
import com.remus.modelo.Cliente;
import com.remus.modelo.Empresa;
import com.remus.modelo.Producto;
import com.remus.modelo.Venta;

import java.util.List;
import java.util.Scanner;

public class MainConsole {
    private static final Scanner sc = new Scanner(System.in);
    private static final IClienteDAO clienteDAO = new ClienteDAOImpl();
    private static final IEmpresaDAO empresaDAO = new EmpresaDAOImpl();
    private static final IProductoDAO productoDAO = new ProductoDAOImpl();
    private static final IVentaDAO ventaDAO = new VentaDAOImpl();

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerInt("Elige una opción: ");
            switch (opcion) {
                case 1:
                    listarClientes();
                    break;
                case 2:
                    insertarCliente();
                    break;
                case 3:
                    listarEmpresas();
                    break;
                case 4:
                    insertarEmpresa();
                    break;
                case 5:
                    listarProductos();
                    break;
                case 6:
                    insertarProducto();
                    break;
                case 7:
                    listarVentas();
                    break;
                case 0:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // ---------------- MENÚ PRINCIPAL ----------------
    private static void mostrarMenu() {
        System.out.println("\n====== MENÚ PRINCIPAL ======");
        System.out.println("1. Listar clientes");
        System.out.println("2. Insertar cliente");
        System.out.println("3. Listar empresas");
        System.out.println("4. Insertar empresa");
        System.out.println("5. Listar productos");
        System.out.println("6. Insertar producto");
        System.out.println("7. Listar ventas");
        System.out.println("0. Salir");
        System.out.println("============================");
    }

    // ---------------- CLIENTES ----------------
    private static void listarClientes() {
        List<Cliente> clientes = clienteDAO.obtenerTodos();
        if (clientes == null || clientes.isEmpty()) System.out.println("No hay clientes registrados.");
        else clientes.forEach(Cliente::mostrar);
    }

    private static void insertarCliente() {
        String nombre = leerTexto("Nombre: ");
        String apellidos = leerTexto("Apellidos: ");
        String dni = leerTexto("DNI: ");
        String telefonoStr = leerTexto("Teléfono (números, opcional): ");
        Integer telefono = null;
        if (!telefonoStr.trim().isEmpty()) {
            try { telefono = Integer.parseInt(telefonoStr.trim()); } catch (NumberFormatException ignored) {}
        }
        String dirHab = leerTexto("Dirección habitual: ");
        String dirEnv = leerTexto("Dirección envío: ");

        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setApellidos(apellidos);
        c.setDni(dni);
        c.setTelefono(telefono);
        c.setDireccionHabitual(dirHab);
        c.setDireccionEnvio(dirEnv);

        if (clienteDAO.insertar(c)) System.out.println("✅ Cliente insertado correctamente.");
        else System.out.println("❌ No se pudo insertar el cliente.");
    }

    // ---------------- EMPRESAS ----------------
    private static void listarEmpresas() {
        List<Empresa> empresas = empresaDAO.obtenerTodas();
        if (empresas == null || empresas.isEmpty()) System.out.println("No hay empresas registradas.");
        else empresas.forEach(e -> System.out.println(e.toString()));
    }

    private static void insertarEmpresa() {
        String cif = leerTexto("CIF: ");
        String nombre = leerTexto("Nombre: ");
        String domicilio = leerTexto("Domicilio: ");
        String localidad = leerTexto("Localidad: ");

        Empresa e = new Empresa();
        e.setCif(cif);
        e.setNombre(nombre);
        e.setDomicilio(domicilio);
        e.setLocalidad(localidad);

        if (empresaDAO.insertar(e)) System.out.println("✅ Empresa insertada correctamente.");
        else System.out.println("❌ No se pudo insertar la empresa.");
    }

    // ---------------- PRODUCTOS ----------------
    private static void listarProductos() {
        List<Producto> productos = productoDAO.obtenerTodos();
        if (productos == null || productos.isEmpty()) System.out.println("No hay productos registrados.");
        else productos.forEach(Producto::mostrar);
    }

    private static void insertarProducto() {
        String codigo = leerTexto("Código: ");
        String descripcion = leerTexto("Descripción: ");
        double precio = leerDouble("Precio recomendado: ");
        int stock = leerInt("Stock inicial: ");
        int stockMinimo = leerInt("Stock mínimo: ");

        Producto p = new Producto(codigo, descripcion, precio, stock, stockMinimo);
        if (productoDAO.insertar(p)) System.out.println("✅ Producto insertado correctamente.");
        else System.out.println("❌ No se pudo insertar el producto.");
    }

    // ---------------- VENTAS ----------------
    private static void listarVentas() {
        List<Venta> ventas = ventaDAO.obtenerTodas();
        if (ventas == null || ventas.isEmpty()) System.out.println("No hay ventas registradas.");
        else ventas.forEach(Venta::mostrar);
    }

    // ---------------- MÉTODOS AUXILIARES ----------------
    private static String leerTexto(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    private static int leerInt(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un número válido.");
            }
        }
    }

    private static double leerDouble(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un número válido (decimal).");
            }
        }
    }
}
