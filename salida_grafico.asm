; ============================================
; CODIGO ENSAMBLADOR GRAFICO GENERADO - DSL
; Intel 8086 / EMU8086 / MASM - Modo 13h
; ============================================

.model small
.stack 100h

.data
    titulo db 'DSL - VISUALIZACION GRAFICA', 0

    HEAP dw 1000 dup(0)
    HEAP_PTR dw 2
    gfx_i dw 0
    gfx_valor dw 0
    rect_x dw 0
    rect_y dw 0
    rect_w dw 0
    rect_h dw 0
    rect_color db 0
    miPila dw 100 dup(0)
    miPila_top dw 0
    miCola dw 50 dup(0)
    miCola_front dw 0
    miCola_rear dw 0
    miCola_count dw 0
    miLista_head dw 0
    miLista_tail dw 0
    T1 dw 0
    T3 dw 0
    T5 dw 0
    T6 dw 0
    T8 dw 0
    T9 dw 0

.code
main proc
    mov ax, @data
    mov ds, ax

    ; Modo grafico 13h: 320x200, 256 colores
    mov ax, 0013h
    int 10h

    ; CREAR PILA miPila TAMANO 100
    call GRAFICAR_TODO
    ; APILAR 10 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L1
    mov ax, 10
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L1:
    call GRAFICAR_TODO
    ; APILAR 20 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L2
    mov ax, 20
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L2:
    call GRAFICAR_TODO
    ; APILAR 30 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L3
    mov ax, 30
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L3:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: TOPE miPila  -> T1
    ; MOSTRAR T1 en modo grafico
    mov ax, [T1]
    call PRINT_NUM_GRAFICO
    ; Operacion grafica pendiente: VACIA miPila  -> T2
    ; Operacion grafica pendiente: IF_FALSE T2 GOTO -> L1
    ; Operacion grafica pendiente: ERROR Estructura vacía  -> 
    ; Operacion grafica pendiente: ETIQUETA   -> L1
    ; DESAPILAR EN miPila
    cmp word ptr [miPila_top], 0
    je GFX_L4
    dec word ptr [miPila_top]
    mov bx, [miPila_top]
    shl bx, 1
    mov word ptr miPila[bx], 0
GFX_L4:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: TOPE miPila  -> T3
    ; MOSTRAR T3 en modo grafico
    mov ax, [T3]
    call PRINT_NUM_GRAFICO
    ; Operacion grafica pendiente: VACIA miPila  -> T4
    ; Operacion grafica pendiente: IF_FALSE T4 GOTO -> L2
    ; Operacion grafica pendiente: ERROR Estructura vacía  -> 
    ; Operacion grafica pendiente: ETIQUETA   -> L2
    ; DESAPILAR EN miPila
    cmp word ptr [miPila_top], 0
    je GFX_L5
    dec word ptr [miPila_top]
    mov bx, [miPila_top]
    shl bx, 1
    mov word ptr miPila[bx], 0
GFX_L5:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: TOPE miPila  -> T5
    ; MOSTRAR T5 en modo grafico
    mov ax, [T5]
    call PRINT_NUM_GRAFICO
    ; CREAR COLA miCola TAMANO 50
    call GRAFICAR_TODO
    ; ENCOLAR 100 EN miCola
    cmp word ptr [miCola_count], 50
    jge GFX_L7
    mov ax, 100
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl GFX_L6
    mov word ptr [miCola_rear], 0
GFX_L6:
    inc word ptr [miCola_count]
GFX_L7:
    call GRAFICAR_TODO
    ; ENCOLAR 200 EN miCola
    cmp word ptr [miCola_count], 50
    jge GFX_L9
    mov ax, 200
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl GFX_L8
    mov word ptr [miCola_rear], 0
GFX_L8:
    inc word ptr [miCola_count]
GFX_L9:
    call GRAFICAR_TODO
    ; ENCOLAR 300 EN miCola
    cmp word ptr [miCola_count], 50
    jge GFX_L11
    mov ax, 300
    mov bx, [miCola_rear]
    shl bx, 1
    mov miCola[bx], ax
    inc word ptr [miCola_rear]
    cmp word ptr [miCola_rear], 50
    jl GFX_L10
    mov word ptr [miCola_rear], 0
GFX_L10:
    inc word ptr [miCola_count]
GFX_L11:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: FRENTE miCola  -> T6
    ; MOSTRAR T6 en modo grafico
    mov ax, [T6]
    call PRINT_NUM_GRAFICO
    ; Operacion grafica pendiente: VACIA miCola  -> T7
    ; Operacion grafica pendiente: IF_FALSE T7 GOTO -> L3
    ; Operacion grafica pendiente: ERROR Estructura vacía  -> 
    ; Operacion grafica pendiente: ETIQUETA   -> L3
    ; DESENCOLAR EN miCola
    cmp word ptr [miCola_count], 0
    je GFX_L13
    mov bx, [miCola_front]
    shl bx, 1
    mov word ptr miCola[bx], 0
    inc word ptr [miCola_front]
    cmp word ptr [miCola_front], 50
    jl GFX_L12
    mov word ptr [miCola_front], 0
GFX_L12:
    dec word ptr [miCola_count]
GFX_L13:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: FRENTE miCola  -> T8
    ; MOSTRAR T8 en modo grafico
    mov ax, [T8]
    call PRINT_NUM_GRAFICO
    ; CREAR LISTA miLista TAMANO 100
    call GRAFICAR_TODO
    ; INSERTAR_FINAL 5 EN miLista
    mov ax, 5
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne GFX_L14
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp GFX_L15
GFX_L14:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
GFX_L15:
    call GRAFICAR_TODO
    ; INSERTAR_FINAL 10 EN miLista
    mov ax, 10
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne GFX_L16
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp GFX_L17
GFX_L16:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
GFX_L17:
    call GRAFICAR_TODO
    ; INSERTAR_FINAL 15 EN miLista
    mov ax, 15
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne GFX_L18
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp GFX_L19
GFX_L18:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
GFX_L19:
    call GRAFICAR_TODO
    ; INSERTAR_FINAL 1 EN miLista
    mov ax, 1
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [miLista_head], 0
    jne GFX_L20
    mov [miLista_head], si
    mov [miLista_tail], si
    jmp GFX_L21
GFX_L20:
    mov bx, [miLista_tail]
    mov HEAP[bx+2], si
    mov [miLista_tail], si
GFX_L21:
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: TAMANO miLista  -> T9
    ; MOSTRAR T9 en modo grafico
    mov ax, [T9]
    call PRINT_NUM_GRAFICO
    ; MOSTRAR 999 en modo grafico
    mov ax, 999
    call PRINT_NUM_GRAFICO
    call GRAFICAR_TODO
    mov ah, 00h
    int 16h
    mov ax, 0003h
    int 10h
    mov ax, 4C00h
    int 21h
main endp

; ============================================
; RUTINAS GRAFICAS
; ============================================

DIBUJAR_PIXEL proc
    push ax
    push bx
    mov ah, 0Ch
    mov bh, 00h
    int 10h
    pop bx
    pop ax
    ret
DIBUJAR_PIXEL endp

DIBUJAR_RECTANGULO proc
    push ax
    push bx
    push cx
    push dx
    push si
    push di
    mov [rect_x], cx
    mov [rect_y], dx
    mov [rect_w], si
    mov [rect_h], di
    mov [rect_color], al
dr_fila:
    mov cx, [rect_x]
    mov si, [rect_w]
dr_columna:
    mov dx, [rect_y]
    mov al, [rect_color]
    call DIBUJAR_PIXEL
    inc cx
    dec si
    jnz dr_columna
    inc word ptr [rect_y]
    dec word ptr [rect_h]
    jnz dr_fila
    pop di
    pop si
    pop dx
    pop cx
    pop bx
    pop ax
    ret
DIBUJAR_RECTANGULO endp

LIMPIAR_PANTALLA proc
    push ax
    push bx
    push cx
    push dx
    mov ax, 0600h
    mov bh, 00h
    mov cx, 0000h
    mov dx, 1827h
    int 10h
    pop dx
    pop cx
    pop bx
    pop ax
    ret
LIMPIAR_PANTALLA endp

SET_CURSOR_PIXEL proc
    push ax
    push bx
    push cx
    push dx
    mov ax, dx
    mov bl, 8
    div bl
    mov dh, al
    mov ax, cx
    mov bl, 8
    div bl
    mov dl, al
    mov ah, 02h
    mov bh, 00h
    int 10h
    pop dx
    pop cx
    pop bx
    pop ax
    ret
SET_CURSOR_PIXEL endp

PRINT_NUM_GRAFICO proc
    push ax
    push bx
    push cx
    push dx
    cmp ax, 0
    jne png_convertir
    mov al, '0'
    mov ah, 0Eh
    int 10h
    jmp png_fin
png_convertir:
    xor cx, cx
    mov bx, 10
png_dividir:
    xor dx, dx
    div bx
    push dx
    inc cx
    cmp ax, 0
    jne png_dividir
png_imprimir:
    pop dx
    mov al, dl
    add al, '0'
    mov ah, 0Eh
    int 10h
    loop png_imprimir
png_fin:
    pop dx
    pop cx
    pop bx
    pop ax
    ret
PRINT_NUM_GRAFICO endp

GRAFICAR_TODO proc
    call LIMPIAR_PANTALLA
    call GRAFICAR_PILA_miPila
    call GRAFICAR_COLA_miCola
    call GRAFICAR_LISTA_miLista
    ret
GRAFICAR_TODO endp

GRAFICAR_PILA_miPila proc
    mov word ptr [gfx_i], 0
miPila_gp_loop:
    mov ax, [gfx_i]
    cmp ax, [miPila_top]
    jge miPila_gp_fin
    mov bx, ax
    shl bx, 1
    mov ax, miPila[bx]
    mov [gfx_valor], ax
    mov ax, [gfx_i]
    mov bx, 12
    mul bx
    mov dx, 180
    sub dx, ax
    mov cx, 10
    mov si, 42
    mov di, 10
    mov al, 0Ah
    call DIBUJAR_RECTANGULO
    mov cx, 22
    call SET_CURSOR_PIXEL
    mov ax, [gfx_valor]
    call PRINT_NUM_GRAFICO
    inc word ptr [gfx_i]
    jmp miPila_gp_loop
miPila_gp_fin:
    ret
GRAFICAR_PILA_miPila endp

GRAFICAR_COLA_miCola proc
    mov word ptr [gfx_i], 0
miCola_gc_loop:
    mov ax, [gfx_i]
    cmp ax, [miCola_count]
    jge miCola_gc_fin
    mov bx, [miCola_front]
    add bx, ax
    cmp bx, 50
    jl miCola_gc_idx_ok
    sub bx, 50
miCola_gc_idx_ok:
    shl bx, 1
    mov ax, miCola[bx]
    mov [gfx_valor], ax
    mov ax, [gfx_i]
    mov bx, 38
    mul bx
    mov cx, 70
    add cx, ax
    mov dx, 20
    mov si, 34
    mov di, 12
    mov al, 0Bh
    call DIBUJAR_RECTANGULO
    call SET_CURSOR_PIXEL
    mov ax, [gfx_valor]
    call PRINT_NUM_GRAFICO
    inc word ptr [gfx_i]
    jmp miCola_gc_loop
miCola_gc_fin:
    ret
GRAFICAR_COLA_miCola endp

GRAFICAR_LISTA_miLista proc
    mov bx, [miLista_head]
    mov word ptr [gfx_i], 0
miLista_gl_loop:
    cmp bx, 0
    je miLista_gl_fin
    mov ax, HEAP[bx]
    mov [gfx_valor], ax
    push bx
    mov ax, [gfx_i]
    mov bx, 42
    mul bx
    mov cx, 10
    add cx, ax
    mov dx, 48
    mov si, 34
    mov di, 12
    mov al, 0Eh
    call DIBUJAR_RECTANGULO
    call SET_CURSOR_PIXEL
    mov ax, [gfx_valor]
    call PRINT_NUM_GRAFICO
    pop bx
    mov bx, HEAP[bx+2]
    inc word ptr [gfx_i]
    jmp miLista_gl_loop
miLista_gl_fin:
    ret
GRAFICAR_LISTA_miLista endp

end main
