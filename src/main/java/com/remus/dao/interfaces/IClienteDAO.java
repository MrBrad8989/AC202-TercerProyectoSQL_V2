package com.remus.dao.interfaces;

import com.remus.modelo.Cliente;

import java.util.List;

public interface IClienteDAO {
    /**
     * Obtiene un Cliente por su código
     */
    Cliente obtenerPorCod(int idCod);

    /**
     * Obtiene todos los Clientes
     */
    List<Cliente> obtenerTodos();

    /**
     * Inserta un nuevo Cliente en la base de datos
     */
    boolean insertar(Cliente Cliente);

    /**
     * Actualiza los datos de un Cliente existente
     */
    boolean actualizar(Cliente Cliente);

    /**
     * Elimina un Cliente por su código
     */
    boolean eliminar(int idCli);

    // ========== CONSULTAS ESPECÍFICAS ==========

    /**
     * Obtiene un Cliente por su nombre
     */
    Cliente obtenerPorNombre(String nombreCli);


    /*
    Obtener por DNI
     */
    Cliente obtenerPorDNI(String dniCli);


    /**
     * Obtiene Clientes por localidad
     */
    List<Cliente> obtenerPorApellidos(String apellidos);

    /**
     * Actualiza la localidad de un Cliente específico
     */
    boolean actualizarApellidos(String nombreCli, String nuevoApellido);

    /**
     * Obtiene el número de empleados por Cliente
     */
    int contarClientes(int idCli);
}

