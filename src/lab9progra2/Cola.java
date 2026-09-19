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
public class Cola {
    private final ListaEnlazada<Paquete> lista = new ListaEnlazada<>();
    private final int capacidadMaxima;
    private final String nombre;

    public Cola(String nombre, int capacidadMaxima) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
    }

    public synchronized void encolar(Paquete p, Control ctrl) throws InterruptedException {
        while (lista.getTamanio() >= capacidadMaxima && ctrl.isEjecutando()) {
            wait();
        }
        if (!ctrl.isEjecutando()) return;

        // Inserción por prioridad
        if (lista.estaVacia()) {
            lista.agregar(p);
        } else {
            Nodo<Paquete> nuevo = new Nodo<>(p);
            if (p.getPrioridad().getValor() > lista.cabeza.dato.getPrioridad().getValor()) {
                nuevo.siguiente = lista.cabeza;
                lista.cabeza = nuevo;
                lista.tamanio++;
            } else {
                Nodo<Paquete> actual = lista.cabeza;
                while (actual.siguiente != null &&
                        actual.siguiente.dato.getPrioridad().getValor() >= p.getPrioridad().getValor()) {
                    actual = actual.siguiente;
                }
                nuevo.siguiente = actual.siguiente;
                actual.siguiente = nuevo;
                lista.tamanio++;
            }
        }
        notifyAll();
    }

    public synchronized Paquete desencolar(Control ctrl) throws InterruptedException {
        while (lista.estaVacia() && ctrl.isEjecutando()) {
            wait();
        }
        if (!ctrl.isEjecutando()) return null;
        Paquete p = lista.eliminarPrimero();
        notifyAll();
        return p;
    }

    public synchronized int getTamanio() {
        return lista.getTamanio();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public synchronized String getResumenTexto(int maxElementos) {
        StringBuilder sb = new StringBuilder();
        Nodo<Paquete> actual = lista.cabeza;
        int count = 0;
        while (actual != null && count < maxElementos) {
            sb.append("[").append(actual.dato.getCodigo())
              .append(" ").append(actual.dato.getPrioridad().getIcono())
              .append("] ");
            if ((count + 1) % 3 == 0) sb.append("\n");
            actual = actual.siguiente;
            count++;
        }
        if (actual != null) {
            sb.append("... (+").append(lista.getTamanio() - count).append(" más)");
        }
        return sb.toString();
    }

    public synchronized void limpiar() {
        lista.limpiar();
        notifyAll();
    }
}
