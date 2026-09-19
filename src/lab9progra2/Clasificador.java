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
public class Clasificador extends Thread {
    private final int id;
    private final Cola almacen;
    private final Cola empaquetado;
    private final Control ctrl;
    private final Logger logger;
    private volatile String estadoActual = "Inactivo";

    public Clasificador(int id, Cola almacen, Cola empaquetado, Control ctrl, Logger logger) {
        this.id = id;
        this.almacen = almacen;
        this.empaquetado = empaquetado;
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
                Paquete p = almacen.desencolar(ctrl);
                if (p != null) {
                    p.setEstado(EstadoPaquete.CLASIFICANDO);
                    estadoActual = "Procesando " + p.getCodigo();
                    logger.log(p.getCodigo() + " tomado por Clasificador-" + id);
                    
                    // ⏱️ 2 segundos de clasificación
                    Thread.sleep(2000);

                    switch (p.getCiudad()) {
                        case "Barcelona Centro":
                        case "Eixample":
                            p.setRutaAsignada("Ruta 1");
                            break;
                        case "Gràcia":
                            p.setRutaAsignada("Ruta 2");
                            break;
                        case "Sant Martí":
                            p.setRutaAsignada("Ruta 3");
                            break;
                        default:
                            p.setRutaAsignada("Ruta 4");
                            break;
                    }

                    p.setEstado(EstadoPaquete.CLASIFICADO);
                    logger.log(p.getCodigo() + " clasificado → " + p.getRutaAsignada());
                    empaquetado.encolar(p, ctrl);
                    estadoActual = "Libre";
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        estadoActual = "Detenido";
    }
}
