; ============================================
; CODIGO ENSAMBLADOR GENERADO - DSL
; Arquitectura objetivo: Intel 8086 / DOS
; ============================================

.model small
.stack 100h

.data
    titulo db 'EJECUCION DE PROGRAMA DSL', 0Dh, 0Ah, '$'
    newline db 0Dh, 0Ah, '$'
    msg_error db 'Error en la operacion', 0Dh, 0Ah, '$'

    HEAP dw 1000 dup(0)
    HEAP_PTR dw 2
    miPila dw 100 dup(0)
    miPila_top dw 0
    miCola dw 50 dup(0)
    miCola_front dw 0
    miCola_rear dw 0
    miCola_count dw 0
    miLista_head dw 0
    miLista_tail dw 0
    T1 dw 0
    T2 dw 0
    T3 dw 0
    T4 dw 0
    T5 dw 0
    T6 dw 0
    T7 dw 0
    T8 dw 0
    T9 dw 0

.code
main proc
    mov ax, @data
    mov ds, ax
    mov dx, offset titulo
    call print_string

    ; CREAR PILA miPila TAMANO 100
    ; APILAR 10 EN miPila
    mov ax, 10
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
    ; APILAR 20 EN miPila
    mov ax, 20
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
    ; APILAR 30 EN miPila
    mov ax, 30
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
    ; TOPE EN miPila
    cmp word ptr [miPila_top], 0
    je ASM_L1
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [T1], ax
    jmp ASM_L2
ASM_L1:
    mov word ptr [T1], 0
ASM_L2:
    ; PRINT T1
    mov ax, [T1]
    call print_num
    mov dx, offset newline
    call print_string
    ; VACIA
    mov word ptr [T2], 0
    cmp word ptr [miPila_top], 0
    je ASM_L3
    jmp ASM_L4
ASM_L3:
    mov word ptr [T2], 1
ASM_L4:
    ; IF_FALSE T2 GOTO L1
    mov ax, [T2]
    cmp ax, 0
    je L1
    ; ERROR: Estructura vacía 
    mov dx, offset msg_error
    call print_string
L1:
    ; DESAPILAR EN miPila
    cmp word ptr [miPila_top], 0
    je ASM_L5
    dec word ptr [miPila_top]
    mov bx, [miPila_top]
    shl bx, 1
    mov word ptr miPila[bx], 0
ASM_L5:
    ; TOPE EN miPila
    cmp word ptr [miPila_top], 0
    je ASM_L6
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [T3], ax
    jmp ASM_L7
ASM_L6:
    mov word ptr [T3], 0
ASM_L7:
    ; PRINT T3
    mov ax, [T3]
    call print_num
    mov dx, offset newline
    call print_string
    ; VACIA
    mov word ptr [T4], 0
    cmp word ptr [miPila_top], 0
    je ASM_L8
    jmp ASM_L9
ASM_L8:
    mov word ptr [T4], 1
ASM_L9:
    ; IF_FALSE T4 GOTO L2
    mov ax, [T4]
    cmp ax, 0
    je L2
    ; ERROR: Estructura vacía 
    mov dx, offset msg_error
    call print_string
L2:
    ; DESAPILAR EN miPila
    cmp word ptr [miPila_top], 0
    je ASM_L10
    dec word ptr [miPila_top]
    mov bx, [miPila_top]
    shl bx, 1
    mov word ptr miPila[bx], 0
ASM_L10:
    ; TOPE EN miPila
    cmp word ptr [miPila_top], 0
    je ASM_L11
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [T5], ax
    jmp ASM_L12
ASM_L11:
    mov word ptr [T5], 0
ASM_L12:
    ; PRINT T5
    mov ax, [T5]
    call print_num
    mov dx, offset newline
    call print_string
    ; CREAR COLA miCola TAMANO 50
    ; ENCOLAR 100 EN miCola
    cmp word ptr [miCola_count], 50
    jge ASM_L14
    mov ax, 100
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl ASM_L13
    mov word ptr [miCola_rear], 0
ASM_L13:
    inc word ptr [miCola_count]
ASM_L14:
    ; ENCOLAR 200 EN miCola
    cmp word ptr [miCola_count], 50
    jge ASM_L16
    mov ax, 200
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl ASM_L15
    mov word ptr [miCola_rear], 0
ASM_L15:
    inc word ptr [miCola_count]
ASM_L16:
    ; ENCOLAR 300 EN miCola
    cmp word ptr [miCola_count], 50
    jge ASM_L18
    mov ax, 300
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl ASM_L17
    mov word ptr [miCola_rear], 0
ASM_L17:
    inc word ptr [miCola_count]
ASM_L18:
    ; FRENTE EN miCola
    cmp word ptr [miCola_count], 0
    je ASM_L19
    mov bx, [miCola_front]
    shl bx, 1
    mov ax, miCola[bx]
    mov [T6], ax
    jmp ASM_L20
ASM_L19:
    mov word ptr [T6], 0
ASM_L20:
    ; PRINT T6
    mov ax, [T6]
    call print_num
    mov dx, offset newline
    call print_string
    ; VACIA
    mov word ptr [T7], 0
    cmp word ptr [miCola_count], 0
    je ASM_L21
    jmp ASM_L22
ASM_L21:
    mov word ptr [T7], 1
ASM_L22:
    ; IF_FALSE T7 GOTO L3
    mov ax, [T7]
    cmp ax, 0
    je L3
    ; ERROR: Estructura vacía 
    mov dx, offset msg_error
    call print_string
L3:
    ; DESENCOLAR EN miCola
    cmp word ptr [miCola_count], 0
    je ASM_L24
    mov bx, [miCola_front]
    shl bx, 1
    mov word ptr miCola[bx], 0
    inc word ptr [miCola_front]
    cmp word ptr [miCola_front], 50
    jl ASM_L23
    mov word ptr [miCola_front], 0
ASM_L23:
    dec word ptr [miCola_count]
ASM_L24:
    ; FRENTE EN miCola
    cmp word ptr [miCola_count], 0
    je ASM_L25
    mov bx, [miCola_front]
    shl bx, 1
    mov ax, miCola[bx]
    mov [T8], ax
    jmp ASM_L26
ASM_L25:
    mov word ptr [T8], 0
ASM_L26:
    ; PRINT T8
    mov ax, [T8]
    call print_num
    mov dx, offset newline
    call print_string
    ; CREAR LISTA miLista
    ; INSERTAR_FINAL 5 EN miLista (nodo HEAP: valor, sig)
    mov ax, 5
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne ASM_L27
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp ASM_L28
ASM_L27:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
ASM_L28:
    ; INSERTAR_FINAL 10 EN miLista (nodo HEAP: valor, sig)
    mov ax, 10
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne ASM_L29
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp ASM_L30
ASM_L29:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
ASM_L30:
    ; INSERTAR_FINAL 15 EN miLista (nodo HEAP: valor, sig)
    mov ax, 15
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne ASM_L31
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp ASM_L32
ASM_L31:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
ASM_L32:
    ; INSERTAR_INICIO 1 EN miLista (nodo HEAP: valor, sig)
    mov ax, 1
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne ASM_L33
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp ASM_L34
ASM_L33:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
ASM_L34:
    ; TAMANO EN miLista recorriendo punteros HEAP
    mov bx, [miLista_head]
    xor cx, cx
    mov word ptr [T9], 0
ASM_L35:
    cmp bx, 0
    je ASM_L36
    inc cx
    mov ax, HEAP[bx]
    mov [T9], ax
    mov bx, HEAP[bx+2]
    jmp ASM_L35
ASM_L36:
    mov [T9], cx
    ; PRINT T9
    mov ax, [T9]
    call print_num
    mov dx, offset newline
    call print_string
    ; PRINT 999
    mov ax, 999
    call print_num
    mov dx, offset newline
    call print_string

; ============================================
; FIN DEL PROGRAMA
; ============================================
    mov ax, 4C00h
    int 21h

main endp

; ============================================
; RUTINAS AUXILIARES
; ============================================

print_string proc
    mov ah, 09h
    int 21h
    ret
print_string endp

print_num proc
    push ax
    push bx
    push cx
    push dx
    cmp ax, 0
    jne pn_convert
    mov dl, '0'
    mov ah, 02h
    int 21h
    jmp pn_done
pn_convert:
    xor cx, cx
    mov bx, 10
pn_loop:
    xor dx, dx
    div bx
    push dx
    inc cx
    cmp ax, 0
    jne pn_loop
pn_print:
    pop dx
    add dl, '0'
    mov ah, 02h
    int 21h
    loop pn_print
pn_done:
    pop dx
    pop cx
    pop bx
    pop ax
    ret
print_num endp

end main

