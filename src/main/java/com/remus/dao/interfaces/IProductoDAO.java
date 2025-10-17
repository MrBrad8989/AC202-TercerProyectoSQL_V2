package com.remus.dao.interfaces;

import com.remus.modelo.Producto;
import java.util.List;

public interface IProductoDAO {
    Producto obtenerPorId(int idProducto);
    Producto obtenerPorCodigo(String codigo);
    List<Producto> obtenerTodos();
    List<Producto> obtenerActivos();
    boolean insertar(Producto producto);
    boolean actualizar(Producto producto);
    boolean eliminar(int idProducto);
    boolean actualizarStock(int idProducto, int nuevoStock);
    List<Producto> obtenerBajoStock();
}

