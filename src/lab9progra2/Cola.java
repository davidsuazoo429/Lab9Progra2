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

    // Inserción ordenada por prioridad estricta
    public synchronized void encolar(Paquete p, Control ctrl) throws InterruptedException {
        while (lista.getTamanio() >= capacidadMaxima && ctrl.isEjecutando()) {
            wait();
        }
        if (!ctrl.isEjecutando()) return;

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

    // Genera bloques con fondo de color real (HTML compatible con Java Swing)
    public synchronized String getResumenHtml(int maxElementos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:2px; font-family:sans-serif;'>");
        sb.append("<table cellspacing='3' cellpadding='3'>");

        Nodo<Paquete> actual = lista.cabeza;
        int count = 0;
        while (actual != null && count < maxElementos) {
            if (count % 2 == 0) sb.append("<tr>");

            Paquete p = actual.dato;
            String color = p.getPrioridad().getColorHex();
            String textoColor = (p.getPrioridad() == Prioridad.NORMAL) ? "#000000" : "#FFFFFF";

            sb.append("<td bgcolor='").append(color).append("' align='center'>")
              .append("<font color='").append(textoColor).append("' size='2'><b> ")
              .append(p.getCodigo()).append(" [").append(p.getPrioridad().getTag()).append("] ")
              .append("</b></font></td>");

            count++;
            if (count % 2 == 0) sb.append("</tr>");
            actual = actual.siguiente;
        }

        if (count % 2 != 0) sb.append("<td></td></tr>");
        sb.append("</table>");

        if (actual != null) {
            sb.append("<div style='color:#666666; font-size:9px;'><i>+(")
              .append(lista.getTamanio() - count).append(" más en cola)</i></div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    public synchronized String getResumenTexto(int maxElementos) {
        return getResumenHtml(maxElementos);
    }

    public synchronized void limpiar() {
        lista.limpiar();
        notifyAll();
    }
}
