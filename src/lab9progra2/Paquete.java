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
public class Paquete {
    private final String codigo;
    private final String cliente;
    private final String direccion;
    private final String ciudad;
    private final double peso;
    private final Prioridad prioridad;
    private EstadoPaquete estado;
    private String rutaAsignada;
    private int intentos;
    private final long tiempoCreacion;

    public Paquete(String codigo, String cliente, String direccion, String ciudad, double peso, Prioridad prioridad) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.peso = peso;
        this.prioridad = prioridad;
        this.estado = EstadoPaquete.RECIBIDO;
        this.rutaAsignada = "Sin Asignar";
        this.intentos = 0;
        this.tiempoCreacion = System.currentTimeMillis();
    }

    public String getCodigo() {
        return codigo; 
    }
    public String getCiudad() {
        return ciudad; 
    }
    public double getPeso() {
        return peso; 
    }
    public Prioridad getPrioridad() {
        return prioridad; 
    }
    public EstadoPaquete getEstado() {
        return estado; 
    }
    public void setEstado(EstadoPaquete estado) {
        this.estado = estado; 
    }
    public String getRutaAsignada() { 
        return rutaAsignada; 
    }
    public void setRutaAsignada(String rutaAsignada) {
        this.rutaAsignada = rutaAsignada; 
    }
    public int getIntentos() {
        return intentos;
    }
    public void incrementarIntentos() { 
        this.intentos++; 
    }
    public long getTiempoCreacion() {
        return tiempoCreacion; 
    } 
}
