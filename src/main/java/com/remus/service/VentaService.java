package com.remus.service;

import com.remus.dao.*;
import com.remus.dao.interfaces.*;
import com.remus.modelo.*;

/**
 * Servicio de negocio para gestionar ventas con validaciones y transacciones
 */
public class VentaService {

    private final IVentaDAO ventaDAO = new VentaDAOImpl();
    private final IProductoDAO productoDAO = new ProductoDAOImpl();
    private final IClienteDAO clienteDAO = new ClienteDAOImpl();

    private static final double RANGO_PRECIO = 0.20;

    public void validarClienteExistente(int idCliente) throws IllegalArgumentException {
        Cliente cliente = clienteDAO.obtenerPorCod(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente con ID " + idCliente + " no existe");
        }
    }

    public void validarProductoExistente(int idProducto) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
        }
    }

    public void validarCantidad(int cantidad) throws IllegalArgumentException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }

    public void validarStock(int idProducto, int cantidad) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        if (!producto.hayStock(cantidad)) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Stock disponible: " + producto.getStock() +
                            ", cantidad solicitada: " + cantidad);
        }
    }

    public void validarPrecioVenta(int idProducto, double precioVenta) throws IllegalArgumentException {
        Producto producto = productoDAO.obtenerPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        if (producto.getPrecioRecomendado() == null) {
            throw new IllegalArgumentException("El producto no tiene un precio recomendado definido");
        }

        double precioRecomendado = producto.getPrecioRecomendado();
        double minimo = precioRecomendado * (1 - RANGO_PRECIO);
        double maximo = precioRecomendado * (1 + RANGO_PRECIO);

        if (precioVenta < minimo || precioVenta > maximo) {
            throw new IllegalArgumentException(
                    String.format("Precio fuera de rango. Rango permitido: %.2f€ - %.2f€ (±20%% de %.2f€)",
                            minimo, maximo, precioRecomendado));
        }
    }

    public void validarDescuento(int descuento) throws IllegalArgumentException {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
    }

    public double calcularImporteLinea(int cantidad, double precioVenta, int descuento) {
        double subtotal = cantidad * precioVenta;
        double descuentoAplicado = subtotal * (descuento / 100.0);
        return subtotal - descuentoAplicado;
    }

    /**
     * Inserta una venta completa con sus líneas (VALIDACIONES EN SERVICE, SQL EN DAO)
     */
    public int insertarVentaConTransaccion(Venta venta) throws Exception {
        // Validaciones de negocio
        validarClienteExistente(venta.getCliente().getIdCliente());

        if (venta.getLineasVenta() == null || venta.getLineasVenta().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos una línea");
        }

        for (LineaVenta linea : venta.getLineasVenta()) {
            validarProductoExistente(linea.getIdProducto());
            validarCantidad(linea.getCantidad());
            validarStock(linea.getIdProducto(), linea.getCantidad());
            validarPrecioVenta(linea.getIdProducto(), linea.getPrecioVenta());
            validarDescuento(linea.getDescuento());
        }

        // Delegar la inserción transaccional al DAO (que realiza INSERT venta + líneas y retorna id)
        int idGenerado = ventaDAO.insertarConLineas(venta);
        System.out.println("✓ Venta registrada con ID: " + idGenerado);
        return idGenerado;
    }

    public double obtenerPrecioRecomendado(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getPrecioRecomendado() : 0.0;
    }

    public int obtenerStockProducto(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        return p != null ? p.getStock() : 0;
    }

    public double[] obtenerRangoPrecio(int idProducto) {
        Producto p = productoDAO.obtenerPorId(idProducto);
        if (p != null) {
            double recomendado = p.getPrecioRecomendado();
            return new double[]{
                    recomendado * (1 - RANGO_PRECIO),
                    recomendado * (1 + RANGO_PRECIO)
            };
        }
        return new double[]{0, 0};
    }
}
