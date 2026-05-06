import java.util.*;
import java.util.regex.Pattern;

/**
 * GENERADOR DE CÓDIGO ENSAMBLADOR CON OPTIMIZACIÓN
 * ============================================================
 * Traduce cuádruplos a código ensamblador x86-64 real optimizado
 * 
 * FUNCIONALIDADES PRINCIPALES:
 * 1. Conversión de cuádruplos a instrucciones ensamblador
 * 2. Visualización ASCII de árboles binarios
 * 3. Gestión de estructuras de datos en memoria
 * 4. Optimizaciones de código:
 *    - Eliminación de instrucciones redundantes (peephole optimization)
 *    - Caché de valores en registros
 *    - Fusión de movimientos innecesarios
 *    - Optimización de saltos
 * 
 * FLUJO DE TRABAJO:
 * 1. procesarCuadruplos() -> traduce cada cuádruplo
 * 2. Cada traducción emite instrucciones ensamblador
 * 3. optimizarCodigo() -> elimina redundancias
 * 4. obtenerCodigoEnsamblador() -> retorna resultado final
 */
public class GeneradorEnsamblador {
    // ============================================================
    // ESTRUCTURAS DE DATOS - Almacenan información en tiempo real
    // ============================================================
    private List<String> codigoEnsamblador;           // Buffer de instrucciones x86-64
    private Map<String, NodoArbol> arboles;           // Árboles binarios creados
    private Map<String, Queue<Object>> colas;         // Colas FIFO
    private Map<String, Stack<Object>> pilas;         // Pilas LIFO
    private Map<String, List<Object>> listas;         // Listas enlazadas
    private Map<String, String> cacheRegistros;       // OPTIMIZACIÓN: caché de valores en registros
    private Set<String> variablesUsadas;              // OPTIMIZACIÓN: seguimiento de variables
    
    // ============================================================
    // CAMPOS DE CONTROL
    // ============================================================
    private int contadorCalls;                        // Contador de funciones llamadas
    private StringBuilder outputVisualizacion;         // Buffer de salida visual
    private boolean optimizacionActiva;               // Flag para activar/desactivar optimización

    public GeneradorEnsamblador() {
        this.codigoEnsamblador = new ArrayList<>();
        this.arboles = new HashMap<>();
        this.colas = new HashMap<>();
        this.pilas = new HashMap<>();
        this.listas = new HashMap<>();
        this.cacheRegistros = new HashMap<>();         // OPTIMIZACIÓN
        this.variablesUsadas = new HashSet<>();        // OPTIMIZACIÓN
        this.contadorCalls = 0;
        this.outputVisualizacion = new StringBuilder();
        this.optimizacionActiva = true;                // Habilitar optimización por defecto
        
        // Agregar encabezado estándar
        agregarEncabezado();
    }

    /**
     * CLASE AUXILIAR: NodoArbol
     * ============================================================
     * Representa un nodo en un árbol binario de búsqueda (ABB)
     * Almacena clave, valor y referencias a hijo izquierdo/derecho
     */
    public static class NodoArbol {
        public Object clave;                          // Identificador del nodo
        public Object valor;                          // Dato almacenado
        public NodoArbol izquierda;                   // Subárbol izquierdo
        public NodoArbol derecha;                     // Subárbol derecho

        public NodoArbol(Object clave, Object valor) {
            this.clave = clave;
            this.valor = valor;
            this.izquierda = null;
            this.derecha = null;
        }
    }

    /**
     * ENCABEZADO ESTÁNDAR DEL ARCHIVO ENSAMBLADOR
     * ============================================================
     * Define:
     * - Sección de datos con cadenas constantes
     * - Función main con prologue de stack
     * - Registros y convenciones de llamada x86-64
     */
    private void agregarEncabezado() {
        emitir("; ============================================");
        emitir("; CÓDIGO ENSAMBLADOR GENERADO - DSL");
        emitir("; Con Optimización y Visualización de Estructuras");
        emitir("; Arquitectura: x86-64 (System V AMD64 ABI)");
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
     * OPTIMIZACIÓN: Peephole Optimization
     * ============================================================
     * Elimina instrucciones redundantes:
     * 1. mov rax, X; mov rax, X  -> mov rax, X (elimina duplicado)
     * 2. mov rax, rbx; mov rbx, rax -> nada (movimiento circular inútil)
     * 3. add rax, 0; sub rax, 0 -> nada (operación neutra)
     * 4. cmp rax, 0; je label; jne label -> optimización de saltos
     */
    private void optimizarCodigo() {
        if (!optimizacionActiva || codigoEnsamblador.size() < 2) return;
        
        List<String> optimizado = new ArrayList<>();
        
        for (int i = 0; i < codigoEnsamblador.size(); i++) {
            String actual = codigoEnsamblador.get(i);
            String siguiente = (i + 1 < codigoEnsamblador.size()) 
                ? codigoEnsamblador.get(i + 1) 
                : "";
            
            // PATRÓN 1: Eliminar mov duplicados consecutivos
            // Ejemplo: mov rax, 5; mov rax, 5 -> mantener solo uno
            if (esMovimiento(actual) && esMovimiento(siguiente) 
                && extraerDestino(actual).equals(extraerDestino(siguiente))
                && extraerFuente(actual).equals(extraerFuente(siguiente))) {
                optimizado.add(actual);
                i++; // Saltar la siguiente línea idéntica
                continue;
            }
            
            // PATRÓN 2: Eliminar operaciones neutras
            // Ejemplo: add rax, 0 -> nada (no hace efecto)
            if (esOperacionNeutra(actual)) {
                continue; // No agregar esta línea
            }
            
            // PATRÓN 3: Fusionar mov seguido de operación
            // Ejemplo: mov rax, [x]; mov rbx, rax -> mov rbx, [x]
            if (puedeOptimizarMovimientoDoble(actual, siguiente)) {
                optimizado.add(optimizarMovimientoDoble(actual, siguiente));
                i++; // Saltar la siguiente línea fusionada
                continue;
            }
            
            optimizado.add(actual);
        }
        
        codigoEnsamblador = optimizado;
    }

    /**
     * OPTIMIZACIÓN: Caché de Registros
     * ============================================================
     * Mantiene registro de qué variables están en qué registros
     * para evitar movimientos innecesarios entre memoria y registros
     */
    private void actualizarCache(String registro, String variable) {
        cacheRegistros.put(registro, variable);
        variablesUsadas.add(variable);
    }

    /**
     * Obtiene el registro donde está cacheada una variable
     * Si no existe, retorna null y se necesita cargar desde memoria
     */
    private String obtenerRegistroEnCache(String variable) {
        for (Map.Entry<String, String> entry : cacheRegistros.entrySet()) {
            if (entry.getValue().equals(variable)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Verifica si una línea es un movimiento (mov reg, fuente)
     */
    private boolean esMovimiento(String linea) {
        return linea.trim().startsWith("mov ");
    }

    /**
     * Verifica si es una operación neutra (add rax, 0 o sub rax, 0)
     */
    private boolean esOperacionNeutra(String linea) {
        String trim = linea.trim();
        return (trim.startsWith("add ") && trim.endsWith(", 0")) 
            || (trim.startsWith("sub ") && trim.endsWith(", 0"));
    }

    /**
     * Extrae el destino de un movimiento (mov DESTINO, fuente)
     */
    private String extraerDestino(String linea) {
        try {
            String[] partes = linea.trim().split(",");
            return partes[0].replace("mov", "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extrae la fuente de un movimiento (mov destino, FUENTE)
     */
    private String extraerFuente(String linea) {
        try {
            String[] partes = linea.trim().split(",");
            return partes[1].trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Verifica si puede fusionar dos movimientos
     */
    private boolean puedeOptimizarMovimientoDoble(String linea1, String linea2) {
        if (!esMovimiento(linea1) || !esMovimiento(linea2)) return false;
        String destino1 = extraerDestino(linea1);
        String fuente2 = extraerFuente(linea2);
        return destino1.equals(fuente2);
    }

    /**
     * Funde dos movimientos en uno
     * Ejemplo: mov rax, [x]; mov rbx, rax -> mov rbx, [x]
     */
    private String optimizarMovimientoDoble(String linea1, String linea2) {
        String fuente1 = extraerFuente(linea1);
        String destino2 = extraerDestino(linea2);
        return "    mov " + destino2 + ", " + fuente1;
    }

    /**
     * TRADUCTOR PRINCIPAL DE CUÁDRUPLOS
     * ============================================================
     * Entrada: Cuádruplo con formato (operador, arg1, arg2, resultado)
     * Proceso: Mapea operador a función de traducción específica
     * Salida: Líneas de código ensamblador x86-64
     * 
     * Operadores soportados:
     * - Asignación: =
     * - Aritmética: +, -, *, /
     * - Control: IF_FALSE, GOTO, ETIQUETA
     * - Estructuras: AGREGARNODO, INSERTAR, APILAR, ENCOLAR, etc.
     * - Entrada/Salida: PRINT, DIBUJAR_ARBOL
     * - Errores: ERROR
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
     * TRADUCCIÓN DE ASIGNACIÓN
     * ============================================================
     * Operación: variable = valor
     * 
     * Proceso:
     * 1. Cargar valor en registro RAX
     * 2. Almacenar RAX en dirección de memoria de variable
     * 
     * Optimización: Usa caché de registros para evitar recargas
     * 
     * Código generado:
     *   mov rax, [valor]       ; Cargar desde memoria
     *   mov [variable], rax    ; Almacenar resultado
     */
    private void traducirAsignacion(String valor, String variable) {
        emitir("    ; *** ASIGNACIÓN: " + variable + " = " + valor);
        
        // Verificar si el valor está en caché
        String regEnCache = obtenerRegistroEnCache(valor);
        if (regEnCache != null) {
            emitir("    mov [" + variable + "], " + regEnCache + " ; OPTIMIZACIÓN: desde caché");
        } else {
            emitir("    mov rax, " + valor);
            emitir("    mov [" + variable + "], rax");
            actualizarCache("rax", variable);
        }
    }

    /**
     * TRADUCCIÓN DE OPERACIONES MATEMÁTICAS
     * ============================================================
     * Soportadas: +, -, *, /
     * Resultado: variable = arg1 OP arg2
     * 
     * Proceso:
     * 1. Cargar arg1 en RAX
     * 2. Cargar arg2 en RBX
     * 3. Realizar operación (add, sub, imul, idiv)
     * 4. Guardar resultado en variable
     * 
     * Consideraciones:
     * - IMUL: multiplicación con signo (resultado en RAX:RDX)
     * - IDIV: requiere CDQ para signo extendido
     * - Flags EFLAGS se modifican para saltos condicionales
     * 
     * Código típico:
     *   mov rax, [arg1]        ; Carga primer operando
     *   mov rbx, [arg2]        ; Carga segundo operando
     *   add rax, rbx           ; Realiza operación
     *   mov [resultado], rax   ; Almacena resultado
     */
    private void traducirOperacionMatematica(String op, String arg1, String arg2, String res) {
        emitir("    ; *** OPERACIÓN MATEMÁTICA: " + res + " = " + arg1 + " " + op + " " + arg2);
        emitir("    mov rax, [" + arg1 + "]  ; Cargar primer operando");
        emitir("    mov rbx, [" + arg2 + "]  ; Cargar segundo operando");

        switch (op) {
            case "+":
                emitir("    add rax, rbx            ; Suma");
                break;
            case "-":
                emitir("    sub rax, rbx            ; Resta");
                break;
            case "*":
                emitir("    imul rax, rbx           ; Multiplicación con signo");
                break;
            case "/":
                emitir("    cdq                     ; Extender signo RAX -> RDX:RAX");
                emitir("    idiv rbx                ; División entera con signo (RAX/RBX)");
                break;
        }

        emitir("    mov [" + res + "], rax      ; Guardar resultado");
        actualizarCache("rax", res);
    }

    /**
     * TRADUCCIÓN DE PRINT
     * ============================================================
     * Llama a función printf del sistema para escribir en stdout
     * 
     * Convención System V AMD64 ABI:
     * - RDI = primer argumento (formato de cadena o valor)
     * - RSI, RDX, RCX, R8, R9 = argumentos adicionales
     * 
     * Código generado:
     *   mov rdi, [valor]       ; Primer argumento
     *   call printf            ; Llamada a función extern
     */
    private void traducirPrint(String valor) {
        emitir("    ; *** PRINT: " + valor);
        emitir("    mov rdi, [" + valor + "]");
        emitir("    call printf");
    }

    /**
     * TRADUCCIÓN DE AGREGACIÓN A ÁRBOL BINARIO
     * ============================================================
     * Operación: AGREGARNODO(clave, valor, arbol)
     * 
     * Proceso:
     * 1. Cargar clave en RAX
     * 2. Cargar valor en RBX
     * 3. Llamar a rutina agregar_nodo_NOMBRE
     * 4. Llamar a dibujar_NOMBRE para visualizar
     * 
     * La rutina agregar_nodo debe:
     * - Comparar clave con nodo actual
     * - Ir a izquierda si es menor
     * - Ir a derecha si es mayor
     * - Insertar si encuentra posición vacía
     */
    private void traducirAgregarNodo(String clave, String valor, String arbol) {
        emitir("    ; *** AGREGARNODO: clave=" + clave + ", valor=" + valor + " EN " + arbol);
        emitir("    mov rax, [" + clave + "]    ; Cargar clave");
        emitir("    mov rbx, [" + valor + "]    ; Cargar valor");
        emitir("    call agregar_nodo_" + arbol);
        emitir("    call dibujar_" + arbol + "  ; Redib después de inserción");
    }

    /**
     * TRADUCCIÓN DE INSERCIÓN EN LISTA
     * ============================================================
     * Operación: INSERTAR(clave, valor, lista)
     * 
     * Similar a agregarnodo pero para listas enlazadas
     * Inserta nuevos nodos manteniendo el orden de clave
     */
    private void traducirInsertar(String clave, String valor, String lista) {
        emitir("    ; *** INSERTAR: clave=" + clave + ", valor=" + valor + " EN " + lista);
        emitir("    mov rax, [" + clave + "]");
        emitir("    mov rbx, [" + valor + "]");
        emitir("    call insertar_" + lista);
    }

    /**
     * TRADUCCIÓN DE APILAR (PUSH en pila)
     * ============================================================
     * Operación: APILAR(valor, pila)
     * 
     * Proceso:
     * 1. Cargar valor en RAX
     * 2. Llamar a rutina apilar_PILA
     * 3. La rutina actualiza puntero de tope
     * 
     * Estructura pila (en memoria):
     *   [Elemento 0]
     *   [Elemento 1]  <- Tope (puntero aquí)
     *   [ vacío ]
     */
    private void traducirApilar(String valor, String pila) {
        emitir("    ; *** APILAR " + valor + " EN " + pila);
        emitir("    mov rax, [" + valor + "]");
        emitir("    call apilar_" + pila);
    }

    /**
     * TRADUCCIÓN DE DESAPILAR (POP en pila)
     * ============================================================
     * Operación: DESAPILAR(pila)
     * 
     * Retorna el elemento en el tope y decrementa puntero
     */
    private void traducirDesapilar(String pila) {
        emitir("    ; *** DESAPILAR EN " + pila);
        emitir("    call desapilar_" + pila);
    }

    /**
     * TRADUCCIÓN DE ENCOLAR (ENQUEUE en cola)
     * ============================================================
     * Operación: ENCOLAR(valor, cola)
     * 
     * Estructura FIFO (First In First Out):
     *   Cola: [Frente] -> [Elem1] -> [Elem2] <- [Final]
     * Inserta al final, extrae del frente
     */
    private void traducirEncolar(String valor, String cola) {
        emitir("    ; *** ENCOLAR " + valor + " EN " + cola);
        emitir("    mov rax, [" + valor + "]");
        emitir("    call encolar_" + cola);
    }

    /**
     * TRADUCCIÓN DE DESENCOLAR (DEQUEUE en cola)
     * ============================================================
     * Operación: DESENCOLAR(cola)
     * 
     * Extrae elemento del frente de la cola
     */
    private void traducirDesencolar(String cola) {
        emitir("    ; *** DESENCOLAR EN " + cola);
        emitir("    call desencolar_" + cola);
    }

    /**
     * TRADUCCIÓN DE VERIFICACIÓN DE VACÍO
     * ============================================================
     * Operación: resultado = VACIA(estructura)
     * 
     * Retorna:
     * - 1 si la estructura está vacía
     * - 0 si contiene elementos
     * 
     * Asigna resultado a variable de salida
     */
    private void traducirVerificaVacia(String estructura, String resultado) {
        emitir("    ; *** VACIA: " + estructura);
        emitir("    call vacia_" + estructura);
        emitir("    mov [" + resultado + "], rax    ; RAX = 1 si vacía, 0 si no");
    }

    /**
     * TRADUCCIÓN DE OBTENER TOPE/FRENTE
     * ============================================================
     * Operación: resultado = TOPE(pila) o resultado = FRENTE(cola)
     * 
     * Retorna el elemento sin extraerlo (PEEK)
     */
    private void traducirObtenerFrente(String operacion, String estructura, String resultado) {
        emitir("    ; *** " + operacion + ": " + estructura);
        emitir("    call " + operacion.toLowerCase() + "_" + estructura);
        emitir("    mov [" + resultado + "], rax");
    }

    /**
     * TRADUCCIÓN DE ERROR
     * ============================================================
     * Maneja errores en tiempo de ejecución
     * Imprime mensaje de error y continúa
     */
    private void traducirError(String msg1, String msg2, String res) {
        emitir("    ; *** ERROR: " + msg1 + " " + msg2);
        emitir("    mov rdi, msg_error");
        emitir("    call printf");
    }

    /**
     * TRADUCCIÓN DE SALTO CONDICIONAL (IF_FALSE)
     * ============================================================
     * Operación: IF_FALSE(condición) GOTO etiqueta
     * 
     * Proceso:
     * 1. Comparar variable con 0
     * 2. Si es falso (0), saltar a etiqueta
     * 3. Si es verdadero (!=0), continuar
     * 
     * Instrucciones:
     * - cmp: resta sin guardar (solo modifica flags)
     * - je: salto si igual (conditional jump if equal)
     * 
     * Código:
     *   cmp [condición], 0    ; Comparar con cero
     *   je etiqueta           ; Saltar si es cero (falso)
     */
    private void traducirIfFalse(String condicion, String etiqueta) {
        emitir("    ; *** IF_FALSE " + condicion + " GOTO " + etiqueta);
        emitir("    cmp [" + condicion + "], 0   ; Comparar condición con 0");
        emitir("    je " + etiqueta + "          ; Saltar si es cero (falso)");
    }

    /**
     * EMISOR DE CÓDIGO ENSAMBLADOR
     * ============================================================
     * Agrega una línea al buffer de código generado
     * 
     * Uso: Se llama desde cada método de traducción
     * Efecto: Acumula instrucciones en codigoEnsamblador
     * 
     * @param linea Instrucción ensamblador o comentario
     */
    private void emitir(String linea) {
        codigoEnsamblador.add(linea);
    }

    /**
     * GENERADOR DE RUTINAS DE VISUALIZACIÓN DE ÁRBOLES
     * ============================================================
     * Crea funciones ensamblador para dibujar árboles en ASCII
     * 
     * ESTRUCTURA:
     * - dibujar_arbol: Punto de entrada, inicializa variables
     * - dibujar_nodo_rec: Función recursiva que procesa nodos
     * 
     * PARÁMETROS (siguiendo convención AMD64):
     * - RAX: puntero al nodo actual
     * - RCX: profundidad en árbol (para indentación)
     * 
     * PROCESO RECURSIVO:
     * 1. Verificar si nodo es NULL (test rax, rax; jz)
     * 2. Imprimir espacios según profundidad
     * 3. Imprimir contenido del nodo
     * 4. Recursión izquierda (RCX++)
     * 5. Recursión derecha (RCX++)
     * 6. Retornar (RET)
     */
    private void generarRutinasArbol() {
        emitir("");
        emitir("; ============================================");
        emitir("; SECCIÓN: RUTINAS DE VISUALIZACIÓN DE ÁRBOLES");
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
        emitir("    mov rcx, 0              ; profundidad inicial = 0");
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
        emitir("    ; PARÁMETROS:");
        emitir("    ;   RAX = puntero al nodo actual");
        emitir("    ;   RCX = profundidad (indentación)");
        emitir("    ");
        emitir("    ; BASE: Si nodo es NULL, retornar");
        emitir("    test rax, rax");
        emitir("    jz .fin_nodo");
        emitir("    ");
        emitir("    ; Imprimir indentación (espacios según profundidad)");
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
        emitir("    ; Imprimir nodo actual [clave:valor]");
        emitir("    mov rdi, formato_nodo");
        emitir("    mov rsi, [rax + 0]      ; campo: clave");
        emitir("    mov rdx, [rax + 8]      ; campo: valor");
        emitir("    call printf");
        emitir("    ");
        emitir("    ; RECURSIÓN IZQUIERDA");
        emitir("    mov rbx, [rax + 16]     ; carga hijo izquierdo");
        emitir("    mov rax, rbx");
        emitir("    inc rcx                 ; aumentar profundidad");
        emitir("    call dibujar_nodo_rec");
        emitir("    dec rcx");
        emitir("    ");
        emitir("    ; RECURSIÓN DERECHA");
        emitir("    mov rax, [rbp + 16]     ; recuperar nodo original del stack");
        emitir("    mov rbx, [rax + 24]     ; carga hijo derecho");
        emitir("    mov rax, rbx");
        emitir("    inc rcx");
        emitir("    call dibujar_nodo_rec");
        emitir("    ");
        emitir(".fin_nodo:");
        emitir("    pop rbp");
        emitir("    ret");
    }

    /**
     * GENERADOR DE SECCIÓN DE DATOS
     * ============================================================
     * Define cadenas y constantes usadas en todo el programa
     * 
     * Formatos printf:
     * - %d: entero decimal
     * - %s: cadena (string)
     * - 10: código ASCII de salto de línea (newline)
     * 
     * Datos:
     * - formato_nodo: Patrón para imprimir nodos del árbol
     * - formato_error: Mensaje cuando estructura está vacía
     * - msg_error: Mensaje genérico de error
     */
    private void generarDatos() {
        emitir("");
        emitir("section .data");
        emitir("    formato_nodo db '[%d:%d] ', 0     ; Formato para nodo del árbol");
        emitir("    formato_error db 'ERROR: Estructura vacia', 10, 0");
        emitir("    msg_error db 'Error en la operacion', 10, 0");
    }

    /**
     * GENERADOR DE FINALIZACIÓN DEL PROGRAMA
     * ============================================================
     * Código final que termina la ejecución
     * 
     * Syscall 60: exit (Linux x86-64)
     * - RAX = 60 (número syscall para exit)
     * - RDI = 0 (código de salida: 0 = éxito)
     * 
     * En sistemas Windows usar:
     *   mov rax, 0xc000013a  ; STATUS_CONTROL_C_EXIT
     * En sistemas macOS usar diferentes syscalls
     */
    private void generarFinalizacion() {
        emitir("");
        emitir("; ============================================");
        emitir("; SECCIÓN: FIN DEL PROGRAMA");
        emitir("; ============================================");
        emitir("");
        emitir("    mov rax, 60             ; Número syscall para exit");
        emitir("    mov rdi, 0              ; Código de salida (0 = éxito)");
        emitir("    syscall                 ; Realizar syscall");
    }

    /**
     * VISUALIZACIÓN DE ÁRBOLES EN FORMATO ASCII
     * ============================================================
     * Dibuja el árbol en representación textual para debugging
     * 
     * Ejemplo de salida:
     *   === ÁRBOL BINARIO: miArbol ===
     *   └── [10:dato1]
     *       ├── [5:dato2]
     *       └── [15:dato3]
     * 
     * @param nombreArbol Nombre de la variable que contiene el árbol
     */
    private void dibujarArbol(String nombreArbol) {
        NodoArbol raiz = arboles.get(nombreArbol);
        
        outputVisualizacion.append("\n╔═══ ÁRBOL BINARIO: ").append(nombreArbol).append(" ═══╗\n");
        
        if (raiz == null) {
            outputVisualizacion.append("║ [ Vacío ]\n");
        } else {
            dibujarNodoRecursivo(raiz, "", true, nombreArbol);
        }
        
        outputVisualizacion.append("╚════════════════════════════════════════╝\n");
    }

    /**
     * DIBUJADOR RECURSIVO DE NODOS
     * ============================================================
     * Dibuja cada nodo con su estructura visual
     * 
     * Símbolos:
     * - └── : Último nodo (sin más hermanos)
     * - ├── : Nodo intermedio (hay más hermanos después)
     * - │   : Conexión vertical
     * - 4 espacios: Sin más nodos
     * 
     * @param nodo Nodo actual a dibujar
     * @param prefijo Espacios/símbolos de indentación
     * @param esUltimo true si es el último hermano
     * @param nombreArbol Para debugging
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
     * OBTENCIÓN DEL CÓDIGO ENSAMBLADOR GENERADO
     * ============================================================
     * Convierte el buffer de instrucciones en un string único
     * 
     * @return Código ensamblador completo, listo para escribir a archivo
     */
    public String obtenerCodigoEnsamblador() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigoEnsamblador) {
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }

    /**
     * OBTENCIÓN DE LA VISUALIZACIÓN
     * ============================================================
     * @return Buffer con la visualización ASCII de todas las estructuras
     */
    public String obtenerVisualizacion() {
        return outputVisualizacion.toString();
    }

    /**
     * IMPRESIÓN COMPLETA DEL COMPILADO
     * ============================================================
     * Salida a consola con dos secciones:
     * 1. Código ensamblador generado
     * 2. Visualización de estructuras procesadas
     */
    public void imprimirCodigoCompleto() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║    CÓDIGO ENSAMBLADOR GENERADO - COMPILADOR DSL   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println(obtenerCodigoEnsamblador());
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║     VISUALIZACIÓN DE ESTRUCTURAS PROCESADAS       ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println(obtenerVisualizacion());
    }

    /**
     * PROCESADOR PRINCIPAL DE CUÁDRUPLOS
     * ============================================================
     * FLUJO COMPLETO:
     * 1. Recibe lista de cuádruplos del análisis semántico
     * 2. Traduce cada cuádruplo a ensamblador
     * 3. Genera rutinas auxiliares (visualización)
     * 4. Genera sección de datos
     * 5. Genera finalización
     * 6. OPTIMIZACIÓN: Aplica peephole optimization
     * 
     * @param cuadruplos Lista de cuádruplos para compilar
     */
    public void procesarCuadruplos(List<Cuadruplo> cuadruplos) {
        // TRADUCCIÓN
        for (Cuadruplo c : cuadruplos) {
            traducirCuadruplo(c);
        }
        
        // GENERACIÓN DE SECCIONES FINALES
        generarRutinasArbol();
        generarDatos();
        generarFinalizacion();
        
        // OPTIMIZACIÓN
        if (optimizacionActiva) {
            System.out.println("[INFO] Aplicando optimizaciones de código...");
            optimizarCodigo();
        }
    }

    /**
     * ACTIVAR/DESACTIVAR OPTIMIZACIÓN
     * ============================================================
     * Permite control sobre si aplicar o no optimizaciones
     */
    public void establecerOptimizacion(boolean activa) {
        this.optimizacionActiva = activa;
    }
}
