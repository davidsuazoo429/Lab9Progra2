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

public class Recepcion extends Thread {
    private final Cola colaRecepcion;
    private final Control ctrl;
    private final Logger logger;
    private final Stats stats;
    private int contador = 100;
    private final Random rand = new Random();

    private final String[] CLIENTES = {"Carlos López", "Ana Gómez", "Pedro Martínez", "Lucía Fernández", "Elena Ramos", "David Silva"};
    private final String[] CIUDADES = {"Barcelona Centro", "Eixample", "Gràcia", "Sant Martí", "Badalona"};
    private final Prioridad[] PRIORIDADES = Prioridad.values();

    public Recepcion(Cola colaRecepcion, Control ctrl, Logger logger, Stats stats) {
        this.colaRecepcion = colaRecepcion;
        this.ctrl = ctrl;
        this.logger = logger;
        this.stats = stats;
    }

    @Override
    public void run() {
        while (ctrl.isEjecutando()) {
            try {
                ctrl.verificarPausa();
                Thread.sleep(5000 + rand.nextInt(3000));
                ctrl.verificarPausa();

                String cod = "PKG-" + (++contador);
                String cli = CLIENTES[rand.nextInt(CLIENTES.length)];
                String ciu = CIUDADES[rand.nextInt(CIUDADES.length)];
                double peso = Math.round((0.5 + rand.nextDouble() * 7.5) * 10.0) / 10.0;
                Prioridad prio = PRIORIDADES[rand.nextInt(PRIORIDADES.length)];

                Paquete p = new Paquete(cod, cli, "Calle " + (rand.nextInt(100) + 1), ciu, peso, prio);
                p.setEstado(EstadoPaquete.RECIBIDO);
                colaRecepcion.encolar(p, ctrl);

                stats.registrarGenerado();
                logger.log(p.getCodigo() + " recibido [" + p.getPrioridad().getNombre() + "] (" + p.getPeso() + "kg, Destino: " + p.getCiudad() + ")");
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
