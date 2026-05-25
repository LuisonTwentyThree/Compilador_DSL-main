//hola
package compilador.codegen.asm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compilador.core.Cuadruplo;

/**
 * Backend grafico 8086 para EMU8086/MASM.
 *
 * Toma los cuadruplos optimizados, inicializa modo 13h y redibuja las
 * estructuras del DSL como rectangulos/nodos usando BIOS int 10h.
 */
public class GeneradorEnsambladorGrafico {
    // Backend ASM grafico: genera un programa que entra a modo 13h y dibuja
    // estructuras.
    private final List<String> codigo;
    private final Set<String> variables;
    private final Set<String> temporalesRecorrido;
    private final Set<String> temporalesTamano;
    private final Set<String> temporalesTope;
    private final Map<String, Integer> temporalesFrenteY;
    private final Map<String, String> estructurasTipo;
    private final Map<String, Integer> estructurasTamano;
    private boolean heapNecesario;
    private boolean colaNivelesNecesaria;
    private boolean recorridoGraficoEmitido;
    private int contadorEtiquetas;

    public GeneradorEnsambladorGrafico() {
        this.codigo = new ArrayList<>();
        this.variables = new LinkedHashSet<>();
        this.temporalesRecorrido = new LinkedHashSet<>();
        this.temporalesTamano = new LinkedHashSet<>();
        this.temporalesTope = new LinkedHashSet<>();
        this.temporalesFrenteY = new LinkedHashMap<>();
        this.estructurasTipo = new LinkedHashMap<>();
        this.estructurasTamano = new LinkedHashMap<>();
        this.heapNecesario = false;
        this.colaNivelesNecesaria = false;
        this.recorridoGraficoEmitido = false;
        this.contadorEtiquetas = 1;
        agregarEncabezado();
    }

    private void agregarEncabezado() {
        // A diferencia del ASM normal, aqui se inicializa modo grafico 13h
        // desde el inicio para poder pintar pixeles con BIOS int 10h.
        emitir("; ============================================");
        emitir("; CODIGO ENSAMBLADOR GRAFICO GENERADO - DSL");
        emitir("; Intel 8086 / EMU8086 / MASM - Modo 13h");
        emitir("; ============================================");
        emitir("");
        emitir(".model small");
        emitir(".stack 1000h");
        emitir("");
        emitir(".data");
        emitir("    titulo db 'DSL - VISUALIZACION GRAFICA', 0");
        emitir("");
        emitir(".code");
        emitir("main proc");
        emitir("    mov ax, @data");
        emitir("    mov ds, ax");
        emitir("");
        emitir("    ; Modo grafico 13h: 320x200, 256 colores");
        emitir("    mov ax, 0013h");
        emitir("    int 10h");
        emitir("");
    }

    public void traducirCuadruplo(Cuadruplo c) {
        // Este switch no intenta cubrir todo el lenguaje con detalle textual;
        // se enfoca en operaciones que cambian estructuras y por eso requieren redibujo.
        if (c == null || c.operador == null) {
            return;
        }

        String op = c.operador.toUpperCase();
        String arg1 = normalizar(c.argumento1);
        String arg2 = normalizar(c.argumento2);
        String res = normalizar(c.resultado);

        switch (op) {
            case "ALLOC":
                traducirAlloc(arg1, arg2, res);
                break;
            case "ETIQUETA":
                traducirEtiqueta(res);
                break;
            case "GOTO":
                traducirGoto(res);
                break;
            case "IF_FALSE":
                traducirSaltoCondicional(arg1, res, false);
                break;
            case "IF_TRUE":
                traducirSaltoCondicional(arg1, res, true);
                break;
            case "=":
                registrarVariable(res);
                cargarAX(arg1);
                emitir("    mov [" + res + "], ax");
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                traducirAritmetica(op, arg1, arg2, res);
                break;
            case "<":
            case ">":
            case "==":
            case "!=":
            case "<=":
            case ">=":
                traducirComparacion(op, arg1, arg2, res);
                break;
            case "APILAR":
            case "PUSH":
            case "ENCOLAR":
            case "ENQUEUE":
            case "INSERTAR":
            case "INSERTAR_FINAL":
            case "INSERTAR_INICIO":
            case "AGREGARNODO":
            case "AGREGARARISTA":
            case "ACTUALIZAR":
                traducirInsercion(op, arg1, arg2, res);
                break;
            case "DESAPILAR":
            case "POP":
            case "DESENCOLAR":
            case "DEQUEUE":
            case "ELIMINAR_INICIO":
            case "ELIMINAR_FINAL":
            case "ELIMINAR_FRENTE":
                traducirEliminacion(op, res);
                break;
            case "PRINT":
            case "MOSTRAR":
                traducirPrint(arg1);
                break;
            case "TOPE":
            case "PEEK":
                traducirTopePila(op, arg1, res);
                break;
            case "FRENTE":
            case "FRONT":
                traducirFrenteCola(op, arg1, res);
                break;
            case "TAMANO":
                traducirTamanoEstructura(arg1, res);
                break;
            case "VACIA":
                traducirEstadoEstructura(arg1, res, true);
                break;
            case "LLENA":
                traducirEstadoEstructura(arg1, res, false);
                break;
            case "BUSCAR":
                traducirBuscarEstructura(arg1, res);
                break;
            case "PREORDEN":
            case "INORDEN":
            case "POSTORDEN":
            case "RECORRIDOPORNIVELES":
                traducirRecorridoArbol(op, arg1, res);
                break;
            case "ELIMINAR":
            case "ELIMINAR_POSICION":
                traducirEliminacionConParam(op, arg1, res);
                break;
            case "VECINOS":
                traducirVecinosGrafo(arg1, arg2, res);
                break;
            default:
                emitir("    ; Operacion grafica pendiente: " + op + " " + arg1 + " " + arg2 + " -> " + res);
                break;
        }
    }

    public void procesarCuadruplos(List<Cuadruplo> cuadruplos) {
        // Recibe la lista optimizada generada por el compilador.
        // Cada operacion relevante actualiza memoria y llama a GRAFICAR_TODO.
        if (cuadruplos != null) {
            for (Cuadruplo c : cuadruplos) {
                traducirCuadruplo(c);
            }
        }
        finalizarPrograma();
    }

    private void traducirEtiqueta(String etiqueta) {
        if (!etiqueta.isEmpty()) {
            emitir(etiqueta + ":");
        }
    }

    private void traducirGoto(String etiqueta) {
        if (!etiqueta.isEmpty()) {
            emitir("    jmp " + etiqueta);
        }
    }

    private void traducirSaltoCondicional(String condicion, String etiqueta, boolean saltarSiVerdadero) {
        if (condicion.isEmpty() || etiqueta.isEmpty()) {
            return;
        }
        cargarAX(condicion);
        emitir("    cmp ax, 0");
        emitir("    " + (saltarSiVerdadero ? "jne " : "je ") + etiqueta);
    }

    private void traducirAritmetica(String op, String arg1, String arg2, String resultado) {
        if (resultado.isEmpty()) {
            return;
        }
        registrarVariable(resultado);
        emitir("    ; " + resultado + " = " + arg1 + " " + op + " " + arg2);
        cargarAX(arg1);

        if ("+".equals(op)) {
            emitirOperacionConAX("add", arg2);
        } else if ("-".equals(op)) {
            emitirOperacionConAX("sub", arg2);
        } else if ("*".equals(op)) {
            cargarBX(arg2);
            emitir("    imul bx");
        } else if ("/".equals(op)) {
            cargarBX(arg2);
            String fin = nuevaEtiqueta();
            emitir("    cmp bx, 0");
            emitir("    je " + fin);
            emitir("    cwd");
            emitir("    idiv bx");
            emitir(fin + ":");
        }

        emitir("    mov [" + resultado + "], ax");
    }

    private void traducirComparacion(String op, String arg1, String arg2, String resultado) {
        if (resultado.isEmpty()) {
            return;
        }
        registrarVariable(resultado);
        String verdadero = nuevaEtiqueta();
        String fin = nuevaEtiqueta();

        emitir("    ; " + resultado + " = " + arg1 + " " + op + " " + arg2);
        cargarAX(arg1);
        compararAXCon(arg2);
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir("    " + saltoComparacion(op) + " " + verdadero);
        emitir("    jmp " + fin);
        emitir(verdadero + ":");
        emitir("    mov word ptr [" + resultado + "], 1");
        emitir(fin + ":");
    }

    private void traducirEstadoEstructura(String estructura, String resultado, boolean consultarVacia) {
        if (estructura.isEmpty() || resultado.isEmpty()) {
            return;
        }
        registrarVariable(resultado);

        String tipo = estructurasTipo.get(estructura);
        String contador = contadorEstructura(estructura, tipo);
        String verdadero = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        int capacidad = estructurasTamano.getOrDefault(estructura, 100);

        emitir("    ; " + (consultarVacia ? "VACIA" : "LLENA") + " EN " + estructura + " -> " + resultado);
        emitir("    mov word ptr [" + resultado + "], 0");

        if (contador == null) {
            emitir("    ; Estado pendiente para " + estructura);
            return;
        }

        if (consultarVacia) {
            emitir("    cmp word ptr [" + contador + "], 0");
        } else {
            emitir("    cmp word ptr [" + contador + "], " + capacidad);
        }
        emitir("    je " + verdadero);
        emitir("    jmp " + fin);
        emitir(verdadero + ":");
        emitir("    mov word ptr [" + resultado + "], 1");
        emitir(fin + ":");
    }

    private void traducirAlloc(String tamano, String tipo, String nombre) {
        // Registra el tipo y capacidad de cada estructura para luego declarar
        // sus arreglos en .data y saber que rutina de dibujo necesita.
        if (nombre.isEmpty()) {
            return;
        }

        String tipoAsm = normalizarTipo(tipo);
        int capacidad = parseInt(tamano, 100);

        estructurasTipo.put(nombre, tipoAsm);
        estructurasTamano.put(nombre, capacidad);

        if ("LISTA".equals(tipoAsm) || "ARBOL".equals(tipoAsm)) {
            heapNecesario = true;
        }

        emitir("    ; CREAR " + tipoAsm + " " + nombre + " TAMANO " + capacidad);
        emitir("    call GRAFICAR_TODO");
    }

    private void traducirInsercion(String op, String arg1, String arg2, String estructura) {
        // Las inserciones modifican el estado interno de la estructura y luego
        // fuerzan un redibujo completo para reflejar el nuevo estado visual.
        String tipo = estructurasTipo.get(estructura);
        if (tipo == null) {
            registrarVariable(estructura);
            cargarAX(arg2.isEmpty() ? arg1 : arg2);
            emitir("    mov [" + estructura + "], ax");
            return;
        }

        if ("PILA".equals(tipo) && ("APILAR".equals(op) || "PUSH".equals(op))) {
            int cap = estructurasTamano.getOrDefault(estructura, 100);
            String fin = nuevaEtiqueta();
            emitir("    ; APILAR " + arg1 + " EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_top], " + cap);
            emitir("    jge " + fin);
            cargarAX(arg1);
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    shl bx, 1");
            emitir("    mov " + estructura + "[bx], ax");
            emitir("    inc word ptr [" + estructura + "_top]");
            emitir(fin + ":");
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("COLA".equals(tipo) && ("ENCOLAR".equals(op) || "ENQUEUE".equals(op))) {
            int cap = estructurasTamano.getOrDefault(estructura, 100);
            String noWrap = nuevaEtiqueta();
            String fin = nuevaEtiqueta();
            emitir("    ; ENCOLAR " + arg1 + " EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_count], " + cap);
            emitir("    jge " + fin);
            cargarAX(arg1);
            emitir("    mov bx, [" + estructura + "_rear]");
            emitir("    shl bx, 1");
            emitir("    mov " + estructura + "[bx], ax");
            emitir("    inc word ptr [" + estructura + "_rear]");
            emitir("    cmp word ptr [" + estructura + "_rear], " + cap);
            emitir("    jl " + noWrap);
            emitir("    mov word ptr [" + estructura + "_rear], 0");
            emitir(noWrap + ":");
            emitir("    inc word ptr [" + estructura + "_count]");
            emitir(fin + ":");
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("LISTA".equals(tipo) && op.startsWith("INSERTAR")) {
            insertarLista(op, arg2.isEmpty() ? arg1 : arg2, estructura);
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("ARBOL".equals(tipo) && "AGREGARNODO".equals(op)) {
            insertarArbol(arg2.isEmpty() ? arg1 : arg2, estructura);
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("GRAFO".equals(tipo) && "AGREGARNODO".equals(op)) {
            insertarNodoGrafo(arg1, estructura);
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("GRAFO".equals(tipo) && "AGREGARARISTA".equals(op)) {
            insertarAristaGrafo(arg1, arg2, estructura);
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("HASH".equals(tipo) && "INSERTAR".equals(op)) {
            insertarHash(arg1, arg2, estructura);
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("HASH".equals(tipo) && "ACTUALIZAR".equals(op)) {
            actualizarHash(arg1, arg2, estructura);
            emitir("    call GRAFICAR_TODO");
        }
    }

    private void traducirBuscarEstructura(String clave, String estructura) {
        String tipo = estructurasTipo.get(estructura);
        if ("HASH".equals(tipo)) {
            buscarHash(clave, estructura);
            return;
        }

        emitir("    ; Operacion grafica pendiente: BUSCAR " + clave + " EN " + estructura);
        emitir("    call GRAFICAR_TODO");
    }

    private void traducirEliminacion(String op, String estructura) {
        // Igual que las inserciones, las eliminaciones actualizan memoria y
        // despues llaman a GRAFICAR_TODO para refrescar la pantalla.
        String tipo = estructurasTipo.get(estructura);
        if ("PILA".equals(tipo) && ("DESAPILAR".equals(op) || "POP".equals(op))) {
            String vacia = nuevaEtiqueta();
            String fin = nuevaEtiqueta();
            emitir("    ; DESAPILAR EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_top], 0");
            emitir("    je " + vacia);
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    dec bx");
            emitir("    shl bx, 1");
            emitir("    mov ax, " + estructura + "[bx]");
            emitir("    mov [gfx_ultimo_desapilado], ax");
            emitir("    dec word ptr [" + estructura + "_top]");
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    shl bx, 1");
            emitir("    mov word ptr " + estructura + "[bx], 0");
            emitir("    call GRAFICAR_TODO");
            emitir("    mov cx, 10");
            emitir("    mov dx, 82");
            emitir("    call SET_CURSOR_PIXEL");
            emitirTextoGrafico("DESAPILAR: ");
            emitir("    mov ax, [gfx_ultimo_desapilado]");
            emitir("    call PRINT_NUM_GRAFICO");
            emitir("    jmp " + fin);
            emitir(vacia + ":");
            emitir("    call GRAFICAR_TODO");
            emitir(fin + ":");
            recorridoGraficoEmitido = true;
            return;
        }

        if ("COLA".equals(tipo) && ("DESENCOLAR".equals(op) || "DEQUEUE".equals(op)
                || "ELIMINAR_FRENTE".equals(op))) {
            int cap = estructurasTamano.getOrDefault(estructura, 100);
            String noWrap = nuevaEtiqueta();
            String fin = nuevaEtiqueta();
            emitir("    ; DESENCOLAR EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_count], 0");
            emitir("    je " + fin);
            emitir("    mov bx, [" + estructura + "_front]");
            emitir("    shl bx, 1");
            emitir("    mov word ptr " + estructura + "[bx], 0");
            emitir("    inc word ptr [" + estructura + "_front]");
            emitir("    cmp word ptr [" + estructura + "_front], " + cap);
            emitir("    jl " + noWrap);
            emitir("    mov word ptr [" + estructura + "_front], 0");
            emitir(noWrap + ":");
            emitir("    dec word ptr [" + estructura + "_count]");
            emitir(fin + ":");
            emitir("    call GRAFICAR_TODO");
            return;
        }

        if ("LISTA".equals(tipo)) {
            if ("ELIMINAR_FINAL".equals(op)) {
                eliminarFinalLista(estructura);
            } else {
                String fin = nuevaEtiqueta();
                emitir("    ; " + op + " EN " + estructura);
                emitir("    cmp word ptr [" + estructura + "_head], 0");
                emitir("    je " + fin);
                emitir("    mov bx, [" + estructura + "_head]");
                emitir("    mov ax, HEAP[bx+2]");
                emitir("    mov [" + estructura + "_head], ax");
                emitir("    dec word ptr [" + estructura + "_count]");
                emitir("    cmp ax, 0");
                emitir("    jne " + fin);
                emitir("    mov word ptr [" + estructura + "_tail], 0");
                emitir(fin + ":");
                emitir("    call GRAFICAR_TODO");
            }
        }
    }

    private void traducirEliminacionConParam(String op, String valor, String estructura) {
        String tipo = estructurasTipo.get(estructura);
        if (tipo == null) {
            emitir("    ; Operacion grafica pendiente: " + op + " " + valor + " -> " + estructura);
            return;
        }
        if ("LISTA".equals(tipo) && "ELIMINAR".equals(op)) {
            eliminarListaPorValor(valor, estructura);
        } else if ("LISTA".equals(tipo) && "ELIMINAR_POSICION".equals(op)) {
            eliminarListaPorPosicion(valor, estructura);
        } else if ("ARBOL".equals(tipo) && "ELIMINAR".equals(op)) {
            eliminarArbol(valor, estructura);
        } else {
            emitir("    ; Operacion grafica pendiente: " + op + " " + valor + " -> " + estructura);
            emitir("    call GRAFICAR_TODO");
        }
    }

    private void eliminarFinalLista(String lista) {
        String soloUno = nuevaEtiqueta();
        String loop = nuevaEtiqueta();
        String encontrado = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; ELIMINAR_FINAL EN " + lista);
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    je " + fin);
        emitir("    mov bx, [" + lista + "_head]");
        emitir("    cmp bx, [" + lista + "_tail]");
        emitir("    je " + soloUno);
        // More than one node: find penultimate
        emitir("    mov si, bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir(loop + ":");
        emitir("    cmp word ptr HEAP[bx+2], 0");
        emitir("    je " + encontrado);
        emitir("    mov si, bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + loop);
        emitir(encontrado + ":");
        emitir("    mov word ptr HEAP[si+2], 0");
        emitir("    mov [" + lista + "_tail], si");
        emitir("    dec word ptr [" + lista + "_count]");
        emitir("    jmp " + fin);
        emitir(soloUno + ":");
        emitir("    mov word ptr [" + lista + "_head], 0");
        emitir("    mov word ptr [" + lista + "_tail], 0");
        emitir("    mov word ptr [" + lista + "_count], 0");
        emitir(fin + ":");
        emitir("    call GRAFICAR_TODO");
    }

    private void eliminarListaPorValor(String valor, String lista) {
        String esHead = nuevaEtiqueta();
        String loop = nuevaEtiqueta();
        String encontrado = nuevaEtiqueta();
        String decCount = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; ELIMINAR " + valor + " EN " + lista);
        cargarAX(valor);
        emitir("    mov [gfx_busqueda], ax");
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    je " + fin);
        emitir("    mov bx, [" + lista + "_head]");
        emitir("    mov ax, HEAP[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    je " + esHead);
        emitir("    mov si, bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir(loop + ":");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov ax, HEAP[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    je " + encontrado);
        emitir("    mov si, bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + loop);
        emitir(encontrado + ":");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov HEAP[si+2], ax");
        emitir("    cmp ax, 0");
        emitir("    jne " + decCount);
        emitir("    mov [" + lista + "_tail], si");
        emitir("    jmp " + decCount);
        emitir(esHead + ":");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov [" + lista + "_head], ax");
        emitir("    cmp ax, 0");
        emitir("    jne " + decCount);
        emitir("    mov word ptr [" + lista + "_tail], 0");
        emitir(decCount + ":");
        emitir("    dec word ptr [" + lista + "_count]");
        emitir(fin + ":");
        emitir("    call GRAFICAR_TODO");
    }

    private void eliminarListaPorPosicion(String pos, String lista) {
        String buscarPos = nuevaEtiqueta();
        String avanzar = nuevaEtiqueta();
        String actualizarTail = nuevaEtiqueta();
        String decCount = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; ELIMINAR_POSICION " + pos + " EN " + lista);
        cargarAX(pos);
        emitir("    mov [gfx_i], ax");
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    je " + fin);
        emitir("    cmp word ptr [gfx_i], 0");
        emitir("    jne " + buscarPos);
        // Position 0: remove head
        emitir("    mov bx, [" + lista + "_head]");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov [" + lista + "_head], ax");
        emitir("    cmp ax, 0");
        emitir("    jne " + decCount);
        emitir("    mov word ptr [" + lista + "_tail], 0");
        emitir("    jmp " + decCount);
        // Position > 0: traverse to position-1
        emitir(buscarPos + ":");
        emitir("    mov si, [" + lista + "_head]");
        emitir("    mov cx, 0");
        emitir(avanzar + ":");
        emitir("    inc cx");
        emitir("    cmp cx, [gfx_i]");
        emitir("    jge " + actualizarTail);
        emitir("    mov ax, HEAP[si+2]");
        emitir("    cmp ax, 0");
        emitir("    je " + fin);
        emitir("    mov si, ax");
        emitir("    jmp " + avanzar);
        emitir(actualizarTail + ":");
        emitir("    mov bx, HEAP[si+2]");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov HEAP[si+2], ax");
        emitir("    cmp ax, 0");
        emitir("    jne " + decCount);
        emitir("    mov [" + lista + "_tail], si");
        emitir(decCount + ":");
        emitir("    dec word ptr [" + lista + "_count]");
        emitir(fin + ":");
        emitir("    call GRAFICAR_TODO");
    }

    private void eliminarArbol(String valor, String arbol) {
        String find = nuevaEtiqueta();
        String goRight = nuevaEtiqueta();
        String found = nuevaEtiqueta();
        String hasLeft = nuevaEtiqueta();
        String twoChildren = nuevaEtiqueta();
        String findSucc = nuevaEtiqueta();
        String succFound = nuevaEtiqueta();
        String succRight = nuevaEtiqueta();
        String doReplace = nuevaEtiqueta();
        String replParent = nuevaEtiqueta();
        String replRight = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; ELIMINAR " + valor + " EN " + arbol);
        cargarAX(valor);
        emitir("    mov [gfx_busqueda], ax");
        emitir("    mov bx, [" + arbol + "_root]");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov word ptr [gfx_i], 0");
        emitir("    mov word ptr [gfx_valor], 0");
        // Find node: bx=current, gfx_i=parent, gfx_valor=direction(0=root,1=left,2=right)
        emitir(find + ":");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov ax, HEAP[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    je " + found);
        emitir("    jl " + goRight);
        emitir("    mov [gfx_i], bx");
        emitir("    mov word ptr [gfx_valor], 1");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + find);
        emitir(goRight + ":");
        emitir("    mov [gfx_i], bx");
        emitir("    mov word ptr [gfx_valor], 2");
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    jmp " + find);
        // Node found
        emitir(found + ":");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    mov cx, HEAP[bx+4]");
        // No left child: replacement = right child (cx)
        emitir("    cmp ax, 0");
        emitir("    jne " + hasLeft);
        emitir("    mov dx, cx");
        emitir("    jmp " + doReplace);
        // Has left child
        emitir(hasLeft + ":");
        emitir("    cmp cx, 0");
        emitir("    jne " + twoChildren);
        emitir("    mov dx, ax");
        emitir("    jmp " + doReplace);
        // Two children: find inorder successor (min of right subtree)
        emitir(twoChildren + ":");
        emitir("    mov [gfx_ultimo_desapilado], bx");
        emitir("    mov si, bx");
        emitir("    mov bx, cx");
        emitir("    mov cl, 2");
        emitir(findSucc + ":");
        emitir("    cmp word ptr HEAP[bx+2], 0");
        emitir("    je " + succFound);
        emitir("    mov si, bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    mov cl, 1");
        emitir("    jmp " + findSucc);
        // Successor found: bx=successor, si=its parent, cl=direction from si to bx
        emitir(succFound + ":");
        emitir("    mov ax, HEAP[bx]");
        emitir("    mov di, [gfx_ultimo_desapilado]");
        emitir("    mov HEAP[di], ax");
        emitir("    mov dx, HEAP[bx+4]");
        emitir("    cmp cl, 1");
        emitir("    jne " + succRight);
        emitir("    mov HEAP[si+2], dx");
        emitir("    jmp " + fin);
        emitir(succRight + ":");
        emitir("    mov HEAP[si+4], dx");
        emitir("    jmp " + fin);
        // Standard replacement (0 or 1 child)
        emitir(doReplace + ":");
        emitir("    cmp word ptr [gfx_i], 0");
        emitir("    jne " + replParent);
        emitir("    mov [" + arbol + "_root], dx");
        emitir("    jmp " + fin);
        emitir(replParent + ":");
        emitir("    mov si, [gfx_i]");
        emitir("    cmp word ptr [gfx_valor], 1");
        emitir("    jne " + replRight);
        emitir("    mov HEAP[si+2], dx");
        emitir("    jmp " + fin);
        emitir(replRight + ":");
        emitir("    mov HEAP[si+4], dx");
        emitir(fin + ":");
        emitir("    call GRAFICAR_TODO");
    }

    private void insertarLista(String op, String valor, String lista) {
        String append = nuevaEtiqueta();
        String noVacia = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; " + op + " " + valor + " EN " + lista);
        cargarAX(valor);
        emitir("    mov si, [HEAP_PTR]");
        emitir("    add word ptr [HEAP_PTR], 4");
        emitir("    mov HEAP[si], ax");
        emitir("    mov word ptr HEAP[si+2], 0");
        if ("INSERTAR_INICIO".equals(op)) {
            emitir("    cmp word ptr [" + lista + "_head], 0");
            emitir("    jne " + noVacia);
            emitir("    mov [" + lista + "_head], si");
            emitir("    mov [" + lista + "_tail], si");
            emitir("    jmp " + fin);
            emitir(noVacia + ":");
            emitir("    mov bx, [" + lista + "_head]");
            emitir("    mov HEAP[si+2], bx");
            emitir("    mov [" + lista + "_head], si");
            emitir("    jmp " + fin);
        }
        emitir("    cmp word ptr [" + lista + "_head], 0");
        emitir("    jne " + append);
        emitir("    mov [" + lista + "_head], si");
        emitir("    mov [" + lista + "_tail], si");
        emitir("    jmp " + fin);
        emitir(append + ":");
        emitir("    mov bx, [" + lista + "_tail]");
        emitir("    mov HEAP[bx+2], si");
        emitir("    mov [" + lista + "_tail], si");
        emitir(fin + ":");
        emitir("    inc word ptr [" + lista + "_count]");
    }

    private void insertarArbol(String valor, String arbol) {
        String rootExiste = nuevaEtiqueta();
        String loop = nuevaEtiqueta();
        String derecha = nuevaEtiqueta();
        String ponerIzq = nuevaEtiqueta();
        String ponerDer = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; AGREGARNODO " + valor + " EN " + arbol);
        cargarAX(valor);
        emitir("    mov si, [HEAP_PTR]");
        emitir("    add word ptr [HEAP_PTR], 6");
        emitir("    mov HEAP[si], ax");
        emitir("    mov word ptr HEAP[si+2], 0");
        emitir("    mov word ptr HEAP[si+4], 0");
        emitir("    cmp word ptr [" + arbol + "_root], 0");
        emitir("    jne " + rootExiste);
        emitir("    mov [" + arbol + "_root], si");
        emitir("    jmp " + fin);
        emitir(rootExiste + ":");
        emitir("    mov bx, [" + arbol + "_root]");
        emitir(loop + ":");
        emitir("    cmp ax, HEAP[bx]");
        emitir("    jg " + derecha);
        emitir("    cmp word ptr HEAP[bx+2], 0");
        emitir("    je " + ponerIzq);
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    jmp " + loop);
        emitir(ponerIzq + ":");
        emitir("    mov HEAP[bx+2], si");
        emitir("    jmp " + fin);
        emitir(derecha + ":");
        emitir("    cmp word ptr HEAP[bx+4], 0");
        emitir("    je " + ponerDer);
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    jmp " + loop);
        emitir(ponerDer + ":");
        emitir("    mov HEAP[bx+4], si");
        emitir(fin + ":");
    }

    private void insertarNodoGrafo(String nodo, String grafo) {
        int cap = estructurasTamano.getOrDefault(grafo, 100);
        String fin = nuevaEtiqueta();
        emitir("    ; AGREGARNODO " + nodo + " EN " + grafo);
        emitir("    cmp word ptr [" + grafo + "_node_count], " + cap);
        emitir("    jge " + fin);
        cargarAX(nodo);
        emitir("    mov bx, [" + grafo + "_node_count]");
        emitir("    shl bx, 1");
        emitir("    mov " + grafo + "_nodes[bx], ax");
        emitir("    inc word ptr [" + grafo + "_node_count]");
        emitir(fin + ":");
    }

    private void insertarAristaGrafo(String origen, String destino, String grafo) {
        int cap = estructurasTamano.getOrDefault(grafo, 100);
        String fin = nuevaEtiqueta();
        emitir("    ; AGREGARARISTA " + origen + " " + destino + " EN " + grafo);
        emitir("    cmp word ptr [" + grafo + "_edge_count], " + cap);
        emitir("    jge " + fin);
        emitir("    mov bx, [" + grafo + "_edge_count]");
        emitir("    shl bx, 1");
        cargarAX(origen);
        emitir("    mov " + grafo + "_edges_from[bx], ax");
        cargarAX(destino);
        emitir("    mov " + grafo + "_edges_to[bx], ax");
        emitir("    inc word ptr [" + grafo + "_edge_count]");
        emitir(fin + ":");
    }

    private void insertarHash(String clave, String valor, String hash) {
        int cap = estructurasTamano.getOrDefault(hash, 100);
        String fin = nuevaEtiqueta();
        emitir("    ; INSERTAR " + clave + " " + valor + " EN " + hash);
        emitir("    cmp word ptr [" + hash + "_count], " + cap);
        emitir("    jge " + fin);
        emitir("    mov bx, [" + hash + "_count]");
        emitir("    shl bx, 1");
        cargarAX(clave);
        emitir("    mov " + hash + "_keys[bx], ax");
        cargarAX(valor);
        emitir("    mov " + hash + "_values[bx], ax");
        emitir("    inc word ptr [" + hash + "_count]");
        emitir(fin + ":");
    }

    private void actualizarHash(String clave, String valor, String hash) {
        int cap = estructurasTamano.getOrDefault(hash, 100);
        String loop = nuevaEtiqueta();
        String encontrado = nuevaEtiqueta();
        String insertar = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir("    ; ACTUALIZAR " + clave + " " + valor + " EN " + hash);
        cargarAX(clave);
        emitir("    mov [gfx_busqueda], ax");
        emitir("    mov si, 0");
        emitir(loop + ":");
        emitir("    cmp si, [" + hash + "_count]");
        emitir("    jge " + insertar);
        emitir("    mov bx, si");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + hash + "_keys[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    je " + encontrado);
        emitir("    inc si");
        emitir("    jmp " + loop);
        emitir(encontrado + ":");
        cargarAX(valor);
        emitir("    mov " + hash + "_values[bx], ax");
        emitir("    jmp " + fin);
        emitir(insertar + ":");
        emitir("    cmp word ptr [" + hash + "_count], " + cap);
        emitir("    jge " + fin);
        emitir("    mov bx, [" + hash + "_count]");
        emitir("    shl bx, 1");
        emitir("    mov ax, [gfx_busqueda]");
        emitir("    mov " + hash + "_keys[bx], ax");
        cargarAX(valor);
        emitir("    mov " + hash + "_values[bx], ax");
        emitir("    inc word ptr [" + hash + "_count]");
        emitir(fin + ":");
    }

    private void buscarHash(String clave, String hash) {
        String loop = nuevaEtiqueta();
        String encontrado = nuevaEtiqueta();
        String noEncontrado = nuevaEtiqueta();
        String imprimir = nuevaEtiqueta();
        emitir("    ; BUSCAR " + clave + " EN " + hash);
        cargarAX(clave);
        emitir("    mov [gfx_busqueda], ax");
        emitir("    mov word ptr [gfx_valor], 0");
        emitir("    mov si, 0");
        emitir(loop + ":");
        emitir("    cmp si, [" + hash + "_count]");
        emitir("    jge " + noEncontrado);
        emitir("    mov bx, si");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + hash + "_keys[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    je " + encontrado);
        emitir("    inc si");
        emitir("    jmp " + loop);
        emitir(encontrado + ":");
        emitir("    mov ax, " + hash + "_values[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    jmp " + imprimir);
        emitir(noEncontrado + ":");
        emitir("    mov word ptr [gfx_valor], 0");
        emitir(imprimir + ":");
        emitir("    mov ax, [gfx_valor]");
        emitir("    mov [gfx_busqueda_resultado], ax");
        emitir("    mov word ptr [gfx_busqueda_activa], 1");
        emitir("    call GRAFICAR_TODO");
    }

    private void traducirPrint(String valor) {
        // En modo grafico no se usa la consola de texto normal; el numero se
        // imprime con una rutina grafica propia sobre la pantalla 13h.
        if (temporalesRecorrido.contains(valor)) {
            emitir("    ; MOSTRAR " + valor + " omitido: el recorrido ya se imprimio en modo grafico");
            return;
        }
        emitir("    ; MOSTRAR " + valor + " en modo grafico");
        if (temporalesTamano.contains(valor)) {
            emitir("    mov cx, 10");
            emitir("    mov dx, 70");
            emitir("    call SET_CURSOR_PIXEL");
            emitirTextoGrafico("TAMANO: ");
        } else if (temporalesTope.contains(valor)) {
            emitir("    mov cx, 10");
            emitir("    mov dx, 94");
            emitir("    call SET_CURSOR_PIXEL");
            emitirTextoGrafico("TOPE: ");
        } else if (temporalesFrenteY.containsKey(valor)) {
            emitir("    call GRAFICAR_TODO");
            emitir("    mov cx, 70");
            emitir("    mov dx, " + temporalesFrenteY.get(valor));
            emitir("    call SET_CURSOR_PIXEL");
            emitirTextoGrafico("FRENTE: ");
        }
        cargarAX(valor);
        emitir("    call PRINT_NUM_GRAFICO");
        recorridoGraficoEmitido = true;
    }

    private void traducirRecorridoArbol(String op, String arbol, String resultado) {
        if (!resultado.isEmpty()) {
            temporalesRecorrido.add(resultado);
        }
        recorridoGraficoEmitido = true;
        if (!"ARBOL".equals(estructurasTipo.get(arbol))) {
            emitir("    ; " + op + " pendiente: " + arbol + " no es ARBOL");
            return;
        }

        int y;
        String rutina;
        String etiqueta;
        if ("PREORDEN".equals(op)) {
            y = 10;
            rutina = "PREORDEN";
            etiqueta = "PREORDEN: ";
        } else if ("INORDEN".equals(op)) {
            y = 22;
            rutina = "INORDEN";
            etiqueta = "INORDEN: ";
        } else if ("POSTORDEN".equals(op)) {
            y = 34;
            rutina = "POSTORDEN";
            etiqueta = "POSTORDEN: ";
        } else {
            y = 46;
            rutina = "NIVELES";
            etiqueta = "NIVELES: ";
            colaNivelesNecesaria = true;
        }

        emitir("    ; " + op + " EN " + arbol + " impreso en modo grafico");
        emitir("    mov cx, 10");
        emitir("    mov dx, " + y);
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico(etiqueta);
        emitir("    mov bx, [" + arbol + "_root]");
        emitir("    call RECORRIDO_" + rutina + "_" + arbol);
    }

    private void traducirVecinosGrafo(String nodo, String grafo, String resultado) {
        if (!resultado.isEmpty()) {
            temporalesRecorrido.add(resultado);
        }
        recorridoGraficoEmitido = true;
        if (!"GRAFO".equals(estructurasTipo.get(grafo))) {
            emitir("    ; VECINOS pendiente: " + grafo + " no es GRAFO");
            return;
        }

        String loop = nuevaEtiqueta();
        String siguiente = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir("    ; VECINOS " + nodo + " EN " + grafo + " impreso en modo grafico");
        emitir("    mov cx, 10");
        emitir("    mov dx, 58");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("VECINOS DE ");
        cargarAX(nodo);
        emitir("    mov [gfx_busqueda], ax");
        emitir("    mov [gfx_valor], ax");
        emitir("    call PRINT_NUM_GRAFICO");
        emitirTextoGrafico(": ");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + grafo + "_edge_count]");
        emitir("    jge " + fin);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + grafo + "_edges_from[bx]");
        emitir("    cmp ax, [gfx_busqueda]");
        emitir("    jne " + siguiente);
        emitir("    mov ax, " + grafo + "_edges_to[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    call PRINT_ESPACIO_GRAFICO");
        emitir(siguiente + ":");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
    }

    private void traducirTamanoEstructura(String estructura, String resultado) {
        registrarVariable(resultado);
        if (!resultado.isEmpty()) {
            temporalesTamano.add(resultado);
        }
        String tipo = estructurasTipo.get(estructura);
        emitir("    ; TAMANO EN " + estructura + " -> " + resultado);
        if ("PILA".equals(tipo)) {
            emitir("    mov ax, [" + estructura + "_top]");
        } else if ("COLA".equals(tipo) || "LISTA".equals(tipo) || "HASH".equals(tipo)) {
            emitir("    mov ax, [" + estructura + "_count]");
        } else if ("GRAFO".equals(tipo)) {
            emitir("    mov ax, [" + estructura + "_node_count]");
        } else {
            emitir("    mov ax, 0");
        }
        emitir("    mov [" + resultado + "], ax");
    }

    private void traducirFrenteCola(String op, String cola, String resultado) {
        registrarVariable(resultado);
        if (!resultado.isEmpty() && !temporalesFrenteY.containsKey(resultado)) {
            temporalesFrenteY.put(resultado, 50 + (temporalesFrenteY.size() * 15));
        }
        recorridoGraficoEmitido = true;
        if (!"COLA".equals(estructurasTipo.get(cola))) {
            emitir("    ; " + op + " pendiente: " + cola + " no es COLA");
            emitir("    mov word ptr [" + resultado + "], 0");
            return;
        }

        String vacia = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir("    ; " + resultado + " = " + op + " " + cola);
        emitir("    cmp word ptr [" + cola + "_count], 0");
        emitir("    je " + vacia);
        emitir("    mov bx, [" + cola + "_front]");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + cola + "[bx]");
        emitir("    mov [" + resultado + "], ax");
        emitir("    jmp " + fin);
        emitir(vacia + ":");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir(fin + ":");
    }

    private void traducirTopePila(String op, String pila, String resultado) {
        registrarVariable(resultado);
        if (!resultado.isEmpty()) {
            temporalesTope.add(resultado);
        }
        if (!"PILA".equals(estructurasTipo.get(pila))) {
            emitir("    ; " + op + " pendiente: " + pila + " no es PILA");
            emitir("    mov word ptr [" + resultado + "], 0");
            return;
        }

        String vacia = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir("    ; " + op + " EN " + pila + " -> " + resultado);
        emitir("    cmp word ptr [" + pila + "_top], 0");
        emitir("    je " + vacia);
        emitir("    mov bx, [" + pila + "_top]");
        emitir("    dec bx");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + pila + "[bx]");
        emitir("    mov [" + resultado + "], ax");
        emitir("    jmp " + fin);
        emitir(vacia + ":");
        emitir("    mov word ptr [" + resultado + "], 0");
        emitir(fin + ":");
    }

    private void finalizarPrograma() {
        // Antes de salir espera una tecla, vuelve a modo texto 03h y termina
        // con int 21h. Asi la ventana grafica no desaparece inmediatamente.
        if (!recorridoGraficoEmitido) {
            emitir("    call GRAFICAR_TODO");
        }
        emitir("    mov ah, 00h");
        emitir("    int 16h");
        emitir("    mov ax, 0003h");
        emitir("    int 10h");
        emitir("    mov ax, 4C00h");
        emitir("    int 21h");
        emitir("main endp");
        emitir("");
        agregarRutinasGraficas();
        emitir("end main");
    }

    private void agregarRutinasGraficas() {
        // Al final del archivo se agregan las subrutinas reutilizables:
        // primitivas de dibujo, GRAFICAR_TODO y rutinas por tipo de estructura.
        emitir("; ============================================");
        emitir("; RUTINAS GRAFICAS");
        emitir("; ============================================");
        emitir("");
        agregarRutinasBase();
        agregarGraficarTodo();
        agregarRutinasPorEstructura();
    }

    private void agregarRutinasBase() {
        emitir("LIMPIAR_PANTALLA proc");
        emitir("    push ax");
        emitir("    mov ax, 0013h");
        emitir("    int 10h");
        emitir("    pop ax");
        emitir("    ret");
        emitir("LIMPIAR_PANTALLA endp");
        emitir("");
        emitir("SET_CURSOR_PIXEL proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    mov ax, dx");
        emitir("    mov bl, 8");
        emitir("    div bl");
        emitir("    mov dh, al");
        emitir("    mov ax, cx");
        emitir("    mov bl, 8");
        emitir("    div bl");
        emitir("    mov dl, al");
        emitir("    mov ah, 02h");
        emitir("    mov bh, 00h");
        emitir("    int 10h");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("SET_CURSOR_PIXEL endp");
        emitir("");
        emitir("PRINT_NUM_GRAFICO proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    cmp ax, 0");
        emitir("    jne png_convertir");
        emitir("    mov al, '0'");
        emitir("    mov ah, 0Eh");
        emitir("    mov bl, [gfx_color]");
        emitir("    int 10h");
        emitir("    jmp png_fin");
        emitir("png_convertir:");
        emitir("    xor cx, cx");
        emitir("    mov bx, 10");
        emitir("png_dividir:");
        emitir("    xor dx, dx");
        emitir("    div bx");
        emitir("    push dx");
        emitir("    inc cx");
        emitir("    cmp ax, 0");
        emitir("    jne png_dividir");
        emitir("png_imprimir:");
        emitir("    pop dx");
        emitir("    mov al, dl");
        emitir("    add al, '0'");
        emitir("    mov ah, 0Eh");
        emitir("    mov bl, [gfx_color]");
        emitir("    int 10h");
        emitir("    loop png_imprimir");
        emitir("png_fin:");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("PRINT_NUM_GRAFICO endp");
        emitir("");
        emitir("PRINT_ESPACIO_GRAFICO proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    mov al, ' '");
        emitir("    mov ah, 0Eh");
        emitir("    mov bl, [gfx_color]");
        emitir("    int 10h");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("PRINT_ESPACIO_GRAFICO endp");
        emitir("");
        emitir("PRINT_VALOR_CORCHETES proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    mov al, '['");
        emitir("    mov ah, 0Eh");
        emitir("    mov bl, [gfx_color]");
        emitir("    int 10h");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    mov al, ']'");
        emitir("    mov ah, 0Eh");
        emitir("    mov bl, [gfx_color]");
        emitir("    int 10h");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("PRINT_VALOR_CORCHETES endp");
        emitir("");
        emitir("PAUSA_GRAFICA proc");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    mov cx, 1");
        emitir("pg_loop_ext:");
        emitir("    mov dx, 1000h");
        emitir("pg_loop_int:");
        emitir("    dec dx");
        emitir("    jnz pg_loop_int");
        emitir("    loop pg_loop_ext");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    ret");
        emitir("PAUSA_GRAFICA endp");
        emitir("");
    }

    private void agregarGraficarTodo() {
        emitir("GRAFICAR_TODO proc");
        emitir("    mov byte ptr [gfx_color], 0Fh");
        emitir("    call LIMPIAR_PANTALLA");
        for (Map.Entry<String, String> e : estructurasTipo.entrySet()) {
            emitir("    call GRAFICAR_" + e.getValue() + "_" + e.getKey());
        }
        emitir("    call DIBUJAR_ULTIMA_BUSQUEDA");
        emitir("    ret");
        emitir("GRAFICAR_TODO endp");
        emitir("");
        rutinaUltimaBusqueda();
    }

    private void rutinaUltimaBusqueda() {
        String fin = "DUB_FIN";
        emitir("DIBUJAR_ULTIMA_BUSQUEDA proc");
        emitir("    cmp word ptr [gfx_busqueda_activa], 1");
        emitir("    jne " + fin);
        emitir("    mov cx, 104");
        emitir("    mov dx, 88");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("BUSCAR ");
        emitir("    mov ax, [gfx_busqueda]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitirTextoGrafico(": ");
        emitir("    mov ax, [gfx_busqueda_resultado]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir(fin + ":");
        emitir("    ret");
        emitir("DIBUJAR_ULTIMA_BUSQUEDA endp");
        emitir("");
    }

    private void agregarRutinasPorEstructura() {
        for (Map.Entry<String, String> e : estructurasTipo.entrySet()) {
            String nombre = e.getKey();
            String tipo = e.getValue();
            if ("PILA".equals(tipo)) {
                rutinaGraficaPila(nombre);
            } else if ("COLA".equals(tipo)) {
                rutinaGraficaCola(nombre);
            } else if ("LISTA".equals(tipo)) {
                rutinaGraficaLista(nombre);
            } else if ("ARBOL".equals(tipo)) {
                rutinaGraficaArbol(nombre);
                rutinaRecorridosArbol(nombre);
            } else if ("GRAFO".equals(tipo)) {
                rutinaGraficaGrafo(nombre);
            } else if ("HASH".equals(tipo)) {
                rutinaGraficaHash(nombre);
            }
        }
    }

    private void rutinaGraficaPila(String nombre) {
        String loop = nombre + "_gp_loop";
        String fin = nombre + "_gp_fin";
        emitir("GRAFICAR_PILA_" + nombre + " proc");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_top]");
        emitir("    jge " + fin);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 12");
        emitir("    mul bx");
        emitir("    mov dx, 180");
        emitir("    sub dx, ax");
        emitir("    mov cx, 10");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir("GRAFICAR_PILA_" + nombre + " endp");
        emitir("");
    }

    private void rutinaGraficaCola(String nombre) {
        String loop = nombre + "_gc_loop";
        String fin = nombre + "_gc_fin";
        emitir("GRAFICAR_COLA_" + nombre + " proc");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_count]");
        emitir("    jge " + fin);
        emitir("    mov bx, [" + nombre + "_front]");
        emitir("    add bx, ax");
        emitir("    cmp bx, " + estructurasTamano.getOrDefault(nombre, 100));
        emitir("    jl " + nombre + "_gc_idx_ok");
        emitir("    sub bx, " + estructurasTamano.getOrDefault(nombre, 100));
        emitir(nombre + "_gc_idx_ok:");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 38");
        emitir("    mul bx");
        emitir("    mov cx, 70");
        emitir("    add cx, ax");
        emitir("    mov dx, 20");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir("GRAFICAR_COLA_" + nombre + " endp");
        emitir("");
    }

    private void rutinaGraficaLista(String nombre) {
        String loop = nombre + "_gl_loop";
        String fin = nombre + "_gl_fin";
        emitir("GRAFICAR_LISTA_" + nombre + " proc");
        emitir("    mov bx, [" + nombre + "_head]");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov ax, HEAP[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    push bx");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 42");
        emitir("    mul bx");
        emitir("    mov cx, 10");
        emitir("    add cx, ax");
        emitir("    mov dx, 48");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    pop bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir("GRAFICAR_LISTA_" + nombre + " endp");
        emitir("");
    }

    private void rutinaGraficaArbol(String nombre) {
        String rec = "GRAFICAR_ARBOL_REC_" + nombre;
        String fin = nuevaEtiqueta();
        String retorno = nuevaEtiqueta();
        String izqOffsetOk = nuevaEtiqueta();
        String derOffsetOk = nuevaEtiqueta();
        String sinIzq = nuevaEtiqueta();
        String sinDer = nuevaEtiqueta();

        emitir("GRAFICAR_ARBOL_" + nombre + " proc");
        emitir("    mov bx, [" + nombre + "_root]");
        emitir("    mov cx, 150");
        emitir("    mov dx, 78");
        emitir("    mov si, 60");
        emitir("    call " + rec);
        emitir("    ret");
        emitir("GRAFICAR_ARBOL_" + nombre + " endp");
        emitir("");
        emitir(rec + " proc");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    mov ax, HEAP[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    mov si, 28");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("");
        emitir("    ; Hijo izquierdo");
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    cmp ax, 0");
        emitir("    je " + sinIzq);
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    mov ax, si");
        emitir("    shr ax, 1");
        emitir("    sub cx, ax");
        emitir("    add dx, 12");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov al, '/'");
        emitir("    mov ah, 0Eh");
        emitir("    int 10h");
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    mov bx, ax");
        emitir("    sub cx, si");
        emitir("    add dx, 24");
        emitir("    shr si, 1");
        emitir("    cmp si, 8");
        emitir("    jge " + izqOffsetOk);
        emitir("    mov si, 8");
        emitir(izqOffsetOk + ":");
        emitir("    call " + rec);
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir(sinIzq + ":");
        emitir("");
        emitir("    ; Hijo derecho");
        emitir("    mov ax, HEAP[bx+4]");
        emitir("    cmp ax, 0");
        emitir("    je " + sinDer);
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    mov ax, si");
        emitir("    shr ax, 1");
        emitir("    add cx, ax");
        emitir("    add dx, 12");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov al, '\\'");
        emitir("    mov ah, 0Eh");
        emitir("    int 10h");
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    mov bx, ax");
        emitir("    add cx, si");
        emitir("    add dx, 24");
        emitir("    shr si, 1");
        emitir("    cmp si, 8");
        emitir("    jge " + derOffsetOk);
        emitir("    mov si, 8");
        emitir(derOffsetOk + ":");
        emitir("    call " + rec);
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir(sinDer + ":");
        emitir("    jmp " + retorno);
        emitir(fin + ":");
        emitir("    ; Nodo NULL: retorno directo, sin push locales pendientes");
        emitir("    ret");
        emitir(retorno + ":");
        emitir("    ret");
        emitir(rec + " endp");
        emitir("");
    }

    private void rutinaRecorridosArbol(String nombre) {
        rutinaRecorridoPreorden(nombre);
        rutinaRecorridoInorden(nombre);
        rutinaRecorridoPostorden(nombre);
        rutinaRecorridoNiveles(nombre);
    }

    private void emitirImprimirNodoRecorrido() {
        emitir("    mov ax, HEAP[bx]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    call PRINT_ESPACIO_GRAFICO");
    }

    private void rutinaRecorridoPreorden(String nombre) {
        String proc = "RECORRIDO_PREORDEN_" + nombre;
        String fin = nuevaEtiqueta();
        emitir(proc + " proc");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitirImprimirNodoRecorrido();
        emitir("    push bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    call " + proc);
        emitir("    pop bx");
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    call " + proc);
        emitir(fin + ":");
        emitir("    ret");
        emitir(proc + " endp");
        emitir("");
    }

    private void rutinaRecorridoInorden(String nombre) {
        String proc = "RECORRIDO_INORDEN_" + nombre;
        String fin = nuevaEtiqueta();
        emitir(proc + " proc");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    push bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    call " + proc);
        emitir("    pop bx");
        emitirImprimirNodoRecorrido();
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    call " + proc);
        emitir(fin + ":");
        emitir("    ret");
        emitir(proc + " endp");
        emitir("");
    }

    private void rutinaRecorridoPostorden(String nombre) {
        String proc = "RECORRIDO_POSTORDEN_" + nombre;
        String fin = nuevaEtiqueta();
        emitir(proc + " proc");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    push bx");
        emitir("    mov bx, HEAP[bx+2]");
        emitir("    call " + proc);
        emitir("    pop bx");
        emitir("    push bx");
        emitir("    mov bx, HEAP[bx+4]");
        emitir("    call " + proc);
        emitir("    pop bx");
        emitirImprimirNodoRecorrido();
        emitir(fin + ":");
        emitir("    ret");
        emitir(proc + " endp");
        emitir("");
    }

    private void rutinaRecorridoNiveles(String nombre) {
        String proc = "RECORRIDO_NIVELES_" + nombre;
        String loop = nuevaEtiqueta();
        String sinIzq = nuevaEtiqueta();
        String sinDer = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir(proc + " proc");
        emitir("    cmp bx, 0");
        emitir("    je " + fin);
        emitir("    mov word ptr [gfx_q_front], 0");
        emitir("    mov word ptr [gfx_q_rear], 0");
        emitir("    mov si, [gfx_q_rear]");
        emitir("    shl si, 1");
        emitir("    mov gfx_queue[si], bx");
        emitir("    inc word ptr [gfx_q_rear]");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_q_front]");
        emitir("    cmp ax, [gfx_q_rear]");
        emitir("    jge " + fin);
        emitir("    mov si, ax");
        emitir("    shl si, 1");
        emitir("    mov bx, gfx_queue[si]");
        emitir("    inc word ptr [gfx_q_front]");
        emitirImprimirNodoRecorrido();
        emitir("    mov ax, HEAP[bx+2]");
        emitir("    cmp ax, 0");
        emitir("    je " + sinIzq);
        emitir("    mov si, [gfx_q_rear]");
        emitir("    cmp si, 128");
        emitir("    jge " + sinIzq);
        emitir("    shl si, 1");
        emitir("    mov gfx_queue[si], ax");
        emitir("    inc word ptr [gfx_q_rear]");
        emitir(sinIzq + ":");
        emitir("    mov ax, HEAP[bx+4]");
        emitir("    cmp ax, 0");
        emitir("    je " + sinDer);
        emitir("    mov si, [gfx_q_rear]");
        emitir("    cmp si, 128");
        emitir("    jge " + sinDer);
        emitir("    shl si, 1");
        emitir("    mov gfx_queue[si], ax");
        emitir("    inc word ptr [gfx_q_rear]");
        emitir(sinDer + ":");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir(proc + " endp");
        emitir("");
    }

    private void rutinaGraficaGrafo(String nombre) {
        String loopNodos = nombre + "_gg_nodos_loop";
        String sinConector = nombre + "_gg_sin_conector";
        String finNodos = nombre + "_gg_nodos_fin";
        String loopAristas = nombre + "_gg_aristas_loop";
        String finAristas = nombre + "_gg_aristas_fin";
        emitir("GRAFICAR_GRAFO_" + nombre + " proc");
        emitir("    mov cx, 15");
        emitir("    mov dx, 120");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("NODOS: ");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loopNodos + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_node_count]");
        emitir("    jge " + finNodos);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_nodes[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    mov ax, [gfx_i]");
        emitir("    inc ax");
        emitir("    cmp ax, [" + nombre + "_node_count]");
        emitir("    jge " + sinConector);
        emitirTextoGrafico(" -- ");
        emitir(sinConector + ":");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loopNodos);
        emitir(finNodos + ":");
        emitir("");
        emitir("    mov cx, 15");
        emitir("    mov dx, 136");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("ARISTAS:");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loopAristas + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_edge_count]");
        emitir("    jge " + finAristas);
        emitir("    mov bx, 10");
        emitir("    mul bx");
        emitir("    mov dx, 148");
        emitir("    add dx, ax");
        emitir("    mov cx, 15");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov bx, [gfx_i]");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_edges_from[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitirTextoGrafico(" -> ");
        emitir("    mov bx, [gfx_i]");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_edges_to[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    call PRINT_VALOR_CORCHETES");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loopAristas);
        emitir(finAristas + ":");
        emitir("    ret");
        emitir("GRAFICAR_GRAFO_" + nombre + " endp");
        emitir("");
    }

    private void rutinaGraficaHash(String nombre) {
        String loop = nombre + "_gh_loop";
        String fin = nombre + "_gh_fin";
        String finBorde = nombre + "_gh_fin_borde";
        emitir("GRAFICAR_HASH_" + nombre + " proc");
        emitir("    mov cx, 104");
        emitir("    mov dx, 104");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("+--------+-------+-------+");
        emitir("    mov cx, 104");
        emitir("    mov dx, 112");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("| INDICE | CLAVE | VALOR |");
        emitir("    mov cx, 104");
        emitir("    mov dx, 120");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("+--------+-------+-------+");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_count]");
        emitir("    jge " + fin);
        emitir("    cmp ax, 8");
        emitir("    jge " + fin);
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 8");
        emitir("    mul bx");
        emitir("    mov dx, 128");
        emitir("    add dx, ax");
        emitir("    mov cx, 104");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("|        |       |       |");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 8");
        emitir("    mul bx");
        emitir("    mov dx, 128");
        emitir("    add dx, ax");
        emitir("    mov cx, 136");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_i]");
        emitir("    inc ax");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov si, 8");
        emitir("    mul si");
        emitir("    mov dx, 128");
        emitir("    add dx, ax");
        emitir("    mov cx, 200");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov bx, [gfx_i]");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_keys[bx]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov si, 8");
        emitir("    mul si");
        emitir("    mov dx, 128");
        emitir("    add dx, ax");
        emitir("    mov cx, 264");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov bx, [gfx_i]");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_values[bx]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    mov ax, [" + nombre + "_count]");
        emitir("    cmp ax, 8");
        emitir("    jle " + finBorde);
        emitir("    mov ax, 8");
        emitir(finBorde + ":");
        emitir("    mov bx, 8");
        emitir("    mul bx");
        emitir("    mov dx, 128");
        emitir("    add dx, ax");
        emitir("    mov cx, 104");
        emitir("    call SET_CURSOR_PIXEL");
        emitirTextoGrafico("+--------+-------+-------+");
        emitir("    ret");
        emitir("GRAFICAR_HASH_" + nombre + " endp");
        emitir("");
    }

    public String obtenerCodigoEnsamblador() {
        // Construye el ASM final e inserta en .data las variables temporales,
        // estructuras y buffers graficos que se descubrieron durante la traduccion.
        StringBuilder sb = new StringBuilder();
        for (String linea : codigo) {
            if (".code".equals(linea)) {
                if (heapNecesario) {
                    sb.append("    HEAP dw 1000 dup(0)\n");
                    sb.append("    HEAP_PTR dw 2\n");
                }
                sb.append("    gfx_i dw 0\n");
                sb.append("    gfx_valor dw 0\n");
                sb.append("    gfx_busqueda dw 0\n");
                sb.append("    gfx_busqueda_resultado dw 0\n");
                sb.append("    gfx_busqueda_activa dw 0\n");
                sb.append("    gfx_ultimo_desapilado dw 0\n");
                sb.append("    gfx_color db 0Fh\n");
                if (colaNivelesNecesaria) {
                    sb.append("    gfx_queue dw 128 dup(0)\n");
                    sb.append("    gfx_q_front dw 0\n");
                    sb.append("    gfx_q_rear dw 0\n");
                }
                for (Map.Entry<String, String> entry : estructurasTipo.entrySet()) {
                    String n = entry.getKey();
                    String t = entry.getValue();
                    int cap = estructurasTamano.getOrDefault(n, 100);
                    if ("PILA".equals(t)) {
                        sb.append("    ").append(n).append(" dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_top dw 0\n");
                    } else if ("COLA".equals(t)) {
                        sb.append("    ").append(n).append(" dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_front dw 0\n");
                        sb.append("    ").append(n).append("_rear dw 0\n");
                        sb.append("    ").append(n).append("_count dw 0\n");
                    } else if ("LISTA".equals(t)) {
                        sb.append("    ").append(n).append("_head dw 0\n");
                        sb.append("    ").append(n).append("_tail dw 0\n");
                        sb.append("    ").append(n).append("_count dw 0\n");
                    } else if ("ARBOL".equals(t)) {
                        sb.append("    ").append(n).append("_root dw 0\n");
                    } else if ("GRAFO".equals(t)) {
                        sb.append("    ").append(n).append("_nodes dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_node_count dw 0\n");
                        sb.append("    ").append(n).append("_edges_from dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_edges_to dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_edge_count dw 0\n");
                    } else if ("HASH".equals(t)) {
                        sb.append("    ").append(n).append("_keys dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_values dw ").append(cap).append(" dup(0)\n");
                        sb.append("    ").append(n).append("_count dw 0\n");
                    }
                }
                for (String v : variables) {
                    if (!estructurasTipo.containsKey(v)) {
                        sb.append("    ").append(v).append(" dw 0\n");
                    }
                }
                sb.append("\n");
            }
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }

    public String generarEsqueletoGraficoPila() {
        procesarCuadruplos(new ArrayList<Cuadruplo>());
        return obtenerCodigoEnsamblador();
    }

    private void cargarAX(String valor) {
        if (valor == null || valor.isEmpty()) {
            emitir("    mov ax, 0");
        } else if (esNumero(valor)) {
            emitir("    mov ax, " + valor);
        } else {
            registrarVariable(valor);
            emitir("    mov ax, [" + valor + "]");
        }
    }

    private void cargarBX(String valor) {
        if (valor == null || valor.isEmpty()) {
            emitir("    mov bx, 0");
        } else if (esNumero(valor)) {
            emitir("    mov bx, " + valor);
        } else {
            registrarVariable(valor);
            emitir("    mov bx, [" + valor + "]");
        }
    }

    private void emitirOperacionConAX(String instruccion, String valor) {
        if (valor == null || valor.isEmpty()) {
            emitir("    " + instruccion + " ax, 0");
        } else if (esNumero(valor)) {
            emitir("    " + instruccion + " ax, " + valor);
        } else {
            registrarVariable(valor);
            emitir("    " + instruccion + " ax, [" + valor + "]");
        }
    }

    private void compararAXCon(String valor) {
        if (valor == null || valor.isEmpty()) {
            emitir("    cmp ax, 0");
        } else if (esNumero(valor)) {
            emitir("    cmp ax, " + valor);
        } else {
            registrarVariable(valor);
            emitir("    cmp ax, [" + valor + "]");
        }
    }

    private String saltoComparacion(String op) {
        switch (op) {
            case "<":
                return "jl";
            case ">":
                return "jg";
            case "==":
                return "je";
            case "!=":
                return "jne";
            case "<=":
                return "jle";
            case ">=":
                return "jge";
            default:
                return "je";
        }
    }

    private String contadorEstructura(String estructura, String tipo) {
        if (tipo == null) {
            return null;
        }
        if ("PILA".equals(tipo)) {
            return estructura + "_top";
        }
        if ("COLA".equals(tipo) || "LISTA".equals(tipo) || "HASH".equals(tipo)) {
            return estructura + "_count";
        }
        if ("GRAFO".equals(tipo)) {
            return estructura + "_node_count";
        }
        return null;
    }

    private void registrarVariable(String nombre) {
        if (nombre != null && !nombre.isEmpty() && !esNumero(nombre) && !estructurasTipo.containsKey(nombre)) {
            variables.add(nombre);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarTipo(String tipo) {
        String t = tipo == null ? "" : tipo.toUpperCase();
        if (t.contains("PILA")) {
            return "PILA";
        }
        if (t.contains("COLA")) {
            return "COLA";
        }
        if (t.contains("LISTA")) {
            return "LISTA";
        }
        if (t.contains("ARBOL")) {
            return "ARBOL";
        }
        if (t.contains("GRAFO")) {
            return "GRAFO";
        }
        if (t.contains("HASH")) {
            return "HASH";
        }
        return t.isEmpty() ? "VAR" : t;
    }

    private boolean esNumero(String valor) {
        return valor != null && valor.matches("-?\\d+");
    }

    private int parseInt(String valor, int defecto) {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return defecto;
        }
    }

    private String nuevaEtiqueta() {
        return "GFX_L" + contadorEtiquetas++;
    }

    private void emitirTextoGrafico(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            emitir("    mov al, '" + escaparCaracterAsm(c) + "'");
            emitir("    mov ah, 0Eh");
            emitir("    mov bl, [gfx_color]");
            emitir("    int 10h");
        }
    }
    private String escaparCaracterAsm(char c) {
        return c == '\'' ? "''" : Character.toString(c);
    }
    private void emitir(String linea) {
        // Punto unico para agregar lineas ASM al programa grafico.
        codigo.add(linea);
    }
}
