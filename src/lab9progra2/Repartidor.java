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
import java.util.Random;

public class Repartidor extends Thread{
    private final int id;
    private final String nombre;
    private final int capacidadMax;
    private final String ruta;
    private final Cola colaExpedicion;
    private final Cola colaReingresoAlmacen;
    private final Control ctrl;
    private final Logger logger;
    private final Stats stats;

    private volatile EstadoRepartidor estado = EstadoRepartidor.DISPONIBLE;
    private final ListaEnlazada<Paquete> cargados = new ListaEnlazada<>();
    private final Random rand = new Random();

    public Repartidor(int id, String nombre, int capacidadMax, String ruta, Cola colaExpedicion, Cola colaReingresoAlmacen,Control ctrl, Logger logger, Stats stats) {
        this.id = id;
        this.nombre = nombre;
        this.capacidadMax = capacidadMax;
        this.ruta = ruta;
        this.colaExpedicion = colaExpedicion;
        this.colaReingresoAlmacen = colaReingresoAlmacen;
        this.ctrl = ctrl;
        this.logger = logger;
        this.stats = stats;
    }

    public EstadoRepartidor getEstadoRepartidor() {
        return estado; 
    }
    public int getCantidadCargada() {
        return cargados.getTamanio(); 
    }
    public int getCapacidadMax() {
        return capacidadMax; 
    }
    public String getRuta() {
        return ruta; 
    }
    public String getNombre() {
        return nombre; 
    }

    @Override
    public void run() {
        while (ctrl.isEjecutando()) {
            try {
                ctrl.verificarPausa();
                estado = EstadoRepartidor.DISPONIBLE;

                Paquete primerP = colaExpedicion.desencolar(ctrl);
                if (primerP == null) continue;

                estado = EstadoRepartidor.CARGANDO;
                cargados.agregar(primerP);
                logger.log(nombre + " cargando " + primerP.getCodigo());

                for (int i = 0; i < 3; i++) {
                    Thread.sleep(600);
                    while (cargados.getTamanio() < capacidadMax && colaExpedicion.getTamanio() > 0) {
                        Paquete extra = colaExpedicion.desencolar(ctrl);
                        if (extra != null) {
                            cargados.agregar(extra);
                            logger.log(nombre + " cargando " + extra.getCodigo());
                        }
                    }
                }

                estado = EstadoRepartidor.EN_RUTA;
                logger.log("[EN RUTA] " + nombre + " sale a reparto con " + cargados.getTamanio() + " paquetes [" + ruta + "]");
                
                Thread.sleep(2500);

                while (!cargados.estaVacia() && ctrl.isEjecutando()) {
                    ctrl.verificarPausa();
                    estado = EstadoRepartidor.ENTREGANDO;
                    Paquete p = cargados.eliminarPrimero();
                    p.setEstado(EstadoPaquete.EN_REPARTO);

                    Thread.sleep(2000);

                    boolean ausente = rand.nextDouble() < 0.20;

                    if (!ausente) {
                        p.setEstado(EstadoPaquete.ENTREGADO);
                        long tiempoMs = System.currentTimeMillis() - p.getTiempoCreacion();
                        stats.registrarEntregado(id, tiempoMs);
                        logger.log("[ENTREGADO] " + p.getCodigo() + " entregado exitosamente por " + nombre);
                    } else {
                        p.incrementarIntentos();
                        logger.log("[INTENTO FALLIDO] Intento " + p.getIntentos() + " para " + p.getCodigo() + " (Cliente ausente)");
                        if (p.getIntentos() >= 3) {
                            p.setEstado(EstadoPaquete.DEVUELTO);
                            stats.registrarDevuelto();
                            logger.log("[DEVUELTO] " + p.getCodigo() + " devuelto tras 3 intentos");
                        } else {
                            p.setEstado(EstadoPaquete.NUEVO_INTENTO);
                            colaReingresoAlmacen.encolar(p, ctrl);
                        }
                    }
                }

                estado = EstadoRepartidor.REGRESANDO;
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                break;
            }
        }
        estado = EstadoRepartidor.FUERA_DE_SERVICIO;
    }

    public void reset() {
        cargados.limpiar();
        estado = EstadoRepartidor.DISPONIBLE;
    }
}
