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

    public Repartidor(int id, String nombre, int capacidadMax, String ruta,
                      Cola colaExpedicion, Cola colaReingresoAlmacen,
                      Control ctrl, Logger logger, Stats stats) {
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

    public EstadoRepartidor getEstadoRepartidor() { return estado; }
    public int getCantidadCargada() { return cargados.getTamanio(); }
    public int getCapacidadMax() { return capacidadMax; }
    public String getRuta() { return ruta; }
    public String getNombre() { return nombre; }

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

                while (cargados.getTamanio() < capacidadMax && colaExpedicion.getTamanio() > 0) {
                    Paquete extra = colaExpedicion.desencolar(ctrl);
                    if (extra != null) {
                        cargados.agregar(extra);
                        logger.log(nombre + " cargando " + extra.getCodigo());
                    }
                }

                // ⏱️ Tiempo cargando la furgoneta (2 segundos)
                Thread.sleep(2000);

                estado = EstadoRepartidor.EN_RUTA;
                logger.log("🚚 " + nombre + " sale a reparto con " + cargados.getTamanio() + " paquetes [" + ruta + "]");
                
                // ⏱️ Tiempo de viaje (4 segundos)
                Thread.sleep(4000);

                while (!cargados.estaVacia() && ctrl.isEjecutando()) {
                    ctrl.verificarPausa();
                    estado = EstadoRepartidor.ENTREGANDO;
                    Paquete p = cargados.eliminarPrimero();
                    p.setEstado(EstadoPaquete.EN_REPARTO);

                    // ⏱️ Tiempo entregando paquete al cliente (3.5 segundos)
                    Thread.sleep(3500);

                    boolean ausente = rand.nextDouble() < 0.20;

                    if (!ausente) {
                        p.setEstado(EstadoPaquete.ENTREGADO);
                        long tiempoMs = System.currentTimeMillis() - p.getTiempoCreacion();
                        stats.registrarEntregado(id, tiempoMs);
                        logger.log("✅ " + p.getCodigo() + " ENTREGADO por " + nombre);
                    } else {
                        p.incrementarIntentos();
                        logger.log("⚠️ Intento " + p.getIntentos() + " fallido para " + p.getCodigo() + " (Ausente)");
                        if (p.getIntentos() >= 3) {
                            p.setEstado(EstadoPaquete.DEVUELTO);
                            stats.registrarDevuelto();
                            logger.log("❌ " + p.getCodigo() + " DEVUELTO (3 intentos fallidos)");
                        } else {
                            p.setEstado(EstadoPaquete.NUEVO_INTENTO);
                            colaReingresoAlmacen.encolar(p, ctrl);
                        }
                    }
                }

                estado = EstadoRepartidor.REGRESANDO;
                // ⏱️ Tiempo de regreso al almacén (3 segundos)
                Thread.sleep(3000);

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
