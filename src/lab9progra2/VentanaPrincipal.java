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
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VentanaPrincipal extends JFrame {
    private final Cola colaRecepcion = new Cola("Recepción", 10);
    private final Cola colaAlmacen = new Cola("Almacén", 20);
    private final Cola colaClasificacion = new Cola("Clasificación", 10);
    private final Cola colaEmpaquetado = new Cola("Empaquetado", 8);
    private final Cola[] colasExpedicion = new Cola[]{
            new Cola("Ruta 1", 10),
            new Cola("Ruta 2", 10),
            new Cola("Ruta 3", 10),
            new Cola("Ruta 4", 10)
    };

    private final Control ctrl = new Control();
    private final Stats stats = new Stats();

    private Recepcion hiloRecep;
    private Almacen hiloAlmacen;
    private Clasificador[] clasificadores;
    private Empaquetador[] empaquetadores;
    private Repartidor[] repartidores;

    private JTextArea txtLog;
    private JProgressBar barRecepcion, barAlmacen, barEmpaquetado;
    
    // Paneles que soportan color HTML
    private JEditorPane lblRecepcionItems, lblAlmacenItems;
    private JTextArea lblClasificacionStatus, lblEmpaquetadoStatus;
    private JEditorPane[] lblExpedicionRutas;
    
    private JPanel[] pnlRepartidores;
    private JLabel[] lblRepartidorStatus;
    private JButton btnIniciar, btnPausar, btnReanudar, btnDetener, btnReiniciar, btnStats;
    private Timer timer;

    public VentanaPrincipal() {
        super("📦 Simulador de Logística y Paquetería");
        inicializarComponentes();
        configurarEventos();
        setSize(1100, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(5, 5));

        // 1. Barra superior
        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlControl.setBackground(new Color(236, 240, 241));
        pnlControl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));

        btnIniciar = new JButton("▶ Iniciar");
        btnPausar = new JButton("⏸ Pausar");
        btnReanudar = new JButton("⏯ Reanudar");
        btnDetener = new JButton("⏹ Detener");
        btnReiniciar = new JButton("🔄 Reiniciar");
        btnStats = new JButton("📊 Estadísticas");

        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnDetener.setEnabled(false);

        pnlControl.add(btnIniciar);
        pnlControl.add(btnPausar);
        pnlControl.add(btnReanudar);
        pnlControl.add(btnDetener);
        pnlControl.add(btnReiniciar);
        pnlControl.add(btnStats);
        add(pnlControl, BorderLayout.NORTH);

        // 2. Área Central
        JPanel pnlCentro = new JPanel(new GridLayout(4, 1, 5, 5));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Fila 1: Recepción, Almacén, Clasificación
        JPanel pnlFila1 = new JPanel(new GridLayout(1, 3, 5, 5));

        // Recepción (HTML con colores)
        JPanel pRecepcion = new JPanel(new BorderLayout(3, 3));
        pRecepcion.setBorder(BorderFactory.createTitledBorder("📥 RECEPCIÓN"));
        barRecepcion = new JProgressBar(0, 10);
        barRecepcion.setStringPainted(true);
        lblRecepcionItems = new JEditorPane();
        lblRecepcionItems.setContentType("text/html");
        lblRecepcionItems.setEditable(false);
        lblRecepcionItems.setBackground(Color.WHITE);
        pRecepcion.add(barRecepcion, BorderLayout.NORTH);
        pRecepcion.add(new JScrollPane(lblRecepcionItems), BorderLayout.CENTER);

        // Almacén Central (HTML con colores)
        JPanel pAlmacen = new JPanel(new BorderLayout(3, 3));
        pAlmacen.setBorder(BorderFactory.createTitledBorder("🏬 ALMACÉN CENTRAL"));
        barAlmacen = new JProgressBar(0, 20);
        barAlmacen.setStringPainted(true);
        lblAlmacenItems = new JEditorPane();
        lblAlmacenItems.setContentType("text/html");
        lblAlmacenItems.setEditable(false);
        lblAlmacenItems.setBackground(Color.WHITE);
        pAlmacen.add(barAlmacen, BorderLayout.NORTH);
        pAlmacen.add(new JScrollPane(lblAlmacenItems), BorderLayout.CENTER);

        // Clasificación
        JPanel pClasificacion = new JPanel(new BorderLayout(3, 3));
        pClasificacion.setBorder(BorderFactory.createTitledBorder("🏷️ CLASIFICACIÓN (3 Clasificadores)"));
        lblClasificacionStatus = new JTextArea(4, 15);
        lblClasificacionStatus.setEditable(false);
        lblClasificacionStatus.setFont(new Font("Monospaced", Font.BOLD, 11));
        pClasificacion.add(new JScrollPane(lblClasificacionStatus), BorderLayout.CENTER);

        pnlFila1.add(pRecepcion);
        pnlFila1.add(pAlmacen);
        pnlFila1.add(pClasificacion);
        pnlCentro.add(pnlFila1);

        // Fila 2: Empaquetado
        JPanel pnlFila2 = new JPanel(new BorderLayout(3, 3));
        pnlFila2.setBorder(BorderFactory.createTitledBorder("📦 EMPAQUETADO (2 Empaquetadores)"));
        barEmpaquetado = new JProgressBar(0, 8);
        barEmpaquetado.setStringPainted(true);
        lblEmpaquetadoStatus = new JTextArea(3, 20);
        lblEmpaquetadoStatus.setEditable(false);
        lblEmpaquetadoStatus.setFont(new Font("Monospaced", Font.BOLD, 11));
        pnlFila2.add(barEmpaquetado, BorderLayout.NORTH);
        pnlFila2.add(new JScrollPane(lblEmpaquetadoStatus), BorderLayout.CENTER);
        pnlCentro.add(pnlFila2);

        // Fila 3: Expedición (HTML con colores en cada ruta)
        JPanel pnlFila3 = new JPanel(new GridLayout(1, 4, 5, 5));
        pnlFila3.setBorder(BorderFactory.createTitledBorder("🚚 EXPEDICIÓN (Por rutas)"));
        lblExpedicionRutas = new JEditorPane[4];
        for (int i = 0; i < 4; i++) {
            JPanel pRuta = new JPanel(new BorderLayout());
            pRuta.setBorder(BorderFactory.createTitledBorder("Ruta " + (i + 1)));
            lblExpedicionRutas[i] = new JEditorPane();
            lblExpedicionRutas[i].setContentType("text/html");
            lblExpedicionRutas[i].setEditable(false);
            lblExpedicionRutas[i].setBackground(Color.WHITE);
            pRuta.add(new JScrollPane(lblExpedicionRutas[i]), BorderLayout.CENTER);
            pnlFila3.add(pRuta);
        }
        pnlCentro.add(pnlFila3);

        // Fila 4: Repartidores
        JPanel pnlFila4 = new JPanel(new GridLayout(1, 4, 5, 5));
        pnlFila4.setBorder(BorderFactory.createTitledBorder("🚛 REPARTIDORES"));
        pnlRepartidores = new JPanel[4];
        lblRepartidorStatus = new JLabel[4];
        int[] capacidades = {5, 4, 6, 5};
        for (int i = 0; i < 4; i++) {
            pnlRepartidores[i] = new JPanel(new GridLayout(3, 1));
            pnlRepartidores[i].setBorder(BorderFactory.createEtchedBorder());
            pnlRepartidores[i].setBackground(new Color(245, 245, 245));
            JLabel lblTitulo = new JLabel("🚚 Repartidor " + (i + 1) + " (Max " + capacidades[i] + ")", JLabel.CENTER);
            lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblRepartidorStatus[i] = new JLabel("DISPONIBLE | 0/" + capacidades[i], JLabel.CENTER);
            JLabel lblRuta = new JLabel("Asignado: Ruta " + (i + 1), JLabel.CENTER);
            lblRuta.setForeground(Color.DARK_GRAY);

            pnlRepartidores[i].add(lblTitulo);
            pnlRepartidores[i].add(lblRepartidorStatus[i]);
            pnlRepartidores[i].add(lblRuta);
            pnlFila4.add(pnlRepartidores[i]);
        }
        pnlCentro.add(pnlFila4);
        add(pnlCentro, BorderLayout.CENTER);

        // Panel Log
        JPanel pnlSur = new JPanel(new BorderLayout());
        pnlSur.setPreferredSize(new Dimension(1000, 160));
        pnlSur.setBorder(BorderFactory.createTitledBorder("📝 LOG DEL SISTEMA"));
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(20, 24, 30));
        txtLog.setForeground(new Color(46, 204, 113));
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        pnlSur.add(scrollLog, BorderLayout.CENTER);
        add(pnlSur, BorderLayout.SOUTH);

        // Timer de refresco visual
        timer = new Timer(200, e -> refrescarGUI());
        timer.start();
    }

    private void configurarEventos() {
        Logger logger = mensaje -> SwingUtilities.invokeLater(() -> {
            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            txtLog.append(hora + " | " + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });

        btnIniciar.addActionListener(e -> {
            ctrl.iniciar();
            iniciarHilos(logger);
            btnIniciar.setEnabled(false);
            btnPausar.setEnabled(true);
            btnDetener.setEnabled(true);
            btnReiniciar.setEnabled(false);
            logger.log(">>> SIMULACIÓN INICIADA <<<");
        });

        btnPausar.addActionListener(e -> {
            ctrl.pausar();
            btnPausar.setEnabled(false);
            btnReanudar.setEnabled(true);
            logger.log(">>> SIMULACIÓN PAUSADA <<<");
        });

        btnReanudar.addActionListener(e -> {
            ctrl.reanudar();
            btnPausar.setEnabled(true);
            btnReanudar.setEnabled(false);
            logger.log(">>> SIMULACIÓN REANUDADA <<<");
        });

        btnDetener.addActionListener(e -> {
            ctrl.detener();
            btnIniciar.setEnabled(true);
            btnPausar.setEnabled(false);
            btnReanudar.setEnabled(false);
            btnDetener.setEnabled(false);
            btnReiniciar.setEnabled(true);
            logger.log(">>> SIMULACIÓN DETENIDA <<<");
        });

        btnReiniciar.addActionListener(e -> {
            ctrl.detener();
            limpiarEstructuras();
            stats.reiniciar();
            txtLog.setText("");
            logger.log(">>> SISTEMA REINICIADO <<<");
            btnIniciar.setEnabled(true);
            btnReiniciar.setEnabled(true);
        });

        btnStats.addActionListener(e -> mostrarEstadisticas());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctrl.detener();
            }
        });
    }

    private void iniciarHilos(Logger logger) {
        hiloRecep = new Recepcion(colaRecepcion, ctrl, logger, stats);
        hiloAlmacen = new Almacen(colaRecepcion, colaAlmacen, ctrl, logger);

        clasificadores = new Clasificador[3];
        for (int i = 0; i < 3; i++) {
            clasificadores[i] = new Clasificador(i + 1, colaAlmacen, colaEmpaquetado, ctrl, logger);
            clasificadores[i].start();
        }

        empaquetadores = new Empaquetador[2];
        for (int i = 0; i < 2; i++) {
            empaquetadores[i] = new Empaquetador(i + 1, colaEmpaquetado, colasExpedicion, ctrl, logger);
            empaquetadores[i].start();
        }

        repartidores = new Repartidor[4];
        int[] caps = {5, 4, 6, 5};
        for (int i = 0; i < 4; i++) {
            repartidores[i] = new Repartidor(i + 1, "Repartidor-" + (i + 1), caps[i],
                    "Ruta " + (i + 1), colasExpedicion[i], colaAlmacen, ctrl, logger, stats);
            repartidores[i].start();
        }

        hiloRecep.start();
        hiloAlmacen.start();
    }

    private void limpiarEstructuras() {
        colaRecepcion.limpiar();
        colaAlmacen.limpiar();
        colaClasificacion.limpiar();
        colaEmpaquetado.limpiar();
        for (Cola c : colasExpedicion) {
            c.limpiar();
        }
        if (repartidores != null) {
            for (Repartidor r : repartidores) {
                if (r != null) r.reset();
            }
        }
        refrescarGUI();
    }

    private void refrescarGUI() {
        // Recepción con HTML y colores
        barRecepcion.setValue(colaRecepcion.getTamanio());
        barRecepcion.setString(colaRecepcion.getTamanio() + " / " + colaRecepcion.getCapacidadMaxima());
        if (lblRecepcionItems != null) {
            lblRecepcionItems.setText(colaRecepcion.getResumenHtml(8));
        }

        // Almacén con HTML y colores
        barAlmacen.setValue(colaAlmacen.getTamanio());
        barAlmacen.setString(colaAlmacen.getTamanio() + " / " + colaAlmacen.getCapacidadMaxima());
        if (lblAlmacenItems != null) {
            lblAlmacenItems.setText(colaAlmacen.getResumenHtml(14));
        }

        // Clasificadores
        if (clasificadores != null) {
            StringBuilder sbC = new StringBuilder();
            for (int i = 0; i < clasificadores.length; i++) {
                if (clasificadores[i] != null) {
                    sbC.append("Clasificador ").append(i + 1).append(": ")
                       .append(clasificadores[i].getEstadoActual()).append("\n");
                }
            }
            lblClasificacionStatus.setText(sbC.toString());
        }

        // Empaquetado
        barEmpaquetado.setValue(colaEmpaquetado.getTamanio());
        barEmpaquetado.setString(colaEmpaquetado.getTamanio() + " / " + colaEmpaquetado.getCapacidadMaxima());
        if (empaquetadores != null) {
            StringBuilder sbE = new StringBuilder();
            for (int i = 0; i < empaquetadores.length; i++) {
                if (empaquetadores[i] != null) {
                    sbE.append("Empaquetador ").append(i + 1).append(": ")
                       .append(empaquetadores[i].getEstadoActual()).append("\n");
                }
            }
            lblEmpaquetadoStatus.setText(sbE.toString());
        }

        // Expedición con HTML y colores
        if (lblExpedicionRutas != null) {
            for (int i = 0; i < 4; i++) {
                if (lblExpedicionRutas[i] != null) {
                    lblExpedicionRutas[i].setText(colasExpedicion[i].getResumenHtml(6));
                }
            }
        }

        // Repartidores
        if (repartidores != null && lblRepartidorStatus != null && pnlRepartidores != null) {
            for (int i = 0; i < 4; i++) {
                if (repartidores[i] != null && lblRepartidorStatus[i] != null && pnlRepartidores[i] != null) {
                    EstadoRepartidor est = repartidores[i].getEstadoRepartidor();
                    lblRepartidorStatus[i].setText(est.name() + " | " + repartidores[i].getCantidadCargada() + "/" + repartidores[i].getCapacidadMax());
                    switch (est) {
                        case DISPONIBLE:
                            pnlRepartidores[i].setBackground(new Color(230, 247, 255));
                            break;
                        case CARGANDO:
                            pnlRepartidores[i].setBackground(new Color(255, 251, 230));
                            break;
                        case EN_RUTA:
                        case ENTREGANDO:
                            pnlRepartidores[i].setBackground(new Color(230, 255, 230));
                            break;
                        case REGRESANDO:
                            pnlRepartidores[i].setBackground(new Color(255, 240, 245));
                            break;
                        default:
                            pnlRepartidores[i].setBackground(new Color(245, 245, 245));
                            break;
                    }
                }
            }
        }
    }

    private void mostrarEstadisticas() {
        int generados = stats.getGenerados();
        int entregados = stats.getEntregados();
        int devueltos = stats.getDevueltos();
        int enProceso = generados - (entregados + devueltos);
        double promedioSegundos = (entregados > 0)
                ? (double) stats.getTiempoTotalMs() / (entregados * 1000.0)
                : 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         📊 ESTADÍSTICAS DEL SISTEMA     \n");
        sb.append("=========================================\n\n");
        sb.append(String.format("  Paquetes Generados:      %d\n", generados));
        sb.append(String.format("  Entregados con Éxito:    %d\n", entregados));
        sb.append(String.format("  Devueltos (Fallidos):    %d\n", devueltos));
        sb.append(String.format("  Actualmente en Proceso:  %d\n", Math.max(0, enProceso)));
        sb.append(String.format("  Tiempo promedio entrega: %.2f s\n\n", promedioSegundos));
        sb.append("-----------------------------------------\n");
        sb.append("  Entregas por Repartidor:\n");
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("    - Repartidor %d: %d paquetes\n", (i + 1), stats.getPorRepartidor(i)));
        }
        sb.append("=========================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.BOLD, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Panel de Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }
}
