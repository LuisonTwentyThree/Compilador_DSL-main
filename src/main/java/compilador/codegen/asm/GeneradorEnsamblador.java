package compilador.codegen.asm;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compilador.core.Cuadruplo;
/**
 * Generador de codigo ensamblador 8086/MASM a partir de cuadruplos.
 *
 * El objetivo de esta clase es producir un .asm consistente y ensamblable para
 * las operaciones basicas del DSL. Las estructuras de datos se representan de
 * forma simplificada como palabras en memoria para evitar llamadas a rutinas que
 * no existen todavia.
 */
public class GeneradorEnsamblador {
    private List<String> codigoEnsamblador;
    private final Map<String, NodoArbol> arboles;
    private final Map<String, String> cacheRegistros;
    private final Set<String> variablesUsadas;
    private final Set<String> variablesDeclaradas;
    private final Map<String, String> textosDeclarados;
    private final Map<String, String> estructurasTipo;
    private final Map<String, Integer> estructurasTamano;
    private final List<String> rutinasArbolRecursivas;
    private final StringBuilder outputVisualizacion;
    private boolean optimizacionActiva;
    private boolean heapNecesario;
    private boolean colaRecorridoArbolNecesaria;
    private int contadorEtiquetasInternas;
    private int contadorTextos;

    public GeneradorEnsamblador() {
        this.codigoEnsamblador = new ArrayList<>();
        this.arboles = new LinkedHashMap<>();
        this.cacheRegistros = new LinkedHashMap<>();
        this.variablesUsadas = new LinkedHashSet<>();
        this.variablesDeclaradas = new LinkedHashSet<>();
        this.textosDeclarados = new LinkedHashMap<>();
        this.estructurasTipo = new LinkedHashMap<>();
        this.estructurasTamano = new LinkedHashMap<>();
        this.rutinasArbolRecursivas = new ArrayList<>();
        this.outputVisualizacion = new StringBuilder();
        this.optimizacionActiva = true;
        this.heapNecesario = false;
        this.colaRecorridoArbolNecesaria = false;
        this.contadorEtiquetasInternas = 1;
        this.contadorTextos = 1;
        agregarEncabezado();
    }
    public static class NodoArbol {
        public Object clave;
        public Object valor;
        public NodoArbol izquierda;
        public NodoArbol derecha;

        public NodoArbol(Object clave, Object valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }
    private void agregarEncabezado() {
        emitir("; ============================================");
        emitir("; CODIGO ENSAMBLADOR GENERADO - DSL");
        emitir("; Arquitectura objetivo: Intel 8086 / DOS");
        emitir("; ============================================");
        emitir("");
        emitir(".model small");
        emitir(".stack 100h");
        emitir("");
        emitir(".data");
        emitir("    titulo db 'EJECUCION DE PROGRAMA DSL', 0Dh, 0Ah, '$'");
        emitir("    newline db 0Dh, 0Ah, '$'");
        emitir("    msg_error db 'Error en la operacion', 0Dh, 0Ah, '$'");
        emitir("");
        emitir(".code");
        emitir("main proc");
        emitir("    mov ax, @data");
        emitir("    mov ds, ax");
        emitir("    mov dx, offset titulo");
        emitir("    call print_string");
        emitir("");
    }

    public void traducirCuadruplo(Cuadruplo c) {
        if (c == null || c.operador == null) {
            return;
        }

        String op = c.operador.toUpperCase();
        String arg1 = normalizar(c.argumento1);
        String arg2 = normalizar(c.argumento2);
        String res = normalizar(c.resultado);

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
            case "<":
            case ">":
            case "==":
            case "!=":
            case "<=":
            case ">=":
                traducirComparacion(op, arg1, arg2, res);
                break;
            case "PRINT":
            case "MOSTRAR":
                traducirPrint(arg1);
                break;
            case "ALLOC":
                traducirAlloc(arg1, arg2, res);
                break;
            case "FREE":
                traducirFree(res);
                break;
            case "AGREGARNODO":
            case "INSERTAR":
            case "INSERTAR_FINAL":
            case "INSERTAR_INICIO":
            case "INSERTAR_EN_POSICION":
            case "INSERTAR_FRENTE":
            case "APILAR":
            case "PUSH":
            case "ENCOLAR":
            case "ENQUEUE":
            case "ACTUALIZAR":
            case "AGREGARARISTA":
                traducirOperacionInsercion(op, arg1, arg2, res);
                break;
            case "DESAPILAR":
            case "POP":
            case "DESENCOLAR":
            case "DEQUEUE":
            case "ELIMINAR":
            case "ELIMINAR_INICIO":
            case "ELIMINAR_FINAL":
            case "ELIMINAR_FRENTE":
            case "ELIMINAR_POSICION":
            case "ELIMINARNODO":
            case "REHASH":
                traducirOperacionSimple(op, res);
                break;
            case "VACIA":
            case "LLENA":
                traducirBooleanoEstructura(op, arg1, res);
                break;
            case "TOPE":
            case "FRENTE":
            case "FRONT":
            case "PEEK":
            case "TAMANO":
            case "ALTURA":
            case "HOJAS":
            case "NODOS":
            case "BUSCAR":
            case "VECINOS":
                if (!arg2.isEmpty()) {
                    traducirConsultarGrafo(op, arg1, arg2, res);
                    break;
                }
                traducirPropiedadEstructura(op, arg1, res);
                break;
            case "PREORDEN":
            case "INORDEN":
            case "POSTORDEN":
            case "RECORRIDOPORNIVELES":
            case "RECORRER":
            case "RECORRERADELANTE":
            case "RECORRERATRAS":
            case "BFS":
            case "DFS":
            case "CAMINOCORTO":
                traducirPropiedadEstructura(op, arg1, res);
                break;
            case "ETIQUETA":
                emitir(etiquetaValida(res) + ":");
                break;
            case "IF_FALSE":
                traducirIfFalse(arg1, res);
                break;
            case "IF_TRUE":
                traducirIfTrue(arg1, res);
                break;
            case "GOTO":
                emitir("    jmp " + etiquetaValida(res));
                break;
            case "ERROR":
                traducirError(arg1, arg2);
                break;
            case "DIBUJAR_ARBOL":
                dibujarArbol(res);
                break;
            default:
                emitir("    ; Operacion no implementada en ASM: " + op + " " + arg1 + " " + arg2 + " " + res);
                break;
        }
    }

    private void traducirAsignacion(String valor, String variable) {
        if (!esIdentificador(variable)) {
            return;
        }

        if (esCadena(valor)) {
            textosDeclarados.put(variable, limpiarCadena(valor));
            emitir("    ; " + variable + " = texto");
            return;
        }

        registrarVariable(variable);
        emitir("    ; " + variable + " = " + valor);
        cargarAX(valor);
        emitir("    mov [" + variable + "], ax");
        actualizarCache("ax", variable);
    }

    private void traducirOperacionMatematica(String op, String arg1, String arg2, String res) {
        registrarVariable(res);
        emitir("    ; " + res + " = " + arg1 + " " + op + " " + arg2);
        cargarAX(arg1);
        cargarBX(arg2);
        switch (op) {
            case "+":
                emitir("    add ax, bx");
                break;
            case "-":
                emitir("    sub ax, bx");
                break;
            case "*":
                emitir("    imul bx");
                break;
            case "/":
                emitir("    cwd");
                emitir("    idiv bx");
                break;
            default:
                break;
        }
        emitir("    mov [" + res + "], ax");
        actualizarCache("ax", res);
    }

    private void traducirComparacion(String op, String arg1, String arg2, String res) {
        registrarVariable(res);
        String lTrue = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; " + res + " = " + arg1 + " " + op + " " + arg2);
        cargarAX(arg1);
        cargarBX(arg2);
        emitir("    cmp ax, bx");
        emitir("    " + saltoComparacion(op) + " " + lTrue);
        emitir("    mov [" + res + "], 0");
        emitir("    jmp " + lFin);
        emitir(lTrue + ":");
        emitir("    mov [" + res + "], 1");
        emitir(lFin + ":");
    }

    private void traducirPrint(String valor) {
        emitir("    ; PRINT " + valor);
        if (valor == null || valor.isEmpty()) {
            emitir("    ; PRINT omitido: expresion vacia");
            return;
        }
        if (esCadena(valor)) {
            String etiqueta = declararTextoAnonimo(limpiarCadena(valor));
            emitir("    mov dx, offset " + etiqueta);
            emitir("    call print_string");
        } else if (textosDeclarados.containsKey(valor)) {
            emitir("    mov dx, offset " + valor);
            emitir("    call print_string");
        } else {
            cargarAX(valor);
            emitir("    call print_num");
        }
        emitir("    mov dx, offset newline");
        emitir("    call print_string");
    }

    private void traducirAlloc(String tamano, String tipo, String variable) {
        if (!esIdentificador(variable)) {
            return;
        }

        int capacidad = esNumero(tamano) ? Integer.parseInt(tamano) : 100;
        String tipoNormalizado = tipo == null ? "" : tipo.toUpperCase();

        if ("PILA".equals(tipoNormalizado) || "PILA_CIRCULAR".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "PILA");
            estructurasTamano.put(variable, capacidad);
            emitir("    ; CREAR PILA " + variable + " TAMANO " + capacidad);
            return;
        }

        if ("COLA".equals(tipoNormalizado) || "BICOLA".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "COLA");
            estructurasTamano.put(variable, capacidad);
            emitir("    ; CREAR COLA " + variable + " TAMANO " + capacidad);
            return;
        }

        if ("LISTA_ENLAZADA".equals(tipoNormalizado) || "LISTA_CIRCULAR".equals(tipoNormalizado)
                || "LISTA_DOBLE_ENLAZADA".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "LISTA");
            estructurasTamano.put(variable, capacidad);
            heapNecesario = true;
            emitir("    ; CREAR LISTA " + variable);
            return;
        }

        if ("ARBOL_BINARIO".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "ARBOL");
            estructurasTamano.put(variable, capacidad);
            heapNecesario = true;
            emitir("    ; CREAR ARBOL_BINARIO " + variable);
            return;
        }

        if ("GRAFO".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "GRAFO");
            estructurasTamano.put(variable, capacidad);
            emitir("    ; CREAR GRAFO " + variable + " CAPACIDAD " + capacidad);
            return;
        }

        if ("TABLA_HASH".equals(tipoNormalizado)) {
            estructurasTipo.put(variable, "HASH");
            estructurasTamano.put(variable, capacidad);
            emitir("    ; CREAR TABLA_HASH " + variable + " CAPACIDAD " + capacidad);
            return;
        }

        registrarVariable(variable);
        emitir("    ; ALLOC " + variable + " TAMANO " + tamano);
        emitir("    mov word ptr [" + variable + "], 0");
    }

    private void traducirFree(String variable) {
        registrarVariable(variable);
        emitir("    ; FREE " + variable);
        emitir("    mov word ptr [" + variable + "], 0");
    }

    private void traducirOperacionInsercion(String op, String arg1, String arg2, String estructura) {
        String tipo = estructurasTipo.get(estructura);
        String valor = arg2.isEmpty() ? arg1 : arg2;

        if ("PILA".equals(tipo) && ("APILAR".equals(op) || "PUSH".equals(op))) {
            emitir("    ; " + op + " " + valor + " EN " + estructura);
            cargarAX(valor);
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    shl bx, 1");
            emitir("    mov " + estructura + "[bx], ax");
            emitir("    inc word ptr [" + estructura + "_top]");
            return;
        }

        if ("COLA".equals(tipo) && ("ENCOLAR".equals(op) || "ENQUEUE".equals(op))) {
            int capacidad = estructurasTamano.getOrDefault(estructura, 100);
            String lNoWrap = nuevaEtiquetaInterna();
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " " + valor + " EN " + estructura);

            // Si la cola esta llena, no inserta.
            emitir("    cmp word ptr [" + estructura + "_count], " + capacidad);
            emitir("    jge " + lFin);

            cargarAX(valor);
            emitir("    mov bx, [" + estructura + "_rear]");
            emitir("    shl bx, 1");
            emitir("    mov " + estructura + "[bx], ax");

            emitir("    inc word ptr [" + estructura + "_rear]");
            emitir("    cmp word ptr [" + estructura + "_rear], " + capacidad);
            emitir("    jl " + lNoWrap);
            emitir("    mov word ptr [" + estructura + "_rear], 0");
            emitir(lNoWrap + ":");

            emitir("    inc word ptr [" + estructura + "_count]");
            emitir(lFin + ":");
            return;
        }

        if ("LISTA".equals(tipo) && (op.startsWith("INSERTAR") || "AGREGARNODO".equals(op))) {
            traducirInsertarLista(op, valor, estructura);
            return;
        }

        if ("ARBOL".equals(tipo) && "AGREGARNODO".equals(op)) {
            traducirAgregarNodoArbol(arg2.isEmpty() ? arg1 : arg2, estructura);
            return;
        }

        if ("GRAFO".equals(tipo) && "AGREGARNODO".equals(op)) {
            traducirAgregarNodoGrafo(arg1, estructura);
            return;
        }

        if ("GRAFO".equals(tipo) && "AGREGARARISTA".equals(op)) {
            traducirAgregarAristaGrafo(arg1, arg2, estructura);
            return;
        }

        if ("HASH".equals(tipo) && ("INSERTAR".equals(op) || "ACTUALIZAR".equals(op))) {
            traducirInsertarHash(arg1, arg2, estructura);
            return;
        }

        registrarVariable(estructura);
        emitir("    ; " + op + " " + arg1 + (arg2.isEmpty() ? "" : " " + arg2) + " EN " + estructura);
        cargarAX(valor);
        emitir("    mov [" + estructura + "], ax");
    }

    private void traducirOperacionSimple(String op, String estructura) {
        String tipo = estructurasTipo.get(estructura);

        if ("PILA".equals(tipo) && ("DESAPILAR".equals(op) || "POP".equals(op))) {
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_top], 0");
            emitir("    je " + lFin);
            emitir("    dec word ptr [" + estructura + "_top]");
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    shl bx, 1");
            emitir("    mov word ptr " + estructura + "[bx], 0");
            emitir(lFin + ":");
            return;
        }

        if ("COLA".equals(tipo) && ("DESENCOLAR".equals(op) || "DEQUEUE".equals(op)
                || "ELIMINAR_FRENTE".equals(op))) {
            int capacidad = estructurasTamano.getOrDefault(estructura, 100);
            String lNoWrap = nuevaEtiquetaInterna();
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " EN " + estructura);

            // Si la cola esta vacia, no elimina.
            emitir("    cmp word ptr [" + estructura + "_count], 0");
            emitir("    je " + lFin);

            emitir("    mov bx, [" + estructura + "_front]");
            emitir("    shl bx, 1");
            emitir("    mov word ptr " + estructura + "[bx], 0");

            emitir("    inc word ptr [" + estructura + "_front]");
            emitir("    cmp word ptr [" + estructura + "_front], " + capacidad);
            emitir("    jl " + lNoWrap);
            emitir("    mov word ptr [" + estructura + "_front], 0");
            emitir(lNoWrap + ":");

            emitir("    dec word ptr [" + estructura + "_count]");
            emitir(lFin + ":");
            return;
        }

        if ("LISTA".equals(tipo) && ("ELIMINAR".equals(op) || "ELIMINAR_INICIO".equals(op)
                || "ELIMINAR_FINAL".equals(op))) {
            traducirEliminarLista(op, estructura);
            return;
        }

        registrarVariable(estructura);
        emitir("    ; " + op + " EN " + estructura);
        emitir("    mov word ptr [" + estructura + "], 0");
    }

    private void traducirBooleanoEstructura(String op, String estructura, String resultado) {
        String tipo = estructurasTipo.get(estructura);
        registrarVariable(resultado);

        if ("PILA".equals(tipo)) {
            traducirBooleanoPorContador(op, estructura + "_top", resultado, estructurasTamano.getOrDefault(estructura, 100));
            return;
        }

        if ("COLA".equals(tipo)) {
            traducirBooleanoPorContador(op, estructura + "_count", resultado, estructurasTamano.getOrDefault(estructura, 100));
            return;
        }

        registrarVariable(estructura);
        traducirBooleanoPorContador(op, estructura, resultado, 1);
    }

    private void traducirInsertarLista(String op, String valor, String lista) {
        String lAppend = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; " + op + " " + valor + " EN " + lista + " (nodo HEAP: valor, sig)");
        cargarAX(valor);
        emitir("    mov si, [HEAP_PTR]");
        emitir("    add word ptr [HEAP_PTR], 4");
        emitir("    mov HEAP[si], ax");
        emitir("    mov word ptr HEAP[si+2], 0");
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    jne " + lAppend);
        emitir("    mov [" + lista + "_head], si");
        emitir("    mov [" + lista + "_tail], si");
        emitir("    jmp " + lFin);
        emitir(lAppend + ":");
        emitir("    mov bx, [" + lista + "_tail]");
        emitir("    mov HEAP[bx+2], si");
        emitir("    mov [" + lista + "_tail], si");
        emitir(lFin + ":");
    }

    private void traducirEliminarLista(String op, String lista) {
        if ("ELIMINAR_FINAL".equals(op)) {
            String lUnico = nuevaEtiquetaInterna();
            String lLoop = nuevaEtiquetaInterna();
            String lEncontrado = nuevaEtiquetaInterna();
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " EN " + lista + " usando punteros HEAP");
            emitir("    cmp word ptr [" + lista + "_head], 0");
            emitir("    je " + lFin);
            emitir("    mov bx, [" + lista + "_head]");
            emitir("    cmp bx, [" + lista + "_tail]");
            emitir("    je " + lUnico);
            emitir(lLoop + ":");
            emitir("    mov si, HEAP[bx+2]");
            emitir("    cmp si, [" + lista + "_tail]");
            emitir("    je " + lEncontrado);
            emitir("    cmp si, 0");
            emitir("    je " + lFin);
            emitir("    mov bx, si");
            emitir("    jmp " + lLoop);
            emitir(lEncontrado + ":");
            emitir("    mov word ptr HEAP[bx+2], 0");
            emitir("    mov [" + lista + "_tail], bx");
            emitir("    jmp " + lFin);
            emitir(lUnico + ":");
            emitir("    mov word ptr [" + lista + "_head], 0");
            emitir("    mov word ptr [" + lista + "_tail], 0");
            emitir(lFin + ":");
            return;
        }

        String lFin = nuevaEtiquetaInterna();

        emitir("    ; " + op + " EN " + lista + " usando punteros HEAP");
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    je " + lFin);
        emitir("    mov bx, [" + lista + "_head]");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov [" + lista + "_head], ax");
        emitir("    cmp ax, 0");
        emitir("    jne " + lFin);
        emitir("    mov word ptr [" + lista + "_tail], 0");
        emitir(lFin + ":");
    }

    private void traducirAgregarNodoArbol(String valor, String arbol) {
        String lRootExiste = nuevaEtiquetaInterna();
        String lLoop = nuevaEtiquetaInterna();
        String lDerecha = nuevaEtiquetaInterna();
        String lIrIzq = nuevaEtiquetaInterna();
        String lIrDer = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; AGREGARNODO " + valor + " EN " + arbol + " (nodo HEAP: valor, izq, der)");
        cargarAX(valor);
        emitir("    mov si, [HEAP_PTR]");
        emitir("    add word ptr [HEAP_PTR], 6");
        emitir("    mov HEAP[si], ax");
        emitir("    mov word ptr HEAP[si+2], 0");
        emitir("    mov word ptr HEAP[si+4], 0");
        emitir("    cmp word ptr [" + arbol + "_root], 0");
        emitir("    jne " + lRootExiste);
        emitir("    mov [" + arbol + "_root], si");
        emitir("    jmp " + lFin);
        emitir(lRootExiste + ":");
        emitir("    mov bx, [" + arbol + "_root]");
        emitir(lLoop + ":");
        emitir("    cmp ax, HEAP[bx]");
        emitir("    jg " + lDerecha);
        emitir("    cmp word ptr HEAP[bx+2], 0");
        emitir("    je " + lIrIzq);
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + lLoop);
        emitir(lIrIzq + ":");
        emitir("    mov HEAP[bx+2], si");
        emitir("    jmp " + lFin);
        emitir(lDerecha + ":");
        emitir("    cmp word ptr HEAP[bx+4], 0");
        emitir("    je " + lIrDer);
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    jmp " + lLoop);
        emitir(lIrDer + ":");
        emitir("    mov HEAP[bx+4], si");
        emitir(lFin + ":");
    }

    private void traducirAgregarNodoGrafo(String nodo, String grafo) {
        int capacidad = estructurasTamano.getOrDefault(grafo, 100);
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; AGREGARNODO " + nodo + " EN " + grafo);
        emitir("    cmp word ptr [" + grafo + "_node_count], " + capacidad);
        emitir("    jge " + lFin);
        cargarAX(nodo);
        emitir("    mov bx, [" + grafo + "_node_count]");
        emitir("    shl bx, 1");
        emitir("    mov " + grafo + "_nodes[bx], ax");
        emitir("    inc word ptr [" + grafo + "_node_count]");
        emitir(lFin + ":");
    }

    private void traducirAgregarAristaGrafo(String origen, String destino, String grafo) {
        int capacidad = estructurasTamano.getOrDefault(grafo, 100);
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; AGREGARARISTA " + origen + " " + destino + " EN " + grafo);
        emitir("    cmp word ptr [" + grafo + "_edge_count], " + capacidad);
        emitir("    jge " + lFin);
        emitir("    mov bx, [" + grafo + "_edge_count]");
        emitir("    shl bx, 1");
        cargarAX(origen);
        emitir("    mov " + grafo + "_edges_from[bx], ax");
        cargarAX(destino);
        emitir("    mov " + grafo + "_edges_to[bx], ax");
        emitir("    inc word ptr [" + grafo + "_edge_count]");
        emitir(lFin + ":");
    }

    private void traducirInsertarHash(String clave, String valor, String hash) {
        int capacidad = estructurasTamano.getOrDefault(hash, 100);
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; INSERTAR " + clave + " " + valor + " EN " + hash);
        emitir("    cmp word ptr [" + hash + "_count], " + capacidad);
        emitir("    jge " + lFin);
        emitir("    mov bx, [" + hash + "_count]");
        emitir("    shl bx, 1");
        cargarAX(clave);
        emitir("    mov " + hash + "_keys[bx], ax");
        cargarAX(valor);
        emitir("    mov " + hash + "_values[bx], ax");
        emitir("    inc word ptr [" + hash + "_count]");
        emitir(lFin + ":");
    }

    private void traducirPropiedadEstructura(String op, String estructura, String resultado) {
        String tipo = estructurasTipo.get(estructura);
        registrarVariable(resultado);

        if ("PILA".equals(tipo) && ("TOPE".equals(op) || "PEEK".equals(op))) {
            String lVacia = nuevaEtiquetaInterna();
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_top], 0");
            emitir("    je " + lVacia);
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    dec bx");
            emitir("    shl bx, 1");
            emitir("    mov ax, " + estructura + "[bx]");
            emitir("    mov [" + resultado + "], ax");
            emitir("    jmp " + lFin);
            emitir(lVacia + ":");
            emitir("    mov word ptr [" + resultado + "], 0");
            emitir(lFin + ":");
            return;
        }

        if ("COLA".equals(tipo) && ("FRENTE".equals(op) || "FRONT".equals(op))) {
            String lVacia = nuevaEtiquetaInterna();
            String lFin = nuevaEtiquetaInterna();

            emitir("    ; " + op + " EN " + estructura);

            emitir("    cmp word ptr [" + estructura + "_count], 0");
            emitir("    je " + lVacia);

            emitir("    mov bx, [" + estructura + "_front]");
            emitir("    shl bx, 1");
            emitir("    mov ax, " + estructura + "[bx]");
            emitir("    mov [" + resultado + "], ax");
            emitir("    jmp " + lFin);

            emitir(lVacia + ":");
            emitir("    mov word ptr [" + resultado + "], 0");

            emitir(lFin + ":");
            return;
        }

        if ("LISTA".equals(tipo) && ("RECORRER".equals(op) || "RECORRERADELANTE".equals(op)
                || "TAMANO".equals(op) || "BUSCAR".equals(op))) {
            traducirRecorrerLista(op, estructura, resultado);
            return;
        }

        if ("ARBOL".equals(tipo) && ("PREORDEN".equals(op) || "INORDEN".equals(op)
                || "POSTORDEN".equals(op) || "RECORRIDOPORNIVELES".equals(op)
                || "ALTURA".equals(op) || "HOJAS".equals(op) || "NODOS".equals(op))) {
            traducirRecorrerArbol(op, estructura, resultado);
            return;
        }

        if ("GRAFO".equals(tipo) && ("VECINOS".equals(op) || "BFS".equals(op) || "DFS".equals(op))) {
            traducirConsultarGrafo(op, estructura, resultado);
            return;
        }

        if ("HASH".equals(tipo) && ("BUSCAR".equals(op) || "TAMANO".equals(op))) {
            traducirConsultarHash(op, estructura, resultado);
            return;
        }

        registrarVariable(estructura);
        emitir("    ; " + op + " EN " + estructura);
        if ("VECINOS".equals(op) || "BFS".equals(op) || "DFS".equals(op) || "CAMINOCORTO".equals(op)) {
            registrarVariable(resultado);
            emitir("    mov ax, 0");
            emitir("    mov [" + resultado + "], ax");
            return;
        }
        emitir("    mov ax, [" + estructura + "]");
        emitir("    mov [" + resultado + "], ax");
    }

    private void traducirRecorrerLista(String op, String lista, String resultado) {
        String lLoop = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        registrarVariable(resultado);
        emitir("    ; " + op + " EN " + lista + " recorriendo punteros HEAP");
        emitir("    mov bx, [" + lista + "_head]");
        emitir("    xor cx, cx");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir(lLoop + ":");
        emitir("    cmp bx, 0");
        emitir("    je " + lFin);
        emitir("    inc cx");
        emitir("    mov ax, HEAP[bx]");
        emitir("    mov [" + resultado + "], ax");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + lLoop);
        emitir(lFin + ":");
        if ("TAMANO".equals(op)) {
            emitir("    mov [" + resultado + "], cx");
        }
    }

    private void traducirRecorrerArbol(String op, String arbol, String resultado) {
        registrarVariable(resultado);

        if ("PREORDEN".equals(op) || "INORDEN".equals(op) || "POSTORDEN".equals(op)) {
            traducirRecorridoArbolImpreso(op, arbol, resultado);
            return;
        }

        if ("RECORRIDOPORNIVELES".equals(op)) {
            traducirRecorridoPorNivelesArbol(arbol, resultado);
            return;
        }

        String lLoop = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; " + op + " EN " + arbol + " recorriendo enlaces HEAP");
        emitir("    mov bx, [" + arbol + "_root]");
        emitir("    xor cx, cx");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir(lLoop + ":");
        emitir("    cmp bx, 0");
        emitir("    je " + lFin);
        emitir("    inc cx");
        emitir("    mov ax, HEAP[bx]");
        emitir("    mov [" + resultado + "], ax");
        if ("ALTURA".equals(op) || "HOJAS".equals(op)) {
            emitir("    mov bx, HEAP[bx+2]");
        } else {
            emitir("    mov bx, HEAP[bx+4]");
        }
        emitir("    jmp " + lLoop);
        emitir(lFin + ":");
        if ("NODOS".equals(op)) {
            emitir("    mov [" + resultado + "], cx");
        }
    }

    private void traducirRecorridoArbolImpreso(String op, String arbol, String resultado) {
        String lRutina = nuevaEtiquetaInterna();
        String lRet = nuevaEtiquetaInterna();
        String lSiguiente = nuevaEtiquetaInterna();

        emitir("    ; " + op + " EN " + arbol + " usando recursion con pila 8086");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir("    mov bx, [" + arbol + "_root]");
        emitir("    call " + lRutina);
        emitir("    mov ax, [" + resultado + "]");
        emitir("    call print_num");
        emitir("    mov dx, offset newline");
        emitir("    call print_string");
        emitir("    jmp " + lSiguiente);
        emitir(lSiguiente + ":");

        List<String> rutina = new ArrayList<>();
        rutina.add(lRutina + ":");
        rutina.add("    cmp bx, 0");
        rutina.add("    je " + lRet);

        if ("PREORDEN".equals(op)) {
            emitirImprimirNodoArbol(rutina, resultado);
        }

        rutina.add("    push bx");
        rutina.add("    mov bx, HEAP[bx+2]");
        rutina.add("    call " + lRutina);
        rutina.add("    pop bx");

        if ("INORDEN".equals(op)) {
            emitirImprimirNodoArbol(rutina, resultado);
        }

        rutina.add("    push bx");
        rutina.add("    mov bx, HEAP[bx+4]");
        rutina.add("    call " + lRutina);
        rutina.add("    pop bx");

        if ("POSTORDEN".equals(op)) {
            emitirImprimirNodoArbol(rutina, resultado);
        }

        rutina.add(lRet + ":");
        rutina.add("    ret");
        rutinasArbolRecursivas.addAll(rutina);
    }

    private void emitirImprimirNodoArbol(String resultado) {
        emitirImprimirNodoArbol(codigoEnsamblador, resultado);
    }

    private void emitirImprimirNodoArbol(List<String> destino, String resultado) {
        destino.add("    mov ax, HEAP[bx]");
        destino.add("    mov [" + resultado + "], ax");
        destino.add("    push bx");
        destino.add("    call print_num");
        destino.add("    mov dx, offset newline");
        destino.add("    call print_string");
        destino.add("    pop bx");
    }

    private void traducirRecorridoPorNivelesArbol(String arbol, String resultado) {
        String lFin = nuevaEtiquetaInterna();
        String lLoop = nuevaEtiquetaInterna();
        String lNodoVacio = nuevaEtiquetaInterna();
        String lSinIzq = nuevaEtiquetaInterna();
        String lSinDer = nuevaEtiquetaInterna();

        colaRecorridoArbolNecesaria = true;
        registrarVariable(resultado);
        emitir("    ; RECORRIDOPORNIVELES EN " + arbol + " usando cola estatica segura");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir("    mov word ptr [ARBOL_Q_FRONT], 0");
        emitir("    mov word ptr [ARBOL_Q_REAR], 0");
        emitir("    mov ax, [" + arbol + "_root]");
        emitir("    cmp ax, 0");
        emitir("    je " + lFin);
        emitir("    mov bx, [ARBOL_Q_REAR]");
        emitir("    shl bx, 1");
        emitir("    mov ARBOL_QUEUE[bx], ax");
        emitir("    inc word ptr [ARBOL_Q_REAR]");
        emitir(lLoop + ":");
        emitir("    mov ax, [ARBOL_Q_FRONT]");
        emitir("    cmp ax, [ARBOL_Q_REAR]");
        emitir("    jge " + lFin);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov bx, ARBOL_QUEUE[bx]");
        emitir("    inc word ptr [ARBOL_Q_FRONT]");
        emitir("    cmp bx, 0");
        emitir("    je " + lNodoVacio);
        emitirImprimirNodoArbol(resultado);
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    cmp ax, 0");
        emitir("    je " + lSinIzq);
        emitir("    mov si, [ARBOL_Q_REAR]");
        emitir("    cmp si, 128");
        emitir("    jge " + lSinIzq);
        emitir("    shl si, 1");
        emitir("    mov ARBOL_QUEUE[si], ax");
        emitir("    inc word ptr [ARBOL_Q_REAR]");
        emitir(lSinIzq + ":");
        emitir("    mov ax, HEAP[bx+4]");
        emitir("    cmp ax, 0");
        emitir("    je " + lSinDer);
        emitir("    mov si, [ARBOL_Q_REAR]");
        emitir("    cmp si, 128");
        emitir("    jge " + lSinDer);
        emitir("    shl si, 1");
        emitir("    mov ARBOL_QUEUE[si], ax");
        emitir("    inc word ptr [ARBOL_Q_REAR]");
        emitir(lSinDer + ":");
        emitir(lNodoVacio + ":");
        emitir("    jmp " + lLoop);
        emitir(lFin + ":");
    }

    private void traducirConsultarGrafo(String op, String grafo, String resultado) {
        String lLoop = nuevaEtiquetaInterna();
        String lMatch = nuevaEtiquetaInterna();
        String lNext = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        registrarVariable(resultado);
        emitir("    ; " + op + " EN " + grafo + " recorriendo arreglos de aristas");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir("    xor si, si");
        emitir(lLoop + ":");
        emitir("    cmp si, [" + grafo + "_edge_count]");
        emitir("    jge " + lFin);
        emitir("    mov bx, si");
        emitir("    shl bx, 1");
        emitir("    cmp word ptr " + grafo + "_edges_from[bx], 0");
        emitir("    jne " + lMatch);
        emitir("    jmp " + lNext);
        emitir(lMatch + ":");
        emitir("    mov ax, " + grafo + "_edges_to[bx]");
        emitir("    mov [" + resultado + "], ax");
        emitir(lNext + ":");
        emitir("    inc si");
        emitir("    jmp " + lLoop);
        emitir(lFin + ":");
    }

    private void traducirConsultarGrafo(String op, String nodoBuscado, String grafo, String resultado) {
        String tipo = estructurasTipo.get(grafo);
        if (!"GRAFO".equals(tipo)) {
            traducirPropiedadEstructura(op, grafo, resultado);
            return;
        }

        String lLoop = nuevaEtiquetaInterna();
        String lNoMatch = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        registrarVariable(resultado);
        emitir("    ; " + op + " " + nodoBuscado + " EN " + grafo + " recorriendo aristas");
        emitir("    mov word ptr [" + resultado + "], 0");
        cargarAX(nodoBuscado);
        emitir("    mov dx, ax");
        emitir("    xor si, si");
        emitir(lLoop + ":");
        emitir("    cmp si, [" + grafo + "_edge_count]");
        emitir("    jge " + lFin);
        emitir("    mov bx, si");
        emitir("    shl bx, 1");
        emitir("    cmp " + grafo + "_edges_from[bx], dx");
        emitir("    jne " + lNoMatch);
        emitir("    mov ax, " + grafo + "_edges_to[bx]");
        emitir("    mov [" + resultado + "], ax");
        emitir("    push dx");
        emitir("    call print_num");
        emitir("    mov dx, offset newline");
        emitir("    call print_string");
        emitir("    pop dx");
        emitir(lNoMatch + ":");
        emitir("    inc si");
        emitir("    jmp " + lLoop);
        emitir(lFin + ":");
    }

    private void traducirConsultarHash(String op, String hash, String resultado) {
        registrarVariable(resultado);
        emitir("    ; " + op + " EN " + hash);
        if ("TAMANO".equals(op)) {
            emitir("    mov ax, [" + hash + "_count]");
            emitir("    mov [" + resultado + "], ax");
        } else {
            emitir("    mov word ptr [" + resultado + "], 0");
        }
    }

    private void traducirBooleanoPorContador(String op, String contador, String resultado, int capacidad) {
        String lTrue = nuevaEtiquetaInterna();
        String lFin = nuevaEtiquetaInterna();

        emitir("    ; " + op);
        emitir("    mov word ptr [" + resultado + "], 0");

        if ("VACIA".equals(op)) {
            emitir("    cmp word ptr [" + contador + "], 0");
            emitir("    je " + lTrue);
        } else {
            emitir("    cmp word ptr [" + contador + "], " + capacidad);
            emitir("    jge " + lTrue);
        }

        emitir("    jmp " + lFin);
        emitir(lTrue + ":");
        emitir("    mov word ptr [" + resultado + "], 1");
        emitir(lFin + ":");
    }

    private void traducirError(String msg1, String msg2) {
        emitir("    ; ERROR: " + msg1 + " " + msg2);
        emitir("    mov dx, offset msg_error");
        emitir("    call print_string");
    }

    private void traducirIfFalse(String condicion, String etiqueta) {
        emitir("    ; IF_FALSE " + condicion + " GOTO " + etiqueta);
        cargarAX(condicion);
        emitir("    cmp ax, 0");
        emitir("    je " + etiquetaValida(etiqueta));
    }

    private void traducirIfTrue(String condicion, String etiqueta) {
        emitir("    ; IF_TRUE " + condicion + " GOTO " + etiqueta);
        cargarAX(condicion);
        emitir("    cmp ax, 0");
        emitir("    jne " + etiquetaValida(etiqueta));
    }

    private void cargarAX(String valor) {
        if (esNumero(valor)) {
            emitir("    mov ax, " + valor);
        } else if (esIdentificador(valor)) {
            registrarVariable(valor);
            emitir("    mov ax, [" + valor + "]");
        } else {
            emitir("    mov ax, 0");
        }
    }

    private void cargarBX(String valor) {
        if (esNumero(valor)) {
            emitir("    mov bx, " + valor);
        } else if (esIdentificador(valor)) {
            registrarVariable(valor);
            emitir("    mov bx, [" + valor + "]");
        } else {
            emitir("    mov bx, 0");
        }
    }

    private void registrarVariable(String nombre) {
        if (esIdentificador(nombre) && !textosDeclarados.containsKey(nombre)) {
            variablesDeclaradas.add(nombre);
            variablesUsadas.add(nombre);
        }
    }

    private void actualizarCache(String registro, String variable) {
        cacheRegistros.put(registro, variable);
        variablesUsadas.add(variable);
    }

    private String nuevaEtiquetaInterna() {
        return "ASM_L" + (contadorEtiquetasInternas++);
    }

    private String declararTextoAnonimo(String texto) {
        String etiqueta = "msg_" + (contadorTextos++);
        textosDeclarados.put(etiqueta, texto);
        return etiqueta;
    }

    private String saltoComparacion(String op) {
        switch (op) {
            case "==": return "je";
            case "!=": return "jne";
            case "<": return "jl";
            case ">": return "jg";
            case "<=": return "jle";
            case ">=": return "jge";
            default: return "je";
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim();
        return "null".equalsIgnoreCase(limpio) ? "" : limpio;
    }

    private boolean esNumero(String valor) {
        return valor != null && valor.matches("-?\\d+");
    }

    private boolean esCadena(String valor) {
        return valor != null && valor.length() >= 2 && valor.startsWith("\"") && valor.endsWith("\"");
    }

    private boolean esIdentificador(String valor) {
        return valor != null && valor.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private String limpiarCadena(String valor) {
        String texto = valor.substring(1, valor.length() - 1);
        return texto.replace("'", "''");
    }

    private String etiquetaValida(String valor) {
        if (esIdentificador(valor)) {
            return valor;
        }
        return nuevaEtiquetaInterna();
    }

    private boolean esMovimiento(String linea) {
        return linea.trim().startsWith("mov ");
    }

    private boolean esOperacionNeutra(String linea) {
        String trim = linea.trim();
        return (trim.startsWith("add ") && trim.endsWith(", 0")) || (trim.startsWith("sub ") && trim.endsWith(", 0"));
    }

    private String extraerDestino(String linea) {
        try {
            String[] partes = linea.trim().split(",");
            return partes[0].replace("mov", "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String extraerFuente(String linea) {
        try {
            String[] partes = linea.trim().split(",");
            return partes[1].trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void emitir(String linea) {
        codigoEnsamblador.add(linea);
    }

    private void generarRutinasAuxiliares() {
        emitir("");
        emitir("; ============================================");
        emitir("; RUTINAS AUXILIARES");
        emitir("; ============================================");
        emitir("");
        emitir("print_string proc");
        emitir("    mov ah, 09h");
        emitir("    int 21h");
        emitir("    ret");
        emitir("print_string endp");
        emitir("");
        emitir("print_num proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    cmp ax, 0");
        emitir("    jne pn_convert");
        emitir("    mov dl, '0'");
        emitir("    mov ah, 02h");
        emitir("    int 21h");
        emitir("    jmp pn_done");
        emitir("pn_convert:");
        emitir("    xor cx, cx");
        emitir("    mov bx, 10");
        emitir("pn_loop:");
        emitir("    xor dx, dx");
        emitir("    div bx");
        emitir("    push dx");
        emitir("    inc cx");
        emitir("    cmp ax, 0");
        emitir("    jne pn_loop");
        emitir("pn_print:");
        emitir("    pop dx");
        emitir("    add dl, '0'");
        emitir("    mov ah, 02h");
        emitir("    int 21h");
        emitir("    loop pn_print");
        emitir("pn_done:");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("print_num endp");
        emitir("");
    }

    private void generarFinalizacion() {
        emitir("");
        emitir("; ============================================");
        emitir("; FIN DEL PROGRAMA");
        emitir("; ============================================");
        if (!rutinasArbolRecursivas.isEmpty()) {
            String lSalidaPrograma = nuevaEtiquetaInterna();
            emitir("    jmp " + lSalidaPrograma);
            emitir("");
            emitir("; ============================================");
            emitir("; RUTINAS RECURSIVAS DE ARBOL");
            emitir("; ============================================");
            for (String linea : rutinasArbolRecursivas) {
                emitir(linea);
            }
            emitir("");
            emitir(lSalidaPrograma + ":");
        }
        emitir("    mov ax, 4C00h");
        emitir("    int 21h");
        emitir("");
        emitir("main endp");
    }

    private void optimizarCodigo() {
        if (!optimizacionActiva || codigoEnsamblador.size() < 2) {
            return;
        }
        List<String> optimizado = new ArrayList<>();
        for (int i = 0; i < codigoEnsamblador.size(); i++) {
            String actual = codigoEnsamblador.get(i);
            String siguiente = (i + 1 < codigoEnsamblador.size()) ? codigoEnsamblador.get(i + 1) : "";
            if (esMovimiento(actual) && esMovimiento(siguiente)
                    && extraerDestino(actual).equals(extraerDestino(siguiente))
                    && extraerFuente(actual).equals(extraerFuente(siguiente))) {
                optimizado.add(actual);
                i++;
                continue;
            }
            if (!esOperacionNeutra(actual)) {
                optimizado.add(actual);
            }
        }
        codigoEnsamblador = optimizado;
    }

    private void dibujarArbol(String nombreArbol) {
        NodoArbol raiz = arboles.get(nombreArbol);
        outputVisualizacion.append("\nARBOL BINARIO: ").append(nombreArbol).append("\n");
        if (raiz == null) {
            outputVisualizacion.append("[ Vacio ]\n");
        } else {
            dibujarNodoRecursivo(raiz, "", true);
        }
    }

    private void dibujarNodoRecursivo(NodoArbol nodo, String prefijo, boolean esUltimo) {
        if (nodo == null) {
            return;
        }
        outputVisualizacion.append(prefijo)
                .append(esUltimo ? "`-- " : "|-- ")
                .append("[")
                .append(nodo.clave)
                .append(":")
                .append(nodo.valor)
                .append("]\n");
        String extension = prefijo + (esUltimo ? "    " : "|   ");
        dibujarNodoRecursivo(nodo.izquierda, extension, nodo.derecha == null);
        dibujarNodoRecursivo(nodo.derecha, extension, true);
    }

    public String obtenerCodigoEnsamblador() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigoEnsamblador) {
            if (".code".equals(linea)) {
                for (Map.Entry<String, String> entry : textosDeclarados.entrySet()) {
                    sb.append("    ")
                            .append(entry.getKey())
                            .append(" db '")
                            .append(entry.getValue())
                            .append("', '$'\n");
                }
                if (heapNecesario) {
                    sb.append("    HEAP dw 1000 dup(0)\n");
                    sb.append("    HEAP_PTR dw 2\n");
                }
                if (colaRecorridoArbolNecesaria) {
                    sb.append("    ARBOL_QUEUE dw 128 dup(0)\n");
                    sb.append("    ARBOL_Q_FRONT dw 0\n");
                    sb.append("    ARBOL_Q_REAR dw 0\n");
                }
                for (Map.Entry<String, String> entry : estructurasTipo.entrySet()) {
                    String nombre = entry.getKey();
                    String tipo = entry.getValue();
                    int tamano = estructurasTamano.getOrDefault(nombre, 100);

                    if ("PILA".equals(tipo)) {
                        sb.append("    ").append(nombre).append(" dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_top dw 0\n");
                    } else if ("COLA".equals(tipo)) {
                        sb.append("    ").append(nombre).append(" dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_front dw 0\n");
                        sb.append("    ").append(nombre).append("_rear dw 0\n");
                        sb.append("    ").append(nombre).append("_count dw 0\n");
                    } else if ("LISTA".equals(tipo)) {
                        sb.append("    ").append(nombre).append("_head dw 0\n");
                        sb.append("    ").append(nombre).append("_tail dw 0\n");
                    } else if ("ARBOL".equals(tipo)) {
                        sb.append("    ").append(nombre).append("_root dw 0\n");
                    } else if ("GRAFO".equals(tipo)) {
                        sb.append("    ").append(nombre).append("_nodes dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_node_count dw 0\n");
                        sb.append("    ").append(nombre).append("_edges_from dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_edges_to dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_edge_count dw 0\n");
                    } else if ("HASH".equals(tipo)) {
                        sb.append("    ").append(nombre).append("_keys dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_values dw ").append(tamano).append(" dup(0)\n");
                        sb.append("    ").append(nombre).append("_count dw 0\n");
                    }
                }
                for (String variable : variablesDeclaradas) {
                    if (!textosDeclarados.containsKey(variable) && !estructurasTipo.containsKey(variable)) {
                        sb.append("    ").append(variable).append(" dw 0\n");
                    }
                }
                sb.append("\n");
            }
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }
    public String obtenerVisualizacion() {
        return outputVisualizacion.toString();
    }

    public void imprimirCodigoCompleto() {
        System.out.println(obtenerCodigoEnsamblador());
        System.out.println(obtenerVisualizacion());
    }
    public void procesarCuadruplos(List<Cuadruplo> cuadruplos) {
        for (Cuadruplo c : cuadruplos) {
            traducirCuadruplo(c);
        }
        generarFinalizacion();
        generarRutinasAuxiliares();
        emitir("end main");
        if (optimizacionActiva) {
            optimizarCodigo();
        }
    }
    public void establecerOptimizacion(boolean activa) {
        this.optimizacionActiva = activa;
    }
}
