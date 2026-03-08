package com.pizzeria.modelo;

/**
 * Tipo de cliente: persona fisica o empresa.
 *
 * En Spring con JPA, este enum se mapea directamente como columna:
 *
 *   @Enumerated(EnumType.STRING)
 *   private TipoCliente tipo;
 */
public enum TipoCliente {
    PERSONA,
    EMPRESA
}
