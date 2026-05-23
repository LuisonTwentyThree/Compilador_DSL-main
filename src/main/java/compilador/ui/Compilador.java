package compilador.ui;
// importamos las librerias graficas de swing y awt ya que nescesitaremos ventanas, tablas y eventos
// tambien importamos io para el manejo de archivos y util para las listas y regex
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import compilador.codegen.GeneradorCGI;
import compilador.codegen.OptimizadorCGI;
import compilador.codegen.asm.GeneradorEnsambladorGrafico;
import compilador.codegen.asm.IntegradorSalidaASM;
import compilador.core.Cuadruplo;
import compilador.core.NodoAST;
import compilador.lexical.MotorLexico;
import compilador.semantic.MotorSemantico;
import compilador.symbol.TablaErrores;
import compilador.symbol.TablaSimbolos;
import compilador.syntax.MotorSintactico;
import compilador.utils.VentanaReferencia;

public class Compilador extends JFrame {

    // --- Componentes de la Interfaz ---
    private TablaSimbolos ts;
    private TablaErrores te;
    private JTextPane txtEntrada;
    private JTextArea txtNumerosLineas;
    private StyledDocument doc;
    private JTextArea txtSintactico;
    private JTextArea txtCodigoIntermedio;
    private JTextArea txtCodigoOptimizado;
    private JTextArea txtASM;
    private JTextArea txtASMGrafico;
    
    // Archivo de salida ASM seleccionado
    private String nombreBaseSalidaASM = "salida";
    private JLabel lblRutaASM;

    // TABLAS SEPARADAS
    private JTable tablaTokens;
    private DefaultTableModel modeloTokens;
    
    private JTable tablaSimbolos;
    private DefaultTableModel simbolos;
    
    private JTable tablaErrores;
    private DefaultTableModel errores;

    private File archivoActual = null;
    private NodoAST raizAST = null;

    private JLabel lblResumen;

    // estilos para el coloreado de sintaxis
    private Style normal;
    private Style reservada;
    private Style numero;
    private Style operador;
    private Style errorStyle;
    private Style verdeComentario;
    private Style estructuraDato;
    private Style cadenaStyle;

    private Timer timerColoreo;
    private boolean coloreando = false;

    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
            "CREAR", "IF", "ELSE", "MOSTRAR",
            "INSERTAR", "INSERTAR_FINAL", "INSERTAR_INICIO", "INSERTAR_EN_POSICION",
            "INSERTARIZQUIERDA", "INSERTARDERECHA", "AGREGARNODO",
            "APILAR", "ENCOLAR", "PUSH", "ENQUEUE",
            "ELIMINAR", "ELIMINAR_INICIO", "ELIMINAR_FINAL",
            "ELIMINAR_FRENTE", "ELIMINAR_POSICION", "ELIMINARNODO",
            "DESAPILAR", "POP", "DESENCOLAR", "DEQUEUE",
            "BUSCAR",
            "RECORRER", "RECORRERADELANTE", "RECORRERATRAS",
            "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES",
            "BFS", "DFS", "AGREGARARISTA", "ELIMINARARISTA", "CAMINOCORTO",
            "EN", "PESO", "ACTUALIZAR", "REHASH", "VACIA",
            "TOPE", "FRENTE", "FRONT", "PEEK", "VERFILA", "CLAVE",
            "TAMANO", "ALTURA", "HOJAS", "NODOS", "VECINOS", "LLENA",
            "NUMERO", "TEXTO", "FOR", "WHILE", "DO", "VER_FILA", "INSERTAR_FRENTE"
    );

    private static final Set<String> ESTRUCTURAS_DATOS = Set.of(
            "PILA", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_CIRCULAR",
            "ARBOL_BINARIO", "TABLA_HASH", "GRAFO", "PILA_CIRCULAR"
    );

    public Compilador() {

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JMenuBar barraMenu = new JMenuBar();

        // --- MENU ARCHIVO ---
        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem itemAbrir = new JMenuItem("Abrir archivo (.txt)");
        itemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemAbrir.addActionListener(e -> abrirArchivo());

        JMenuItem itemGuardar = new JMenuItem("Guardar");
        itemGuardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemGuardar.addActionListener(e -> guardarArchivo());

        JMenuItem itemGuardarComo = new JMenuItem("Guardar Como...");
        itemGuardarComo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarComo.addActionListener(e -> guardarArchivoComo());

        JMenuItem itemSeleccionarSalidaASM = new JMenuItem("Seleccionar salida ASM...");
        itemSeleccionarSalidaASM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemSeleccionarSalidaASM.addActionListener(e -> seleccionarRutaSalidaASM());

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSeleccionarSalidaASM);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuReferencias = new JMenu("Referencias");

        JMenuItem itemTabla = new JMenuItem("Tabla de Símbolos (Léxico)");
        itemTabla.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        itemTabla.addActionListener(e -> VentanaReferencia.mostrarTablaSimbolos());

        JMenuItem itemGramatica = new JMenuItem("Gramática BNF (Sintáctico)");
        itemGramatica.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        itemGramatica.addActionListener(e -> VentanaReferencia.mostrarGramatica());

        menuReferencias.add(itemTabla);
        menuReferencias.addSeparator();
        menuReferencias.add(itemGramatica);

        this.ts = new TablaSimbolos();
        this.te = new TablaErrores();

        // --- MENU RUN ---
        JMenu menuRun = new JMenu("Run");

        JMenuItem itemRunLexico = new JMenuItem("Analizador Léxico (Solo Tokens)");
        itemRunLexico.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemRunLexico.addActionListener(e -> ejecutarAnalisisSoloLexico());

        JMenuItem itemRunSintactico = new JMenuItem("Analizador Sintáctico (Completo)");
        itemRunSintactico.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        itemRunSintactico.addActionListener(e -> ejecutarAnalisisSintactico());

        menuRun.add(itemRunLexico);
        menuRun.add(itemRunSintactico);

        barraMenu.add(menuArchivo);
        barraMenu.add(menuReferencias);
        barraMenu.add(menuRun);

        setJMenuBar(barraMenu);

        // --- EDITOR DE CÓDIGO ---
        JPanel panelCodigo = new JPanel(new BorderLayout(5, 5));
        panelCodigo.setBorder(BorderFactory.createTitledBorder(" Editor de Código DSL "));

        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtEntrada.setText("");

        doc = txtEntrada.getStyledDocument();
        inicializarEstilos();

        txtNumerosLineas = new JTextArea("1");
        txtNumerosLineas.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtNumerosLineas.setBackground(new Color(230, 230, 230));
        txtNumerosLineas.setForeground(Color.GRAY);
        txtNumerosLineas.setEditable(false);
        txtNumerosLineas.setMargin(new Insets(0, 5, 0, 5));

        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setRowHeaderView(txtNumerosLineas);
        scrollCodigo.setPreferredSize(new Dimension(1000, 200));

        JButton btnAnalizar = new JButton("Compilar / Ejecutar");
        btnAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setBackground(new Color(0, 120, 215));
        btnAnalizar.setFocusPainted(false);
        btnAnalizar.setBorderPainted(false);
        btnAnalizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnalizar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnAnalizar.addActionListener(e -> ejecutarAnalisisSintactico());

        btnAnalizar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAnalizar.setBackground(new Color(0, 90, 170));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAnalizar.setBackground(new Color(0, 120, 215));
            }
        });

        panelCodigo.add(scrollCodigo, BorderLayout.CENTER);
        panelCodigo.add(btnAnalizar, BorderLayout.EAST);

        // --- PESTAÑAS DE RESULTADOS ---
        JTabbedPane pestañas = new JTabbedPane();

        // 1. DICCIONARIO
        String[] colsDiccionario = {"Código", "Categoría", "Significado / Solución"};
        DefaultTableModel modeloDic = new DefaultTableModel(getDatosDiccionario(), colsDiccionario) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tablaDiccionario = new JTable(modeloDic);
        tablaDiccionario.setRowHeight(25);
        tablaDiccionario.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaDiccionario.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaDiccionario.getColumnModel().getColumn(2).setPreferredWidth(400);
        tablaDiccionario.setBackground(new Color(245, 245, 250));

        // 2. TABLA DE TOKENS (NUEVO)
        String[] colsTokens = {"Lexema", "Línea", "Col", "Tipo Token"};
        modeloTokens = new DefaultTableModel(colsTokens, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaTokens = new JTable(modeloTokens);
        tablaTokens.setFillsViewportHeight(true);
        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        pestañas.addTab("Tabla de Tokens (Léxico)", scrollTokens);

        // 3. TABLA DE SÍMBOLOS REAL (ACTUALIZADO)
        String[] colsSimbolos = {"Nombre", "Tipo", "Valor"};
        simbolos = new DefaultTableModel(colsSimbolos, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaSimbolos = new JTable(simbolos);
        tablaSimbolos.setFillsViewportHeight(true);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        pestañas.addTab("Tabla de Símbolos (Memoria)", scrollSimbolos);

        // 4. ÁRBOL SINTÁCTICO
        txtSintactico = new JTextArea();
        txtSintactico.setEditable(false);
        txtSintactico.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtSintactico.setForeground(new Color(40, 40, 40));
        JScrollPane scrollSintactico = new JScrollPane(txtSintactico);

        JPanel panelArbol = new JPanel(new BorderLayout());
        panelArbol.add(scrollSintactico, BorderLayout.CENTER);

        JButton btnGuardarArbol = new JButton("Guardar Árbol");
        btnGuardarArbol.addActionListener(e -> imprimirArbolSintactico());
        panelArbol.add(btnGuardarArbol, BorderLayout.SOUTH);

        pestañas.addTab("Árbol Sintáctico (AST)", panelArbol);

        // --- INICIO CÓDIGO NUEVO: PESTAÑA DE CÓDIGO INTERMEDIO ---
        txtCodigoIntermedio = new JTextArea();
        txtCodigoIntermedio.setEditable(false);
        txtCodigoIntermedio.setFont(new Font("Consolas", Font.BOLD, 14));
        txtCodigoIntermedio.setForeground(new Color(0, 50, 150)); // Un azul oscuro para que resalte
        JScrollPane scrollCGI = new JScrollPane(txtCodigoIntermedio);
        pestañas.addTab("Código Intermedio (C3D)", scrollCGI);
        // --- PESTAÑA: CÓDIGO OPTIMIZADO ---
        txtCodigoOptimizado = new JTextArea();
        txtCodigoOptimizado.setEditable(false);
        txtCodigoOptimizado.setFont(new Font("Consolas", Font.BOLD, 14));
        txtCodigoOptimizado.setForeground(new Color(0, 100, 0)); // verde oscuro
        JScrollPane scrollOpt = new JScrollPane(txtCodigoOptimizado);
        pestañas.addTab("Código Optimizado", scrollOpt);
        // --- PESTAÑA: ENSAMBLADOR (ASM) ---
        txtASM = new JTextArea();
        txtASM.setEditable(false);
        txtASM.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtASM.setForeground(new Color(30, 30, 30));
        JScrollPane scrollASM = new JScrollPane(txtASM);
        txtASMGrafico = new JTextArea();
        txtASMGrafico.setEditable(false);
        txtASMGrafico.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtASMGrafico.setForeground(new Color(45, 45, 45));
        JScrollPane scrollASMGrafico = new JScrollPane(txtASMGrafico);
        pestañas.addTab("Ensamblador Grafico (ASM)", scrollASMGrafico);
        pestañas.addTab("Ensamblador (ASM)", scrollASM);
        // --- FIN CÓDIGO NUEVO ---
        // 5. ERRORES
        String[] colsErrores = {"Línea", "Fase", "Descripción del Error"};
        errores = new DefaultTableModel(colsErrores, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaErrores = new JTable(errores);
        tablaErrores.setFillsViewportHeight(true);
        tablaErrores.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaErrores.getColumnModel().getColumn(1).setMaxWidth(100);

        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        pestañas.addTab("Errores Encontrados", scrollErrores);
        
        // Se agrega el diccionario al final
        pestañas.addTab("Diccionario de Errores", new JScrollPane(tablaDiccionario));

        lblResumen = new JLabel(" Esperando código...");
        lblResumen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        lblRutaASM = new JLabel("Salida ASM: " + nombreBaseSalidaASM + ".asm");
        lblRutaASM.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRutaASM.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(lblResumen, BorderLayout.WEST);
        panelInferior.add(lblRutaASM, BorderLayout.EAST);

        add(panelCodigo, BorderLayout.NORTH);
        add(pestañas, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        timerColoreo = new Timer(500, e -> colorearTexto());
        timerColoreo.setRepeats(false);

        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                actualizarLineas();
                timerColoreo.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                actualizarLineas();
                timerColoreo.restart();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        actualizarLineas();
        colorearTexto();
    }

    private void inicializarEstilos() {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        normal = sc.addStyle("normal", null);
        StyleConstants.setForeground(normal, Color.BLACK);

        reservada = sc.addStyle("reservada", null);
        StyleConstants.setForeground(reservada, Color.BLUE);
        StyleConstants.setBold(reservada, true);

        numero = sc.addStyle("numero", null);
        StyleConstants.setForeground(numero, new Color(150, 0, 150));

        operador = sc.addStyle("operador", null);
        StyleConstants.setForeground(operador, Color.DARK_GRAY);

        errorStyle = sc.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, Color.RED);

        verdeComentario = sc.addStyle("comentario", null);
        StyleConstants.setForeground(verdeComentario, new Color(0, 128, 0));
        StyleConstants.setItalic(verdeComentario, true);

        estructuraDato = sc.addStyle("estructura", null);
        StyleConstants.setForeground(estructuraDato, new Color(255, 140, 0));
        StyleConstants.setBold(estructuraDato, true);

        cadenaStyle = sc.addStyle("cadena", null);
        StyleConstants.setForeground(cadenaStyle, new Color(200, 20, 20));
    }

    private void actualizarLineas() {
        int lineas = txtEntrada.getText().split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineas; i++) {
            sb.append(i).append("\n");
        }
        txtNumerosLineas.setText(sb.toString());
    }

    private void limpiarTablas() {
        if(modeloTokens != null) modeloTokens.setRowCount(0);
        if(simbolos != null) simbolos.setRowCount(0);
        if(errores != null) errores.setRowCount(0);
        txtSintactico.setText("");
        if(txtCodigoIntermedio != null) txtCodigoIntermedio.setText(""); // <--- AGREGA ESTA LÍNEA
        if(txtCodigoOptimizado != null) txtCodigoOptimizado.setText("");
        if(txtASM != null) txtASM.setText("");
        if(txtASMGrafico != null) txtASMGrafico.setText("");
        raizAST = null;
        lblResumen.setForeground(Color.BLACK);
        lblResumen.setText("Analizando...");
    }

    private void ejecutarAnalisisSoloLexico() {
        limpiarTablas();
        String codigo = txtEntrada.getText();

        MotorLexico.ResultadoLexico resultado = MotorLexico.ejecutar(codigo);

        for (Object[] fila : resultado.datosSimbolos) {
            modeloTokens.addRow(fila); // <--- Llenamos la nueva tabla de tokens
        }

        for (MotorLexico.ErrorDatos err : resultado.errores) {
            errores.addRow(new Object[]{err.linea, "LÉXICO", err.descripcion});
        }

        if (resultado.errores.isEmpty()) {
            lblResumen.setText(" Análisis LÉXICO finalizado con ÉXITO.");
            lblResumen.setForeground(new Color(0, 128, 0));
            txtSintactico.setText("--- Modo Solo Léxico ---\nEl árbol no se genera en este modo.");
        } else {
            lblResumen.setText(" Se encontraron " + resultado.errores.size() + " errores léxicos.");
            lblResumen.setForeground(Color.RED);
        }
    }

    /* * MÉTODO CORREGIDO QUE INTEGRA EL MOTOR SEMÁNTICO Y UNIFICA LAS TABLAS DE ERRORES 
     */
    private void ejecutarAnalisisSintactico() {
        limpiarTablas();
        String codigo = txtEntrada.getText();

        // 1. Ejecutamos léxico
        MotorLexico.ResultadoLexico resLexico = MotorLexico.ejecutar(codigo);

        // --- SOLUCIÓN 1: LLENAR LA TABLA DE TOKENS (LÉXICO) ---
        if (resLexico.datosSimbolos != null) {
            for (Object[] fila : resLexico.datosSimbolos) {
                modeloTokens.addRow(fila); // <--- Llenamos la nueva tabla de tokens
            }
        }

        // 2. Ejecutamos sintáctico con los tokens válidos
        MotorSintactico.ResultadoSintactico resSintactico = MotorSintactico.ejecutar(resLexico.tokensValidos);

        // Preparamos las tablas asegurando que no sean nulas
        TablaSimbolos tsActual = resSintactico.tablaSimbolos != null ? resSintactico.tablaSimbolos : new TablaSimbolos();
        TablaErrores teActual = resSintactico.tablaErrores != null ? resSintactico.tablaErrores : new TablaErrores();

        // 3. ¡FASE SEMÁNTICA! 
        MotorSemantico.ejecutar(resSintactico.raiz, tsActual, teActual);

        // Sincronizamos las variables globales de la ventana 
        this.ts = tsActual;
        this.te = teActual;
        this.raizAST = resSintactico.raiz;

        // --- SOLUCIÓN 2: LLENAR LA VERDADERA TABLA DE SÍMBOLOS ---
        if (this.ts != null) {
            java.util.Map<String, TablaSimbolos.Simbolo> variablesMemoria = this.ts.getTodosLosSimbolos();
            if (variablesMemoria != null) {
                for (TablaSimbolos.Simbolo sim : variablesMemoria.values()) {
                    // Llenamos la tabla gráfica de memoria con {Nombre, Tipo, Valor}
                    simbolos.addRow(new Object[]{sim.nombre, sim.tipo, sim.valor});
                }
            }
        }

        // 4. Actualizamos el Árbol en pantalla
        txtSintactico.setText(resSintactico.logArbol);
        txtSintactico.setCaretPosition(0);

        // 5. --- SOLUCIÓN 3: RECOPILAR ERRORES CON VALIDACIÓN ---
        int totalErrores = 0;

        // a) Errores Léxicos
        if (resLexico.errores != null) {
            for (MotorLexico.ErrorDatos err : resLexico.errores) {
                errores.addRow(new Object[]{err.linea, "LÉXICO", err.descripcion});
                totalErrores++;
            }
        }

        // b) Errores Sintácticos
        if (resSintactico.errores != null) {
            for (MotorSintactico.ErrorDatosSintactico err : resSintactico.errores) {
                errores.addRow(new Object[]{err.linea, "SINTÁCTICO", err.descripcion});
                totalErrores++;
            }
        }

        // c) Errores Semánticos (Desde la TablaErrores)
        if (teActual != null) {
            Object[][] datosSemanticos = teActual.getDatosParaTabla();
            if (datosSemanticos != null) {
                for (Object[] filaError : datosSemanticos) {
                    errores.addRow(filaError);
                    totalErrores++;
                }
            }
        }

        // 6. Actualizamos el label de resumen final y Generamos CGI
        if (totalErrores == 0) {
            lblResumen.setText(" Compilación exitosa (0 errores). Generando Código Intermedio...");
            lblResumen.setForeground(new Color(0, 128, 0));
            
            // --- INICIO: GENERAR CÓDIGO INTERMEDIO ---
            try {
                GeneradorCGI generador = new GeneradorCGI();
                java.util.List<Cuadruplo> codigoGenerado = generador.generar(resSintactico.raiz);
                
                StringBuilder sbCGI = new StringBuilder();
                sbCGI.append("--- CÓDIGO INTERMEDIO (TRES DIRECCIONES) ---\n\n");
                for (Cuadruplo c : codigoGenerado) {
                    sbCGI.append(c.toString()).append("\n");
                }
                txtCodigoIntermedio.setText(sbCGI.toString());
                txtCodigoIntermedio.setCaretPosition(0);
                // --- INICIO: OPTIMIZAR CÓDIGO INTERMEDIO ---
                try {
                    OptimizadorCGI optimizador = new OptimizadorCGI();
                    java.util.List<Cuadruplo> codigoOptimizado = optimizador.optimizar(codigoGenerado);
                    StringBuilder sbOpt = new StringBuilder();
                    sbOpt.append("--- CÓDIGO INTERMEDIO OPTIMIZADO ---\n\n");
                    for (Cuadruplo c : codigoOptimizado) {
                        sbOpt.append(c.toString()).append("\n");
                    }
                    txtCodigoOptimizado.setText(sbOpt.toString());
                    txtCodigoOptimizado.setCaretPosition(0);
                    // --- INTEGRAR GENERACIÓN ASM CON EL INTEGRADOR EXISTENTE ---
                    try {
                        // IntegradorSalidaASM no es solo salida por terminal:
                        // recibe los cuadruplos optimizados, genera el .asm normal
                        // y mantiene el codigo en memoria para mostrarlo en txtASM.
                        IntegradorSalidaASM integrador = new IntegradorSalidaASM(nombreBaseSalidaASM);
                        for (Cuadruplo c : codigoOptimizado) {
                            integrador.agregarCuadruplo(c);
                        }
                        integrador.generarArchivosCompletos();
                        // Aqui se pasa el resultado al modo grafico de la aplicacion
                        // Swing: el texto generado se muestra en la pestana/panel ASM.
                        txtASM.setText(integrador.obtenerCodigoEnsamblador());
                        txtASM.setCaretPosition(0);
                        // Esta salida grafica es independiente: usa los mismos
                        // cuadruplos, pero los traduce con GeneradorEnsambladorGrafico.
                        generarEnsambladorGrafico(codigoOptimizado);
                        lblResumen.setText(" Compilación exitosa. Archivos ASM generados: " + nombreBaseSalidaASM + ".asm");
                        lblResumen.setForeground(new Color(0, 128, 0));
                    } catch (Exception exAsm) {
                        txtASM.setText("Error al generar ASM:\n" + exAsm.getMessage());
                    }
                } catch (Exception exOpt) {
                    txtCodigoOptimizado.setText("Error durante la optimización:\n" + exOpt.getMessage());
                }
                // --- FIN: OPTIMIZAR CÓDIGO INTERMEDIO ---
                
            } catch (Exception e) {
                txtCodigoIntermedio.setText("Error durante la generación de código intermedio:\n" + e.getMessage());
                lblResumen.setText(" Error al generar CGI.");
                lblResumen.setForeground(Color.RED);
            }
            // --- FIN: GENERAR CÓDIGO INTERMEDIO ---
            
        } else {
            lblResumen.setText(" Se encontraron " + totalErrores + " errores en total.");
            lblResumen.setForeground(Color.RED);
            txtCodigoIntermedio.setText(""); // Lo dejamos vacío si hay errores
        }
    }

    // --- MÉTODOS DE ARCHIVO ---
    private void generarEnsambladorGrafico(java.util.List<Cuadruplo> codigoOptimizado) {
        try {
            GeneradorEnsambladorGrafico generadorGrafico = new GeneradorEnsambladorGrafico();
            generadorGrafico.procesarCuadruplos(codigoOptimizado);
            String asmGrafico = generadorGrafico.obtenerCodigoEnsamblador();

            if (txtASMGrafico != null) {
                txtASMGrafico.setText(asmGrafico);
                txtASMGrafico.setCaretPosition(0);
            }

            String archivoGrafico = nombreBaseSalidaASM + "_grafico.asm";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoGrafico))) {
                writer.write(asmGrafico);
            }
        } catch (Exception exGrafico) {
            if (txtASMGrafico != null) {
                txtASMGrafico.setText("Error al generar ASM grafico:\n" + exGrafico.getMessage());
            }
        }
    }

    private void abrirArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Abrir archivo de código DSL");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));

        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoActual = selector.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(archivoActual))) {
                txtEntrada.setText("");
                String linea;
                while ((linea = br.readLine()) != null) {
                    doc.insertString(doc.getLength(), linea + "\n", normal);
                }
                setTitle("Analizador DSL - " + archivoActual.getName());
                colorearTexto();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarArchivo() {
        if (archivoActual != null) {
            escribirArchivo(archivoActual, txtEntrada.getText());
        } else {
            guardarArchivoComo();
        }
    }

    private void guardarArchivoComo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar código como...");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoAGuardar = selector.getSelectedFile();
            if (!archivoAGuardar.getName().toLowerCase().endsWith(".txt")) {
                archivoAGuardar = new File(archivoAGuardar.getAbsolutePath() + ".txt");
            }
            escribirArchivo(archivoAGuardar, txtEntrada.getText());
            archivoActual = archivoAGuardar;
            setTitle("Analizador DSL - " + archivoActual.getName());
        }
    }

    private void seleccionarRutaSalidaASM() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar ruta de salida ASM");
        selector.setSelectedFile(new File(nombreBaseSalidaASM + ".asm"));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo ASM (.asm)", "asm"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = selector.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".asm")) {
                ruta = ruta.substring(0, ruta.length() - 4);
            }
            nombreBaseSalidaASM = ruta;
            lblRutaASM.setText("Salida ASM: " + ruta + ".asm");
            lblResumen.setText(" Ruta de salida ASM actualizada.");
            lblResumen.setForeground(new Color(0, 128, 0));
        }
    }

    private void imprimirArbolSintactico() {
        String contenidoArbol = txtSintactico.getText();
        if (contenidoArbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El árbol está vacío. Primero ejecuta el análisis.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar Árbol Sintáctico");
        selector.setSelectedFile(new File("arbol_sintactico.txt"));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoAGuardar = selector.getSelectedFile();
            if (!archivoAGuardar.getName().toLowerCase().endsWith(".txt")) {
                archivoAGuardar = new File(archivoAGuardar.getAbsolutePath() + ".txt");
            }
            escribirArchivo(archivoAGuardar, contenidoArbol);
            JOptionPane.showMessageDialog(this, "Árbol guardado con éxito.");
        }
    }

    private void escribirArchivo(File archivo, String contenido) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write(contenido);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS DE COLOREADO ---
    private void colorearTexto() {

        if (coloreando) {
            return;
        }
        coloreando = true;

        SwingUtilities.invokeLater(() -> {

            try {

                int posicionCursor = txtEntrada.getCaretPosition();

                String texto = doc.getText(0, doc.getLength());

                // limpiar estilos
                doc.setCharacterAttributes(0, texto.length(), normal, true);

                Pattern patron = Pattern.compile(
                        "//.*"
                        + // comentario
                        "|\"[^\"]*\""
                        + // cadena
                        "|\\b\\d+\\b"
                        + // número
                        "|\\b[A-Za-z_][A-Za-z0-9_]*\\b" // palabra
                );

                Matcher m = patron.matcher(texto);

                while (m.find()) {

                    String token = m.group();
                    int inicio = m.start();
                    int longitud = m.end() - m.start();

                    if (token.startsWith("//")) {

                        doc.setCharacterAttributes(inicio, longitud, verdeComentario, false);

                    } else if (token.startsWith("\"")) {

                        doc.setCharacterAttributes(inicio, longitud, cadenaStyle, false);

                    } else if (token.matches("\\d+")) {

                        doc.setCharacterAttributes(inicio, longitud, numero, false);

                    } else {

                        String palabra = token.toUpperCase();

                        if (ESTRUCTURAS_DATOS.contains(palabra)) {

                            doc.setCharacterAttributes(inicio, longitud, estructuraDato, false);

                        } else if (PALABRAS_RESERVADAS.contains(palabra)) {

                            doc.setCharacterAttributes(inicio, longitud, reservada, false);

                        }
                    }
                }

                // restaurar cursor
                txtEntrada.setCaretPosition(Math.min(posicionCursor, doc.getLength()));

            } catch (Exception ex) {

                ex.printStackTrace();

            } finally {

                coloreando = false;

            }

        });
    }

    private Object[][] getDatosDiccionario() {
        return new Object[][]{
            {"DSL(101)", "Léxico", "Símbolo no reconocido en el alfabeto del lenguaje."},
            {"DSL(102)", "Léxico", "Cadena de texto (string) sin cerrar o mal formada."},
            {"DSL(103)", "Léxico", "Número o identificador mal formado (ej. 123abc)."},
            {"DSL(201)", "Sintáctico", "Falta el punto y coma (;) al final de la sentencia."},
            {"DSL(202)", "Sintáctico", "Falta un delimitador de bloque ( } ) o paréntesis ( ) )."},
            {"DSL(203)", "Sintáctico", "Sentencia no válida o palabra reservada mal posicionada."},
            {"DSL(204)", "Sintáctico", "Tipo de estructura de datos desconocido o no soportado."},
            {"DSL(205)", "Sintáctico", "Falta la palabra clave 'EN' necesaria para la operación."},
            {"DSL(206)", "Sintáctico", "Se esperaba un operador relacional (==, !=, <, >, etc)."},
            {"DSL(207)", "Sintáctico", "Expresión matemática o lógica mal formada."},
            {"DSL(301)", "Semántico", "La variable no ha sido declarada previamente con CREAR."},
            {"DSL(302)", "Semántico", "La variable ya existe en la Tabla de Símbolos."},
            {"DSL(303)", "Semántico", "Incompatibilidad de tipos en la operación (ej. Numero + Texto)."},
            {"DSL(304)", "Semántico", "Comando no compatible con el tipo de estructura de datos utilizada."}
        };
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Compilador().setVisible(true));
        
    }
}
