package com.remus.dao.interfaces;

import com.remus.modelo.LineaVenta;
import java.util.List;

public interface ILineaVentaDAO {
    LineaVenta obtenerPorId(int idLinea);
    List<LineaVenta> obtenerPorVenta(int idVenta);
    boolean insertar(LineaVenta lineaVenta);
    boolean actualizar(LineaVenta lineaVenta);
    boolean eliminar(int idLinea);
    boolean eliminarPorVenta(int idVenta);
}
