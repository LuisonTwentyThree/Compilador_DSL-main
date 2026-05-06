; ============================================
; CÓDIGO ENSAMBLADOR GENERADO - DSL
; Con Optimización y Visualización de Estructuras
; Arquitectura: x86-64 (System V AMD64 ABI)
; ============================================

section .data
    titulo db 'EJECUCION DE PROGRAMA DSL', 0
    newline db 10, 0
    espacio db ' ', 0
    arbolVacio db 'Arbol vacio', 10, 0

section .text
    global main
    extern printf

main:
    push rbp
    mov rsp, rbp

    ; *** ASIGNACIÓN: a = 5
    mov [a], 5
    ; *** ASIGNACIÓN: b = 3
    mov [b], 3
    ; *** OPERACIÓN MATEMÁTICA: c = a + b
    mov rax, [a]  ; Cargar primer operando
    mov rbx, [b]  ; Cargar segundo operando
    add rax, rbx            ; Suma
    mov [c], rax      ; Guardar resultado
    ; *** PRINT: c
    mov rdi, [c]
    call printf
    ; *** IF_FALSE c GOTO fin
    cmp [c], 0   ; Comparar condición con 0
    je fin          ; Saltar si es cero (falso)
    ; *** APILAR c EN pila1
    mov rax, [c]
    call apilar_pila1
fin:
    jmp salida

; ============================================
; SECCIÓN: RUTINAS DE VISUALIZACIÓN DE ÁRBOLES
; ============================================

dibujar_arbol:
    push rbp
    mov rbp, rsp
    
    ; Inicializar visualización
    mov rdi, newline
    call printf
    
    ; Llamar a función recursiva de dibujo
    mov rax, [raiz_arbol]
    mov rcx, 0              ; profundidad inicial = 0
    call dibujar_nodo_rec
    
    mov rdi, newline
    call printf
    pop rbp
    ret

dibujar_nodo_rec:
    push rbp
    mov rbp, rsp
    
    ; PARÁMETROS:
    ;   RAX = puntero al nodo actual
    ;   RCX = profundidad (indentación)
    
    ; BASE: Si nodo es NULL, retornar
    test rax, rax
    jz .fin_nodo
    
    ; Imprimir indentación (espacios según profundidad)
    push rcx
    mov r8, rcx
.loop_indent:
    test r8, r8
    jz .fin_indent
    mov rdi, espacio
    call printf
    dec r8
    jmp .loop_indent
.fin_indent:
    pop rcx
    
    ; Imprimir nodo actual [clave:valor]
    mov rdi, formato_nodo
    mov rsi, [rax + 0]      ; campo: clave
    mov rdx, [rax + 8]      ; campo: valor
    call printf
    
    ; RECURSIÓN IZQUIERDA
    mov rax, [rax + 16]     ; carga hijo izquierdo
    inc rcx                 ; aumentar profundidad
    call dibujar_nodo_rec
    dec rcx
    
    ; RECURSIÓN DERECHA
    mov rax, [rbp + 16]     ; recuperar nodo original del stack
    mov rax, [rax + 24]     ; carga hijo derecho
    inc rcx
    call dibujar_nodo_rec
    
.fin_nodo:
    pop rbp
    ret

section .data
    formato_nodo db '[%d:%d] ', 0     ; Formato para nodo del árbol
    formato_error db 'ERROR: Estructura vacia', 10, 0
    msg_error db 'Error en la operacion', 10, 0

; ============================================
; SECCIÓN: FIN DEL PROGRAMA
; ============================================

    mov rax, 60             ; Número syscall para exit
    mov rdi, 0              ; Código de salida (0 = éxito)
    syscall                 ; Realizar syscall
