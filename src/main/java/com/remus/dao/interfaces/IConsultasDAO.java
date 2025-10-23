package com.remus.dao.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IConsultasDAO {
    List<Map<String, Object>> obtenerClientesOrdenados();
    List<Map<String, Object>> obtenerProductosOrdenados();
    Map<Integer, Map<String, Object>> obtenerVentasConLineas();
    Map<String, Object> obtenerResumenVentas(LocalDate fechaInicio, LocalDate fechaFin);
    List<Map<String, Object>> obtenerVentasPorCliente(LocalDate fechaInicio, LocalDate fechaFin);
}

