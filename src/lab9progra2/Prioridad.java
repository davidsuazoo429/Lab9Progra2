/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab9progra2;

/**
 *
 * @author David Suazo Palao
 */
public enum Prioridad {
    BAJA(1, "🟢"),
    NORMAL(2, "🟡"),
    ALTA(3, "🟠"),
    URGENTE(4, "🔴");

    private final int valor;
    private final String icono;

    Prioridad(int valor, String icono) {
        this.valor = valor;
        this.icono = icono;
    }

    public int getValor() { return valor; }
    public String getIcono() { return icono; }
}
