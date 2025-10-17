package com.remus.dao.interfaces;

import com.remus.modelo.Empresa;
import java.util.List;

public interface IEmpresaDAO {
    Empresa obtenerPorId(int idEmpresa);
    List<Empresa> obtenerTodas();
    boolean insertar(Empresa empresa);
    boolean actualizar(Empresa empresa);
    boolean eliminar(int idEmpresa);
    Empresa obtenerPorCif(String cif);
    int contarEmpresas();
}
