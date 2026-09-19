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
public class Stats {
    private int generados = 0;
    private int entregados = 0;
    private int devueltos = 0;
    private long tiempoTotalMs = 0;
    private final int[] porRepartidor = new int[4];

    public synchronized void registrarGenerado() {
        generados++;
    }

    public synchronized void registrarEntregado(int idRepartidor, long tiempoMs) {
        entregados++;
        tiempoTotalMs += tiempoMs;
        if (idRepartidor >= 1 && idRepartidor <= 4) {
            porRepartidor[idRepartidor - 1]++;
        }
    }

    public synchronized void registrarDevuelto() {
        devueltos++;
    }

    public synchronized void reiniciar() {
        generados = 0;
        entregados = 0;
        devueltos = 0;
        tiempoTotalMs = 0;
        for (int i = 0; i < 4; i++) porRepartidor[i] = 0;
    }

    public synchronized int getGenerados() {
        return generados; 
    }
    public synchronized int getEntregados() {
        return entregados; 
    }
    public synchronized int getDevueltos() {
        return devueltos; 
    }
    public synchronized long getTiempoTotalMs() {
        return tiempoTotalMs; 
    }
    public synchronized int getPorRepartidor(int index) {
        return porRepartidor[index]; 
    }
}
