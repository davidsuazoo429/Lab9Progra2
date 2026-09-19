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
public class Control {
    private volatile boolean ejecutando = false;
    private volatile boolean pausado = false;
    private final Object lock = new Object();

    public boolean isEjecutando() { return ejecutando; }
    public boolean isPausado() { return pausado; }

    public void iniciar() {
        ejecutando = true;
        pausado = false;
    }

    public void pausar() {
        pausado = true;
    }

    public void reanudar() {
        synchronized (lock) {
            pausado = false;
            lock.notifyAll();
        }
    }

    public void detener() {
        ejecutando = false;
        reanudar();
    }

    public void verificarPausa() throws InterruptedException {
        synchronized (lock) {
            while (pausado && ejecutando) {
                lock.wait();
            }
        }
    }
}
