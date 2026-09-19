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
public class Empaquetador extends Thread{
    private final int id;
    private final Cola empaquetadoCola;
    private final Cola[] expedicionRutas;
    private final Control ctrl;
    private final Logger logger;
    private volatile String estadoActual = "Inactivo";

    public Empaquetador(int id, Cola empaquetadoCola, Cola[] expedicionRutas, Control ctrl, Logger logger) {
        this.id = id;
        this.empaquetadoCola = empaquetadoCola;
        this.expedicionRutas = expedicionRutas;
        this.ctrl = ctrl;
        this.logger = logger;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    @Override
    public void run() {
        while (ctrl.isEjecutando()) {
            try {
                ctrl.verificarPausa();
                estadoActual = "Esperando";
                Paquete p = empaquetadoCola.desencolar(ctrl);
                if (p != null) {
                    p.setEstado(EstadoPaquete.EMPAQUETANDO);
                    estadoActual = "Empaquetando " + p.getCodigo();

                    long tiempoMs = (p.getPeso() <= 2.0) ? 1500 : (p.getPeso() <= 5.0 ? 2300 : 3200);
                    Thread.sleep(tiempoMs);

                    p.setEstado(EstadoPaquete.EMPAQUETADO);
                    logger.log(p.getCodigo() + " empaquetado por Empaquetador-" + id + " (" + (tiempoMs / 1000.0) + "s)");

                    int idxRuta = 0;
                    if (p.getRutaAsignada().endsWith("2")) idxRuta = 1;
                    else if (p.getRutaAsignada().endsWith("3")) idxRuta = 2;
                    else if (p.getRutaAsignada().endsWith("4")) idxRuta = 3;

                    p.setEstado(EstadoPaquete.EN_EXPEDICION);
                    expedicionRutas[idxRuta].encolar(p, ctrl);
                    estadoActual = "Libre";
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        estadoActual = "Detenido";
    }
}
