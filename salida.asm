; ============================================
; CÓDIGO ENSAMBLADOR GENERADO - DSL
; Con Visualización de Estructuras de Datos
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


; ============================================
; RUTINAS DE VISUALIZACIÓN DE ÁRBOLES
; ============================================

dibujar_arbol:
    push rbp
    mov rbp, rsp
    
    ; Inicializar visualización
    mov rdi, newline
    call printf
    
    ; Llamar a función recursiva de dibujo
    mov rax, [raiz_arbol]
    mov rcx, 0  ; profundidad inicial
    call dibujar_nodo_rec
    
    mov rdi, newline
    call printf
    pop rbp
    ret

dibujar_nodo_rec:
    push rbp
    mov rbp, rsp
    
    ; rax = puntero al nodo
    ; rcx = profundidad
    
    ; Si nodo es NULL, retornar
    test rax, rax
    jz .fin_nodo
    
    ; Imprimir indentación (espacios)
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
    
    ; Imprimir nodo actual
    mov rdi, formato_nodo
    mov rsi, [rax + 0]   ; clave
    mov rdx, [rax + 8]   ; valor
    call printf
    
    ; Recursión izquierda
    mov rbx, [rax + 16]  ; hijo izquierdo
    mov rax, rbx
    inc rcx
    call dibujar_nodo_rec
    dec rcx
    
    ; Recursión derecha (del nodo original)
    mov rax, [rbp + 16]  ; recuperar nodo original
    mov rbx, [rax + 24]  ; hijo derecho
    mov rax, rbx
    inc rcx
    call dibujar_nodo_rec
    
.fin_nodo:
    pop rbp
    ret

section .data
    formato_nodo db '[%d:%d] ', 0
    formato_error db 'ERROR: Estructura vacia', 10, 0
    msg_error db 'Error en la operacion', 10, 0

; ============================================
; FIN DEL PROGRAMA
; ============================================

    mov rax, 60        ; exit syscall
    mov rdi, 0         ; código de salida
    syscall

