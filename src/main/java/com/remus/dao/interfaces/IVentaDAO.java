package com.remus.dao.interfaces;

import com.remus.modelo.Venta;
import java.util.List;

public interface IVentaDAO {
    Venta obtenerPorId(int idVenta);
    List<Venta> obtenerTodas();
    List<Venta> obtenerPorCliente(int idCliente);
    boolean insertar(Venta venta);
    boolean actualizar(Venta venta);
    boolean eliminar(int idVenta);
    boolean actualizarEstado(int idVenta, String nuevoEstado);
    double calcularImporteTotal(int idVenta);
}
