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
public class Almacen extends Thread{
    private final Cola recepcion;
    private final Cola almacen;
    private final Control ctrl;
    private final Logger logger;

    public Almacen(Cola recepcion, Cola almacen, Control ctrl, Logger logger) {
        this.recepcion = recepcion;
        this.almacen = almacen;
        this.ctrl = ctrl;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (ctrl.isEjecutando()) {
            try {
                ctrl.verificarPausa();
                Paquete p = recepcion.desencolar(ctrl);
                if (p != null) {
                    Thread.sleep(500);
                    p.setEstado(EstadoPaquete.ALMACENADO);
                    almacen.encolar(p, ctrl);
                    logger.log(p.getCodigo() + " almacenado en bodega central");
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
