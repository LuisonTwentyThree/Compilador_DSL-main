import java.util.*;

/**
 * Generador de Código Ensamblador con Visualización de Estructuras
 * 
 * Traduce cuádruplos a código ensamblador real con capacidades de:
 * - Visualización ASCII de árboles binarios
 * - Seguimiento de operaciones en estructuras de datos
 * - Generación de rutinas de dibujo dinámico
 */
public class GeneradorEnsamblador {
    private List<String> codigoEnsamblador;
    private Map<String, NodoArbol> arboles; // Almacena árboles en memoria
    private Map<String, Queue<Object>> colas; // Almacena colas
    private Map<String, Stack<Object>> pilas; // Almacena pilas
    private Map<String, List<Object>> listas; // Almacena listas
    private int contadorCalls;
    private StringBuilder outputVisualizacion;

    public GeneradorEnsamblador() {
        this.codigoEnsamblador = new ArrayList<>();
        this.arboles = new HashMap<>();
        this.colas = new HashMap<>();
        this.pilas = new HashMap<>();
        this.listas = new HashMap<>();
        this.contadorCalls = 0;
        this.outputVisualizacion = new StringBuilder();
        
        // Agregar encabezado estándar
        agregarEncabezado();
    }

    /**
     * Clase auxiliar para representar nodos de árbol
     */
    public static class NodoArbol {
        public Object clave;
        public Object valor;
        public NodoArbol izquierda;
        public NodoArbol derecha;

        public NodoArbol(Object clave, Object valor) {
            this.clave = clave;
            this.valor = valor;
            this.izquierda = null;
            this.derecha = null;
        }
    }

    /**
     * Añade el encabezado estándar del archivo ensamblador
     */
    private void agregarEncabezado() {
        emitir("; ============================================");
        emitir("; CÓDIGO ENSAMBLADOR GENERADO - DSL");
        emitir("; Con Visualización de Estructuras de Datos");
        emitir("; ============================================");
        emitir("");
        emitir("section .data");
        emitir("    titulo db 'EJECUCION DE PROGRAMA DSL', 0");
        emitir("    newline db 10, 0");
        emitir("    espacio db ' ', 0");
        emitir("    arbolVacio db 'Arbol vacio', 10, 0");
        emitir("");
        emitir("section .text");
        emitir("    global main");
        emitir("    extern printf");
        emitir("");
        emitir("main:");
        emitir("    push rbp");
        emitir("    mov rsp, rbp");
        emitir("");
    }

    /**
     * Traduce un cuádruplo a instrucciones ensamblador
     */
    public void traducirCuadruplo(Cuadruplo c) {
        String op = c.operador.toUpperCase();
        String arg1 = c.argumento1;
        String arg2 = c.argumento2;
        String res = c.resultado;

        switch (op) {
            case "=":
                traducirAsignacion(arg1, res);
                break;

            case "+":
            case "-":
            case "*":
            case "/":
                traducirOperacionMatematica(op, arg1, arg2, res);
                break;

            case "PRINT":
                traducirPrint(arg1);
                break;

            case "AGREGARNODO":
                traducirAgregarNodo(arg1, arg2, res);
                break;

            case "INSERTAR":
                traducirInsertar(arg1, arg2, res);
                break;

            case "APILAR":
                traducirApilar(arg1, res);
                break;

            case "DESAPILAR":
                traducirDesapilar(res);
                break;

            case "ENCOLAR":
                traducirEncolar(arg1, res);
                break;

            case "DESENCOLAR":
                traducirDesencolar(res);
                break;

            case "VACIA":
                traducirVerificaVacia(arg1, res);
                break;

            case "TOPE":
            case "FRENTE":
                traducirObtenerFrente(op, arg1, res);
                break;

            case "DIBUJAR_ARBOL":
                dibujarArbol(res);
                break;

            case "ETIQUETA":
                emitir(res + ":");
                break;

            case "IF_FALSE":
                traducirIfFalse(arg1, res);
                break;

            case "GOTO":
                emitir("    jmp " + res);
                break;

            case "ERROR":
                traducirError(arg1, arg2, res);
                break;

            default:
                emitir("    ; Operación no reconocida: " + op);
        }
    }

    /**
     * Traduce asignación de variables
     */
    private void traducirAsignacion(String valor, String variable) {
        emitir("    ; Asignación: " + variable + " = " + valor);
        emitir("    mov rax, " + valor);
        emitir("    mov [" + variable + "], rax");
    }

    /**
     * Traduce operaciones matemáticas básicas
     */
    private void traducirOperacionMatematica(String op, String arg1, String arg2, String res) {
        emitir("    ; Operación: " + res + " = " + arg1 + " " + op + " " + arg2);
        emitir("    mov rax, [" + arg1 + "]");
        emitir("    mov rbx, [" + arg2 + "]");

        switch (op) {
            case "+":
                emitir("    add rax, rbx");
                break;
            case "-":
                emitir("    sub rax, rbx");
                break;
            case "*":
                emitir("    imul rax, rbx");
                break;
            case "/":
                emitir("    cdq");
                emitir("    idiv rbx");
                break;
        }

        emitir("    mov [" + res + "], rax");
    }

    /**
     * Traduce instrucción PRINT
     */
    private void traducirPrint(String valor) {
        emitir("    ; PRINT: " + valor);
        emitir("    mov rdi, [" + valor + "]");
        emitir("    call printf");
    }

    /**
     * Traduce agregar nodo a árbol binario
     */
    private void traducirAgregarNodo(String clave, String valor, String arbol) {
        emitir("    ; AGREGARNODO: clave=" + clave + ", valor=" + valor + " EN " + arbol);
        emitir("    mov rax, [" + clave + "]");
        emitir("    mov rbx, [" + valor + "]");
        emitir("    call agregar_nodo_" + arbol);
        emitir("    ; Dibujar árbol después de la inserción");
        emitir("    call dibujar_" + arbol);
    }

    /**
     * Traduce insertar en lista enlazada
     */
    private void traducirInsertar(String clave, String valor, String lista) {
        emitir("    ; INSERTAR: clave=" + clave + ", valor=" + valor + " EN " + lista);
        emitir("    mov rax, [" + clave + "]");
        emitir("    mov rbx, [" + valor + "]");
        emitir("    call insertar_" + lista);
    }

    /**
     * Traduce apilar
     */
    private void traducirApilar(String valor, String pila) {
        emitir("    ; APILAR " + valor + " EN " + pila);
        emitir("    mov rax, [" + valor + "]");
        emitir("    call apilar_" + pila);
    }

    /**
     * Traduce desapilar
     */
    private void traducirDesapilar(String pila) {
        emitir("    ; DESAPILAR EN " + pila);
        emitir("    call desapilar_" + pila);
    }

    /**
     * Traduce encolar
     */
    private void traducirEncolar(String valor, String cola) {
        emitir("    ; ENCOLAR " + valor + " EN " + cola);
        emitir("    mov rax, [" + valor + "]");
        emitir("    call encolar_" + cola);
    }

    /**
     * Traduce desencolar
     */
    private void traducirDesencolar(String cola) {
        emitir("    ; DESENCOLAR EN " + cola);
        emitir("    call desencolar_" + cola);
    }

    /**
     * Traduce verificación de vacío
     */
    private void traducirVerificaVacia(String estructura, String resultado) {
        emitir("    ; VACIA: " + estructura);
        emitir("    call vacia_" + estructura);
        emitir("    mov [" + resultado + "], rax");
    }

    /**
     * Traduce obtener frente o tope
     */
    private void traducirObtenerFrente(String operacion, String estructura, String resultado) {
        emitir("    ; " + operacion + ": " + estructura);
        emitir("    call " + operacion.toLowerCase() + "_" + estructura);
        emitir("    mov [" + resultado + "], rax");
    }

    /**
     * Traduce error
     */
    private void traducirError(String msg1, String msg2, String res) {
        emitir("    ; ERROR: " + msg1 + " " + msg2);
        emitir("    mov rdi, msg_error");
        emitir("    call printf");
    }

    /**
     * Traduce IF_FALSE
     */
    private void traducirIfFalse(String condicion, String etiqueta) {
        emitir("    ; IF_FALSE " + condicion + " GOTO " + etiqueta);
        emitir("    cmp [" + condicion + "], 0");
        emitir("    je " + etiqueta);
    }

    /**
     * Emite una línea de código ensamblador
     */
    private void emitir(String linea) {
        codigoEnsamblador.add(linea);
    }

    /**
     * Genera rutinas de dibujo de árboles
     */
    private void generarRutinasArbol() {
        emitir("");
        emitir("; ============================================");
        emitir("; RUTINAS DE VISUALIZACIÓN DE ÁRBOLES");
        emitir("; ============================================");
        emitir("");

        emitir("dibujar_arbol:");
        emitir("    push rbp");
        emitir("    mov rbp, rsp");
        emitir("    ");
        emitir("    ; Inicializar visualización");
        emitir("    mov rdi, newline");
        emitir("    call printf");
        emitir("    ");
        emitir("    ; Llamar a función recursiva de dibujo");
        emitir("    mov rax, [raiz_arbol]");
        emitir("    mov rcx, 0  ; profundidad inicial");
        emitir("    call dibujar_nodo_rec");
        emitir("    ");
        emitir("    mov rdi, newline");
        emitir("    call printf");
        emitir("    pop rbp");
        emitir("    ret");
        emitir("");

        emitir("dibujar_nodo_rec:");
        emitir("    push rbp");
        emitir("    mov rbp, rsp");
        emitir("    ");
        emitir("    ; rax = puntero al nodo");
        emitir("    ; rcx = profundidad");
        emitir("    ");
        emitir("    ; Si nodo es NULL, retornar");
        emitir("    test rax, rax");
        emitir("    jz .fin_nodo");
        emitir("    ");
        emitir("    ; Imprimir indentación (espacios)");
        emitir("    push rcx");
        emitir("    mov r8, rcx");
        emitir(".loop_indent:");
        emitir("    test r8, r8");
        emitir("    jz .fin_indent");
        emitir("    mov rdi, espacio");
        emitir("    call printf");
        emitir("    dec r8");
        emitir("    jmp .loop_indent");
        emitir(".fin_indent:");
        emitir("    pop rcx");
        emitir("    ");
        emitir("    ; Imprimir nodo actual");
        emitir("    mov rdi, formato_nodo");
        emitir("    mov rsi, [rax + 0]   ; clave");
        emitir("    mov rdx, [rax + 8]   ; valor");
        emitir("    call printf");
        emitir("    ");
        emitir("    ; Recursión izquierda");
        emitir("    mov rbx, [rax + 16]  ; hijo izquierdo");
        emitir("    mov rax, rbx");
        emitir("    inc rcx");
        emitir("    call dibujar_nodo_rec");
        emitir("    dec rcx");
        emitir("    ");
        emitir("    ; Recursión derecha (del nodo original)");
        emitir("    mov rax, [rbp + 16]  ; recuperar nodo original");
        emitir("    mov rbx, [rax + 24]  ; hijo derecho");
        emitir("    mov rax, rbx");
        emitir("    inc rcx");
        emitir("    call dibujar_nodo_rec");
        emitir("    ");
        emitir(".fin_nodo:");
        emitir("    pop rbp");
        emitir("    ret");
    }

    /**
     * Genera sección de datos con formatos
     */
    private void generarDatos() {
        emitir("");
        emitir("section .data");
        emitir("    formato_nodo db '[%d:%d] ', 0");
        emitir("    formato_error db 'ERROR: Estructura vacia', 10, 0");
        emitir("    msg_error db 'Error en la operacion', 10, 0");
    }

    /**
     * Genera rutina principal de terminación
     */
    private void generarFinalizacion() {
        emitir("");
        emitir("; ============================================");
        emitir("; FIN DEL PROGRAMA");
        emitir("; ============================================");
        emitir("");
        emitir("    mov rax, 60        ; exit syscall");
        emitir("    mov rdi, 0         ; código de salida");
        emitir("    syscall");
    }

    /**
     * Dibuja un árbol en el output de visualización
     */
    private void dibujarArbol(String nombreArbol) {
        NodoArbol raiz = arboles.get(nombreArbol);
        
        outputVisualizacion.append("\n=== ÁRBOL BINARIO: ").append(nombreArbol).append(" ===\n");
        
        if (raiz == null) {
            outputVisualizacion.append("[ Vacío ]\n");
        } else {
            dibujarNodoRecursivo(raiz, "", true, nombreArbol);
        }
        
        outputVisualizacion.append("\n");
    }

    /**
     * Dibuja recursivamente nodos del árbol en formato ASCII
     */
    private void dibujarNodoRecursivo(NodoArbol nodo, String prefijo, boolean esUltimo, String nombreArbol) {
        if (nodo == null) return;

        outputVisualizacion.append(prefijo);
        outputVisualizacion.append(esUltimo ? "└── " : "├── ");
        outputVisualizacion.append("[").append(nodo.clave).append(":").append(nodo.valor).append("]\n");

        String extension = prefijo + (esUltimo ? "    " : "│   ");

        if (nodo.izquierda != null || nodo.derecha != null) {
            if (nodo.izquierda != null) {
                dibujarNodoRecursivo(nodo.izquierda, extension, nodo.derecha == null, nombreArbol);
            }
            if (nodo.derecha != null) {
                dibujarNodoRecursivo(nodo.derecha, extension, true, nombreArbol);
            }
        }
    }

    /**
     * Obtiene el código ensamblador generado
     */
    public String obtenerCodigoEnsamblador() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigoEnsamblador) {
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }

    /**
     * Obtiene la visualización de estructuras
     */
    public String obtenerVisualizacion() {
        return outputVisualizacion.toString();
    }

    /**
     * Imprime todo el código generado
     */
    public void imprimirCodigoCompleto() {
        System.out.println("--- CÓDIGO ENSAMBLADOR GENERADO ---");
        System.out.println(obtenerCodigoEnsamblador());
        System.out.println("\n--- VISUALIZACIÓN DE ESTRUCTURAS ---");
        System.out.println(obtenerVisualizacion());
    }

    /**
     * Simula la ejecución de un árbol (para testing)
     */
    public void procesarCuadruplos(List<Cuadruplo> cuadruplos) {
        for (Cuadruplo c : cuadruplos) {
            traducirCuadruplo(c);
        }
        
        // Agregar finalizaciones
        generarRutinasArbol();
        generarDatos();
        generarFinalizacion();
    }
}
