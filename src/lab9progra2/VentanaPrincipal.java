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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VentanaPrincipal extends JFrame {
    private static final Color COLOR_FONDO = new Color(241, 245, 249);
    private static final Color COLOR_HEADER = new Color(15, 23, 42);
    private static final Color COLOR_CARD = new Color(255, 255, 255);
    private static final Color COLOR_BORDE = new Color(226, 232, 240);
    private static final Color COLOR_TEXTO_TITULO = new Color(30, 41, 59);

    private final Cola colaRecepcion = new Cola("Recepcion", 10);
    private final Cola colaAlmacen = new Cola("Almacen", 20);
    private final Cola colaClasificacion = new Cola("Clasificacion", 10);
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
    
    private JEditorPane lblRecepcionItems, lblAlmacenItems;
    private JTextArea lblClasificacionStatus, lblEmpaquetadoStatus;
    private JEditorPane[] lblExpedicionRutas;
    
    private JPanel[] pnlRepartidores;
    private JLabel[] lblRepartidorStatus, lblRepartidorSub;
    private JButton btnIniciar, btnPausar, btnReanudar, btnDetener, btnReiniciar, btnStats;
    private Timer timer;

    public VentanaPrincipal() {
        super("Centro de Distribucion y Paqueteria");
        inicializarComponentes();
        configurarEventos();
        setSize(1180, 920);
        setMinimumSize(new Dimension(1050, 850));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JButton crearBotonModerno(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(7, 16, 7, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(COLOR_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(COLOR_TEXTO_TITULO);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 5, 0));
        card.add(lblTitulo, BorderLayout.NORTH);

        return card;
    }

    private JProgressBar crearBarraProgreso(int max, Color colorBarra) {
        JProgressBar bar = new JProgressBar(0, max);
        bar.setStringPainted(true);
        bar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bar.setForeground(colorBarra);
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorder(new LineBorder(COLOR_BORDE, 1, true));
        bar.setPreferredSize(new Dimension(bar.getPreferredSize().width, 20));
        return bar;
    }

    private void inicializarComponentes() {
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_HEADER);
        pnlHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblAppTitle = new JLabel("CENTRO DE DISTRIBUCION Y PAQUETERIA");
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAppTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblAppTitle, BorderLayout.WEST);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBotones.setOpaque(false);

        btnIniciar = crearBotonModerno("Iniciar", new Color(16, 185, 129));      
        btnPausar = crearBotonModerno("Pausar", new Color(245, 158, 11));       
        btnReanudar = crearBotonModerno("Reanudar", new Color(6, 182, 212));    
        btnDetener = crearBotonModerno("Detener", new Color(239, 68, 68));      
        btnReiniciar = crearBotonModerno("Reiniciar", new Color(99, 102, 241)); 
        btnStats = crearBotonModerno("Estadisticas", new Color(71, 85, 105));   

        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnDetener.setEnabled(false);

        pnlBotones.add(btnIniciar);
        pnlBotones.add(btnPausar);
        pnlBotones.add(btnReanudar);
        pnlBotones.add(btnDetener);
        pnlBotones.add(btnReiniciar);
        pnlBotones.add(btnStats);
        pnlHeader.add(pnlBotones, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel pnlFila1 = new JPanel(new GridLayout(1, 3, 10, 10));
        pnlFila1.setOpaque(false);

        JPanel cardRecepcion = crearTarjeta("RECEPCIoN");
        barRecepcion = crearBarraProgreso(10, new Color(59, 130, 246));
        lblRecepcionItems = new JEditorPane();
        lblRecepcionItems.setContentType("text/html");
        lblRecepcionItems.setEditable(false);
        lblRecepcionItems.setBackground(Color.WHITE);
        cardRecepcion.add(barRecepcion, BorderLayout.SOUTH);
        cardRecepcion.add(new JScrollPane(lblRecepcionItems), BorderLayout.CENTER);

        JPanel cardAlmacen = crearTarjeta("ALMACEN CENTRAL");
        barAlmacen = crearBarraProgreso(20, new Color(139, 92, 246));
        lblAlmacenItems = new JEditorPane();
        lblAlmacenItems.setContentType("text/html");
        lblAlmacenItems.setEditable(false);
        lblAlmacenItems.setBackground(Color.WHITE);
        cardAlmacen.add(barAlmacen, BorderLayout.SOUTH);
        cardAlmacen.add(new JScrollPane(lblAlmacenItems), BorderLayout.CENTER);

        JPanel cardClasificacion = crearTarjeta("CLASIFICACION (3 Operarios)");
        lblClasificacionStatus = new JTextArea(4, 15);
        lblClasificacionStatus.setEditable(false);
        lblClasificacionStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClasificacionStatus.setForeground(new Color(51, 65, 85));
        lblClasificacionStatus.setBackground(new Color(248, 250, 252));
        lblClasificacionStatus.setBorder(new EmptyBorder(5, 8, 5, 8));
        cardClasificacion.add(new JScrollPane(lblClasificacionStatus), BorderLayout.CENTER);

        pnlFila1.add(cardRecepcion);
        pnlFila1.add(cardAlmacen);
        pnlFila1.add(cardClasificacion);
        pnlCentro.add(pnlFila1);

        JPanel cardEmpaquetado = crearTarjeta("AREA DE EMPAQUETADO (Velocidad según peso)");
        barEmpaquetado = crearBarraProgreso(8, new Color(236, 72, 153));
        lblEmpaquetadoStatus = new JTextArea(2, 20);
        lblEmpaquetadoStatus.setEditable(false);
        lblEmpaquetadoStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmpaquetadoStatus.setForeground(new Color(51, 65, 85));
        lblEmpaquetadoStatus.setBackground(new Color(248, 250, 252));
        lblEmpaquetadoStatus.setBorder(new EmptyBorder(5, 8, 5, 8));
        cardEmpaquetado.add(barEmpaquetado, BorderLayout.SOUTH);
        cardEmpaquetado.add(new JScrollPane(lblEmpaquetadoStatus), BorderLayout.CENTER);
        pnlCentro.add(cardEmpaquetado);

        JPanel cardExpedicion = crearTarjeta("EXPEDICION Y SALIDA POR RUTAS");
        JPanel pnlRutasGrid = new JPanel(new GridLayout(1, 4, 8, 8));
        pnlRutasGrid.setOpaque(false);
        lblExpedicionRutas = new JEditorPane[4];

        for (int i = 0; i < 4; i++) {
            JPanel pRutaBox = new JPanel(new BorderLayout());
            pRutaBox.setBackground(Color.WHITE);
            pRutaBox.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDE, 1, true),
                    new EmptyBorder(4, 6, 4, 6)
            ));

            JLabel lblRutaName = new JLabel("Ruta " + (i + 1), JLabel.CENTER);
            lblRutaName.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblRutaName.setForeground(new Color(79, 70, 229));

            lblExpedicionRutas[i] = new JEditorPane();
            lblExpedicionRutas[i].setContentType("text/html");
            lblExpedicionRutas[i].setEditable(false);
            lblExpedicionRutas[i].setBackground(Color.WHITE);

            pRutaBox.add(lblRutaName, BorderLayout.NORTH);
            pRutaBox.add(new JScrollPane(lblExpedicionRutas[i]), BorderLayout.CENTER);
            pnlRutasGrid.add(pRutaBox);
        }
        cardExpedicion.add(pnlRutasGrid, BorderLayout.CENTER);
        pnlCentro.add(cardExpedicion);

        JPanel cardReparto = crearTarjeta("FLOTA DE REPARTIDORES EN SERVICIO");
        JPanel pnlRepartoGrid = new JPanel(new GridLayout(1, 4, 8, 8));
        pnlRepartoGrid.setOpaque(false);

        pnlRepartidores = new JPanel[4];
        lblRepartidorStatus = new JLabel[4];
        lblRepartidorSub = new JLabel[4];
        int[] capacidades = {5, 4, 6, 5};

        for (int i = 0; i < 4; i++) {
            pnlRepartidores[i] = new JPanel(new GridLayout(3, 1, 2, 2));
            pnlRepartidores[i].setBackground(new Color(248, 250, 252));
            pnlRepartidores[i].setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDE, 1, true),
                    new EmptyBorder(6, 8, 6, 8)
            ));

            JLabel lblTitulo = new JLabel("Repartidor " + (i + 1), JLabel.CENTER);
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitulo.setForeground(COLOR_TEXTO_TITULO);

            lblRepartidorStatus[i] = new JLabel("DISPONIBLE", JLabel.CENTER);
            lblRepartidorStatus[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblRepartidorStatus[i].setForeground(new Color(16, 185, 129));

            lblRepartidorSub[i] = new JLabel("Carga: 0/" + capacidades[i] + "  |  Ruta " + (i + 1), JLabel.CENTER);
            lblRepartidorSub[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblRepartidorSub[i].setForeground(new Color(100, 116, 139));

            pnlRepartidores[i].add(lblTitulo);
            pnlRepartidores[i].add(lblRepartidorStatus[i]);
            pnlRepartidores[i].add(lblRepartidorSub[i]);
            pnlRepartoGrid.add(pnlRepartidores[i]);
        }
        cardReparto.add(pnlRepartoGrid, BorderLayout.CENTER);
        pnlCentro.add(cardReparto);

        add(pnlCentro, BorderLayout.CENTER);

        JPanel pnlLogCard = crearTarjeta("REGISTRO DE EVENTOS EN TIEMPO REAL (LOG)");
        pnlLogCard.setPreferredSize(new Dimension(1000, 160));
        pnlLogCard.setBorder(new EmptyBorder(0, 15, 12, 15));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(15, 23, 42));
        txtLog.setForeground(new Color(52, 211, 153));
        txtLog.setCaretColor(Color.WHITE);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(new LineBorder(new Color(51, 65, 85), 1, true));
        pnlLogCard.add(scrollLog, BorderLayout.CENTER);
        add(pnlLogCard, BorderLayout.SOUTH);

        timer = new Timer(200, e -> refrescarGUI());
        timer.start();
    }

    private void configurarEventos() {
        Logger logger = mensaje -> SwingUtilities.invokeLater(() -> {
            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            txtLog.append(" [" + hora + "]  " + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });

        btnIniciar.addActionListener(e -> {
            ctrl.iniciar();
            iniciarHilos(logger);
            btnIniciar.setEnabled(false);
            btnPausar.setEnabled(true);
            btnDetener.setEnabled(true);
            btnReiniciar.setEnabled(false);
            logger.log(">>> SISTEMA INICIADO - INICIANDO PROCESAMIENTO <<<");
        });

        btnPausar.addActionListener(e -> {
            ctrl.pausar();
            btnPausar.setEnabled(false);
            btnReanudar.setEnabled(true);
            logger.log(">>> SIMULACION PAUSADA <<<");
        });

        btnReanudar.addActionListener(e -> {
            ctrl.reanudar();
            btnPausar.setEnabled(true);
            btnReanudar.setEnabled(false);
            logger.log(">>> SIMULACION REANUDADA <<<");
        });

        btnDetener.addActionListener(e -> {
            ctrl.detener();
            btnIniciar.setEnabled(true);
            btnPausar.setEnabled(false);
            btnReanudar.setEnabled(false);
            btnDetener.setEnabled(false);
            btnReiniciar.setEnabled(true);
            logger.log(">>> SIMULACION DETENIDA <<<");
        });

        btnReiniciar.addActionListener(e -> {
            ctrl.detener();
            limpiarEstructuras();
            stats.reiniciar();
            txtLog.setText("");
            logger.log(">>> SISTEMA REINICIADO - MEMORIA LIMPIA <<<");
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
        barRecepcion.setValue(colaRecepcion.getTamanio());
        barRecepcion.setString(colaRecepcion.getTamanio() + " / " + colaRecepcion.getCapacidadMaxima() + " paquetes");
        if (lblRecepcionItems != null) {
            lblRecepcionItems.setText(colaRecepcion.getResumenHtml(8));
        }

        barAlmacen.setValue(colaAlmacen.getTamanio());
        barAlmacen.setString(colaAlmacen.getTamanio() + " / " + colaAlmacen.getCapacidadMaxima() + " paquetes");
        if (lblAlmacenItems != null) {
            lblAlmacenItems.setText(colaAlmacen.getResumenHtml(14));
        }

        if (clasificadores != null) {
            StringBuilder sbC = new StringBuilder();
            for (int i = 0; i < clasificadores.length; i++) {
                if (clasificadores[i] != null) {
                    sbC.append(" - Clasificador ").append(i + 1).append(": ")
                       .append(clasificadores[i].getEstadoActual()).append("\n");
                }
            }
            lblClasificacionStatus.setText(sbC.toString());
        }

        barEmpaquetado.setValue(colaEmpaquetado.getTamanio());
        barEmpaquetado.setString(colaEmpaquetado.getTamanio() + " / " + colaEmpaquetado.getCapacidadMaxima() + " paquetes");
        if (empaquetadores != null) {
            StringBuilder sbE = new StringBuilder();
            for (int i = 0; i < empaquetadores.length; i++) {
                if (empaquetadores[i] != null) {
                    sbE.append(" - Empaquetador ").append(i + 1).append(": ")
                       .append(empaquetadores[i].getEstadoActual()).append("\n");
                }
            }
            lblEmpaquetadoStatus.setText(sbE.toString());
        }

        if (lblExpedicionRutas != null) {
            for (int i = 0; i < 4; i++) {
                if (lblExpedicionRutas[i] != null) {
                    lblExpedicionRutas[i].setText(colasExpedicion[i].getResumenHtml(4));
                }
            }
        }

        if (repartidores != null && lblRepartidorStatus != null && pnlRepartidores != null) {
            for (int i = 0; i < 4; i++) {
                if (repartidores[i] != null && lblRepartidorStatus[i] != null && pnlRepartidores[i] != null) {
                    EstadoRepartidor est = repartidores[i].getEstadoRepartidor();
                    lblRepartidorStatus[i].setText(est.name());
                    lblRepartidorSub[i].setText("Carga: " + repartidores[i].getCantidadCargada() + "/" + repartidores[i].getCapacidadMax() + "  |  " + repartidores[i].getRuta());

                    switch (est) {
                        case DISPONIBLE:
                            pnlRepartidores[i].setBackground(new Color(241, 245, 249));
                            lblRepartidorStatus[i].setForeground(new Color(71, 85, 105));
                            break;
                        case CARGANDO:
                            pnlRepartidores[i].setBackground(new Color(254, 243, 199));
                            lblRepartidorStatus[i].setForeground(new Color(217, 119, 6));
                            break;
                        case EN_RUTA:
                        case ENTREGANDO:
                            pnlRepartidores[i].setBackground(new Color(209, 250, 229));
                            lblRepartidorStatus[i].setForeground(new Color(5, 150, 105));
                            break;
                        case REGRESANDO:
                            pnlRepartidores[i].setBackground(new Color(224, 231, 255));
                            lblRepartidorStatus[i].setForeground(new Color(79, 70, 229));
                            break;
                        default:
                            pnlRepartidores[i].setBackground(new Color(248, 250, 252));
                            lblRepartidorStatus[i].setForeground(new Color(148, 163, 184));
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

        JDialog dialog = new JDialog(this, "Estadisticas del Sistema", true);
        dialog.setSize(520, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(241, 245, 249));

        JPanel pnlHeaderModal = new JPanel(new BorderLayout());
        pnlHeaderModal.setBackground(new Color(15, 23, 42));
        pnlHeaderModal.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTituloModal = new JLabel("PANEL DE RENDIMIENTO LOGISTICO");
        lblTituloModal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloModal.setForeground(Color.WHITE);

        JLabel lblSubModal = new JLabel("Metricas operativas del centro de distribucion");
        lblSubModal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubModal.setForeground(new Color(148, 163, 184));

        pnlHeaderModal.add(lblTituloModal, BorderLayout.NORTH);
        pnlHeaderModal.add(lblSubModal, BorderLayout.SOUTH);
        dialog.add(pnlHeaderModal, BorderLayout.NORTH);

        JPanel pnlContenido = new JPanel();
        pnlContenido.setLayout(new BoxLayout(pnlContenido, BoxLayout.Y_AXIS));
        pnlContenido.setOpaque(false);
        pnlContenido.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel pnlKPIs = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setMaximumSize(new Dimension(480, 150));

        pnlKPIs.add(crearCardKPI("Recibidos", String.valueOf(generados), new Color(59, 130, 246)));
        pnlKPIs.add(crearCardKPI("Entregados", String.valueOf(entregados), new Color(16, 185, 129)));
        pnlKPIs.add(crearCardKPI("Devueltos", String.valueOf(devueltos), new Color(239, 68, 68)));
        pnlKPIs.add(crearCardKPI("En Proceso", String.valueOf(Math.max(0, enProceso)), new Color(245, 158, 11)));
        pnlContenido.add(pnlKPIs);

        pnlContenido.add(Box.createVerticalStrut(10));

        JPanel cardTiempo = new JPanel(new BorderLayout());
        cardTiempo.setBackground(Color.WHITE);
        cardTiempo.setMaximumSize(new Dimension(480, 45));
        cardTiempo.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));
        JLabel lblTiempoTitulo = new JLabel("Tiempo Promedio por Entrega");
        lblTiempoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTiempoTitulo.setForeground(new Color(71, 85, 105));

        JLabel lblTiempoValor = new JLabel(String.format("%.2f s", promedioSegundos));
        lblTiempoValor.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTiempoValor.setForeground(new Color(99, 102, 241));

        cardTiempo.add(lblTiempoTitulo, BorderLayout.WEST);
        cardTiempo.add(lblTiempoValor, BorderLayout.EAST);
        pnlContenido.add(cardTiempo);

        pnlContenido.add(Box.createVerticalStrut(12));

        JPanel cardRepartidores = new JPanel();
        cardRepartidores.setLayout(new BoxLayout(cardRepartidores, BoxLayout.Y_AXIS));
        cardRepartidores.setBackground(Color.WHITE);
        cardRepartidores.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblRepartidoresTitulo = new JLabel("Entregas Individuales por Repartidor");
        lblRepartidoresTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRepartidoresTitulo.setForeground(new Color(30, 41, 59));
        lblRepartidoresTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardRepartidores.add(lblRepartidoresTitulo);
        cardRepartidores.add(Box.createVerticalStrut(8));

        int maxEntregas = 1;
        for (int i = 0; i < 4; i++) {
            if (stats.getPorRepartidor(i) > maxEntregas) {
                maxEntregas = stats.getPorRepartidor(i);
            }
        }

        for (int i = 0; i < 4; i++) {
            int ent = stats.getPorRepartidor(i);
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(450, 22));

            JLabel lblNombre = new JLabel("Repartidor " + (i + 1));
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblNombre.setPreferredSize(new Dimension(95, 20));

            JProgressBar barMini = new JProgressBar(0, maxEntregas);
            barMini.setValue(ent);
            barMini.setForeground(new Color(16, 185, 129));
            barMini.setBackground(new Color(241, 245, 249));
            barMini.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

            JLabel lblCant = new JLabel(ent + " paquetes");
            lblCant.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblCant.setForeground(new Color(71, 85, 105));
            lblCant.setPreferredSize(new Dimension(80, 20));

            fila.add(lblNombre, BorderLayout.WEST);
            fila.add(barMini, BorderLayout.CENTER);
            fila.add(lblCant, BorderLayout.EAST);

            cardRepartidores.add(fila);
            if (i < 3) cardRepartidores.add(Box.createVerticalStrut(6));
        }
        pnlContenido.add(cardRepartidores);

        dialog.add(pnlContenido, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JButton btnCerrar = crearBotonModerno("Cerrar", new Color(71, 85, 105));
        btnCerrar.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnCerrar);
        dialog.add(pnlFooter, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel crearCardKPI(String titulo, String valor, Color colorAcento) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTit.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(colorAcento);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }
}
