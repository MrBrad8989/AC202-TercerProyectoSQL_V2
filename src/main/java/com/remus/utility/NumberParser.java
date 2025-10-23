package com.remus.utility;

/**
 * Utilidad para parsear números con coma o punto y separadores de miles.
 */
public class NumberParser {

    /**
     * Parsea un string de precio aceptando formatos como "1.234,56", "1234.56", "12,34", "1 234,56" y símbolos de moneda.
     * Devuelve un double con el valor normalizado. Lanza NumberFormatException con mensaje claro si no se puede parsear.
     */
    public static double parsePrecio(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("Precio vacío");
        String s = input.trim();
        // Eliminar símbolos de moneda y espacios
        s = s.replaceAll("[€$\\s]", "");
        // Mantener solo dígitos, comas, puntos y signo negativo
        s = s.replaceAll("[^0-9,.-]", "");
        if (s.isEmpty()) throw new NumberFormatException("Precio vacío");

        int lastDot = s.lastIndexOf('.');
        int lastComma = s.lastIndexOf(',');

        if (lastComma > -1 && lastDot > -1) {
            // Aparecen ambos. Determinar cuál es el separador decimal mirando cuál aparece más a la derecha
            if (lastComma > lastDot) {
                // La coma es decimal: eliminar puntos (miles) y convertir coma a punto
                s = s.replace(".", "");
                s = s.replace(',', '.');
            } else {
                // El punto es decimal: eliminar comas (miles)
                s = s.replace(",", "");
            }
        } else if (lastComma > -1) {
            // Solo coma presente -> decimal
            s = s.replace(',', '.');
        } else {
            // Solo punto o ninguno -> eliminar comas por si acaso
            s = s.replace(",", "");
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Formato no válido: '" + input + "'");
        }
    }
}

