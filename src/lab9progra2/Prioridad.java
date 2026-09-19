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
    BAJA(1, "BAJ", "BAJA", "#2E7D32"),        
    NORMAL(2, "NOR", "NORMAL", "#C68A00"),    
    ALTA(3, "ALT", "ALTA", "#E65100"),       
    URGENTE(4, "URG", "URGENTE", "#D32F2F");  

    private final int valor;
    private final String tag;
    private final String nombre;
    private final String colorHex;

    Prioridad(int valor, String tag, String nombre, String colorHex) {
        this.valor = valor;
        this.tag = tag;
        this.nombre = nombre;
        this.colorHex = colorHex;
    }

    public int getValor() {
        return valor; 
    }
    public String getTag() {
        return tag; 
    }
    public String getNombre() {
        return nombre; 
    }
    public String getColorHex() {
        return colorHex; 
    }
}
