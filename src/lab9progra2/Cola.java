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

    public synchronized String getResumenHtml(int maxElementos) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:4px; font-family:Segoe UI, Tahoma, sans-serif; background-color:#FFFFFF;'>");
        sb.append("<table cellspacing='4' cellpadding='4' width='100%'>");

        Nodo<Paquete> actual = lista.cabeza;
        int count = 0;
        while (actual != null && count < maxElementos) {
            if (count % 2 == 0) sb.append("<tr>");

            Paquete p = actual.dato;
            String color = p.getPrioridad().getColorHex();
            String textoColor = (p.getPrioridad() == Prioridad.NORMAL) ? "#333333" : "#FFFFFF";

            sb.append("<td bgcolor='").append(color).append("' width='50%' style='padding:5px; border-radius:4px;'>")
              .append("<font color='").append(textoColor).append("' size='3'><b>")
              .append(p.getCodigo())
              .append("</b></font><br>")
              .append("<font color='").append(textoColor).append("' size='1'>")
              .append(p.getPrioridad().getNombre()).append(" | ").append(p.getPeso()).append("kg")
              .append("</font></td>");

            count++;
            if (count % 2 == 0) sb.append("</tr>");
            actual = actual.siguiente;
        }

        if (count % 2 != 0) sb.append("<td width='50%'></td></tr>");
        sb.append("</table>");

        if (actual != null) {
            sb.append("<div align='center' style='color:#64748B; font-size:10px; margin-top:2px;'><b>+ ")
              .append(lista.getTamanio() - count).append(" paquetes mas en espera</b></div>");
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
