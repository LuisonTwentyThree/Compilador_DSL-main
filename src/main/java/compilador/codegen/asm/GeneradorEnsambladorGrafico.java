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
 * No reemplaza a GeneradorEnsamblador: es una salida paralela que usa los
 * mismos cuadruplos, pero inicializa modo 13h y redibuja las estructuras del DSL
 * como rectangulos/nodos usando BIOS int 10h.
 */
public class GeneradorEnsambladorGrafico {
    private final List<String> codigo;
    private final Set<String> variables;
    private final Map<String, String> estructurasTipo;
    private final Map<String, Integer> estructurasTamano;
    private boolean heapNecesario;
    private int contadorEtiquetas;

    public GeneradorEnsambladorGrafico() {
        this.codigo = new ArrayList<>();
        this.variables = new LinkedHashSet<>();
        this.estructurasTipo = new LinkedHashMap<>();
        this.estructurasTamano = new LinkedHashMap<>();
        this.heapNecesario = false;
        this.contadorEtiquetas = 1;
        agregarEncabezado();
    }

    private void agregarEncabezado() {
        emitir("; ============================================");
        emitir("; CODIGO ENSAMBLADOR GRAFICO GENERADO - DSL");
        emitir("; Intel 8086 / EMU8086 / MASM - Modo 13h");
        emitir("; ============================================");
        emitir("");
        emitir(".model small");
        emitir(".stack 100h");
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
            case "=":
                registrarVariable(res);
                cargarAX(arg1);
                emitir("    mov [" + res + "], ax");
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
            default:
                emitir("    ; Operacion grafica pendiente: " + op + " " + arg1 + " " + arg2 + " -> " + res);
                break;
        }
    }

    public void procesarCuadruplos(List<Cuadruplo> cuadruplos) {
        if (cuadruplos != null) {
            for (Cuadruplo c : cuadruplos) {
                traducirCuadruplo(c);
            }
        }
        finalizarPrograma();
    }

    private void traducirAlloc(String tamano, String tipo, String nombre) {
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
            insertarLista(arg2.isEmpty() ? arg1 : arg2, estructura);
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
        }
    }

    private void traducirEliminacion(String op, String estructura) {
        String tipo = estructurasTipo.get(estructura);
        if ("PILA".equals(tipo) && ("DESAPILAR".equals(op) || "POP".equals(op))) {
            String fin = nuevaEtiqueta();
            emitir("    ; DESAPILAR EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_top], 0");
            emitir("    je " + fin);
            emitir("    dec word ptr [" + estructura + "_top]");
            emitir("    mov bx, [" + estructura + "_top]");
            emitir("    shl bx, 1");
            emitir("    mov word ptr " + estructura + "[bx], 0");
            emitir(fin + ":");
            emitir("    call GRAFICAR_TODO");
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
            String fin = nuevaEtiqueta();
            emitir("    ; " + op + " EN " + estructura);
            emitir("    cmp word ptr [" + estructura + "_head], 0");
            emitir("    je " + fin);
            emitir("    mov bx, [" + estructura + "_head]");
            emitir("    mov ax, HEAP[bx+2]");
            emitir("    mov [" + estructura + "_head], ax");
            emitir("    cmp ax, 0");
            emitir("    jne " + fin);
            emitir("    mov word ptr [" + estructura + "_tail], 0");
            emitir(fin + ":");
            emitir("    call GRAFICAR_TODO");
        }
    }

    private void insertarLista(String valor, String lista) {
        String append = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        heapNecesario = true;
        emitir("    ; INSERTAR_FINAL " + valor + " EN " + lista);
        cargarAX(valor);
        emitir("    mov si, [HEAP_PTR]");
        emitir("    add word ptr [HEAP_PTR], 4");
        emitir("    mov HEAP[si], ax");
        emitir("    mov word ptr HEAP[si+2], 0");
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

    private void traducirPrint(String valor) {
        emitir("    ; MOSTRAR " + valor + " en modo grafico");
        cargarAX(valor);
        emitir("    call PRINT_NUM_GRAFICO");
    }

    private void finalizarPrograma() {
        emitir("    call GRAFICAR_TODO");
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
        emitir("; ============================================");
        emitir("; RUTINAS GRAFICAS");
        emitir("; ============================================");
        emitir("");
        agregarRutinasBase();
        agregarGraficarTodo();
        agregarRutinasPorEstructura();
    }

    private void agregarRutinasBase() {
        emitir("DIBUJAR_PIXEL proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    mov ah, 0Ch");
        emitir("    mov bh, 00h");
        emitir("    int 10h");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("DIBUJAR_PIXEL endp");
        emitir("");
        emitir("DIBUJAR_RECTANGULO proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    push si");
        emitir("    push di");
        emitir("    mov [rect_x], cx");
        emitir("    mov [rect_y], dx");
        emitir("    mov [rect_w], si");
        emitir("    mov [rect_h], di");
        emitir("    mov [rect_color], al");
        emitir("dr_fila:");
        emitir("    mov cx, [rect_x]");
        emitir("    mov si, [rect_w]");
        emitir("dr_columna:");
        emitir("    mov dx, [rect_y]");
        emitir("    mov al, [rect_color]");
        emitir("    call DIBUJAR_PIXEL");
        emitir("    inc cx");
        emitir("    dec si");
        emitir("    jnz dr_columna");
        emitir("    inc word ptr [rect_y]");
        emitir("    dec word ptr [rect_h]");
        emitir("    jnz dr_fila");
        emitir("    pop di");
        emitir("    pop si");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
        emitir("    pop ax");
        emitir("    ret");
        emitir("DIBUJAR_RECTANGULO endp");
        emitir("");
        emitir("LIMPIAR_PANTALLA proc");
        emitir("    push ax");
        emitir("    push bx");
        emitir("    push cx");
        emitir("    push dx");
        emitir("    mov ax, 0600h");
        emitir("    mov bh, 00h");
        emitir("    mov cx, 0000h");
        emitir("    mov dx, 1827h");
        emitir("    int 10h");
        emitir("    pop dx");
        emitir("    pop cx");
        emitir("    pop bx");
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
    }

    private void agregarGraficarTodo() {
        emitir("GRAFICAR_TODO proc");
        emitir("    call LIMPIAR_PANTALLA");
        for (Map.Entry<String, String> e : estructurasTipo.entrySet()) {
            emitir("    call GRAFICAR_" + e.getValue() + "_" + e.getKey());
        }
        emitir("    ret");
        emitir("GRAFICAR_TODO endp");
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
        emitir("    mov si, 42");
        emitir("    mov di, 10");
        emitir("    mov al, 0Ah");
        emitir("    call DIBUJAR_RECTANGULO");
        emitir("    mov cx, 22");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
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
        emitir("    mov si, 34");
        emitir("    mov di, 12");
        emitir("    mov al, 0Bh");
        emitir("    call DIBUJAR_RECTANGULO");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
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
        emitir("    mov si, 34");
        emitir("    mov di, 12");
        emitir("    mov al, 0Eh");
        emitir("    call DIBUJAR_RECTANGULO");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
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
        String fin = "GRAFICAR_ARBOL_FIN_" + nombre;
        String retorno = "GRAFICAR_ARBOL_RET_" + nombre;
        String izqOffsetOk = "GRAFICAR_ARBOL_IZQ_OFFSET_OK_" + nombre;
        String derOffsetOk = "GRAFICAR_ARBOL_DER_OFFSET_OK_" + nombre;
        String sinIzq = "GRAFICAR_ARBOL_SIN_IZQ_" + nombre;
        String sinDer = "GRAFICAR_ARBOL_SIN_DER_" + nombre;

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
        emitir("    mov di, 12");
        emitir("    mov al, 0Ch");
        emitir("    call DIBUJAR_RECTANGULO");
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

    private void rutinaGraficaGrafo(String nombre) {
        String loop = nombre + "_gg_loop";
        String fin = nombre + "_gg_fin";
        emitir("GRAFICAR_GRAFO_" + nombre + " proc");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_node_count]");
        emitir("    jge " + fin);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_nodes[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 45");
        emitir("    mul bx");
        emitir("    mov cx, 15");
        emitir("    add cx, ax");
        emitir("    mov dx, 120");
        emitir("    mov si, 26");
        emitir("    mov di, 14");
        emitir("    mov al, 09h");
        emitir("    call DIBUJAR_RECTANGULO");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir("GRAFICAR_GRAFO_" + nombre + " endp");
        emitir("");
    }

    private void rutinaGraficaHash(String nombre) {
        String loop = nombre + "_gh_loop";
        String fin = nombre + "_gh_fin";
        emitir("GRAFICAR_HASH_" + nombre + " proc");
        emitir("    mov word ptr [gfx_i], 0");
        emitir(loop + ":");
        emitir("    mov ax, [gfx_i]");
        emitir("    cmp ax, [" + nombre + "_count]");
        emitir("    jge " + fin);
        emitir("    mov bx, ax");
        emitir("    shl bx, 1");
        emitir("    mov ax, " + nombre + "_values[bx]");
        emitir("    mov [gfx_valor], ax");
        emitir("    mov ax, [gfx_i]");
        emitir("    mov bx, 18");
        emitir("    mul bx");
        emitir("    mov dx, 150");
        emitir("    add dx, ax");
        emitir("    mov cx, 245");
        emitir("    mov si, 44");
        emitir("    mov di, 14");
        emitir("    mov al, 0Dh");
        emitir("    call DIBUJAR_RECTANGULO");
        emitir("    call SET_CURSOR_PIXEL");
        emitir("    mov ax, [gfx_valor]");
        emitir("    call PRINT_NUM_GRAFICO");
        emitir("    inc word ptr [gfx_i]");
        emitir("    jmp " + loop);
        emitir(fin + ":");
        emitir("    ret");
        emitir("GRAFICAR_HASH_" + nombre + " endp");
        emitir("");
    }

    public String obtenerCodigoEnsamblador() {
        StringBuilder sb = new StringBuilder();
        for (String linea : codigo) {
            if (".code".equals(linea)) {
                if (heapNecesario) {
                    sb.append("    HEAP dw 1000 dup(0)\n");
                    sb.append("    HEAP_PTR dw 2\n");
                }
                sb.append("    gfx_i dw 0\n");
                sb.append("    gfx_valor dw 0\n");
                sb.append("    rect_x dw 0\n");
                sb.append("    rect_y dw 0\n");
                sb.append("    rect_w dw 0\n");
                sb.append("    rect_h dw 0\n");
                sb.append("    rect_color db 0\n");
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

    private void emitir(String linea) {
        codigo.add(linea);
    }

    public static void main(String[] args) {
        GeneradorEnsambladorGrafico g = new GeneradorEnsambladorGrafico();
        List<Cuadruplo> demo = new ArrayList<>();
        demo.add(new Cuadruplo("ALLOC", "100", "PILA", "miPila"));
        demo.add(new Cuadruplo("APILAR", "10", "", "miPila"));
        demo.add(new Cuadruplo("APILAR", "20", "", "miPila"));
        demo.add(new Cuadruplo("ALLOC", "50", "COLA", "miCola"));
        demo.add(new Cuadruplo("ENCOLAR", "7", "", "miCola"));
        demo.add(new Cuadruplo("ALLOC", "100", "LISTA_ENLAZADA", "miLista"));
        demo.add(new Cuadruplo("INSERTAR_FINAL", "5", "", "miLista"));
        demo.add(new Cuadruplo("ALLOC", "100", "ARBOL_BINARIO", "miArbol"));
        demo.add(new Cuadruplo("AGREGARNODO", "1", "25", "miArbol"));
        demo.add(new Cuadruplo("ALLOC", "30", "GRAFO", "miGrafo"));
        demo.add(new Cuadruplo("AGREGARNODO", "1", "", "miGrafo"));
        demo.add(new Cuadruplo("ALLOC", "30", "TABLA_HASH", "miHash"));
        demo.add(new Cuadruplo("INSERTAR", "101", "1000", "miHash"));
        g.procesarCuadruplos(demo);
        System.out.println(g.obtenerCodigoEnsamblador());
    }
}
