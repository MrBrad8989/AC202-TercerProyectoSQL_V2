package com.remus.service;

import com.remus.dao.interfaces.IClienteDAO;
import com.remus.dao.ClienteDAOImpl;
import com.remus.modelo.Cliente;

public class ClienteValidar {

    private static final IClienteDAO clienteDAO = new ClienteDAOImpl();

    // Validar ID positivo
    public static void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser mayor que 0");
        }
    }

    // Validar nombre obligatorio, máximo 30 caracteres
    public static void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (nombre.length() > 30) {
            throw new IllegalArgumentException("El nombre del cliente no puede tener más de 30 caracteres");
        }
    }

    // Validar apellidos obligatorios, máximo 50 caracteres
    public static void validarApellidos(String apellidos) {
        if (apellidos == null || apellidos.trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos del cliente son obligatorios");
        }
        if (apellidos.length() > 50) {
            throw new IllegalArgumentException("Los apellidos del cliente no pueden tener más de 50 caracteres");
        }
    }

    // Validar email con formato correcto (básico)
    public static void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido");
        }
        if (email.length() > 50) {
            throw new IllegalArgumentException("El correo electrónico no puede tener más de 50 caracteres");
        }
    }

    // Validar teléfono tipo Integer (6 a 15 dígitos)
    public static void validarTelefono(Integer telefono) {
        if (telefono == null || telefono <= 0) {
            throw new IllegalArgumentException("El teléfono del cliente es obligatorio y debe ser positivo");
        }

        int longitud = String.valueOf(telefono).length();
        if (longitud < 6 || longitud > 15) {
            throw new IllegalArgumentException("El teléfono debe tener entre 6 y 15 dígitos");
        }
    }

    // Verificar que no exista otro cliente con el mismo ID
    public static void validarExistencia(int id) {
        if (clienteDAO.obtenerPorCod(id) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con este ID");
        }
    }

    // Validación general para insertar o modificar cliente
    public static void validarCliente(Cliente cliente) {
        validarId(cliente.getIdCliente());
        validarNombre(cliente.getNombre());
        validarApellidos(cliente.getApellidos());
        validarTelefono(cliente.getTelefono());

        // Solo verificar existencia si es un nuevo cliente
        validarExistencia(cliente.getIdCliente());
    }
}
