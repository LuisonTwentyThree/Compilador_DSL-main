; ============================================
; CODIGO ENSAMBLADOR GRAFICO GENERADO - DSL
; Intel 8086 / EMU8086 / MASM - Modo 13h
; ============================================

.model small
.stack 100h

.data
    titulo db 'DSL - VISUALIZACION GRAFICA', 0

    gfx_i dw 0
    gfx_valor dw 0
    gfx_busqueda dw 0
    gfx_ultimo_desapilado dw 0
    gfx_color db 0Fh
    rect_x dw 0
    rect_y dw 0
    rect_w dw 0
    rect_h dw 0
    rect_color db 0
    miPila dw 100 dup(0)
    miPila_top dw 0
    T1 dw 0
    T3 dw 0

.code
main proc
    mov ax, @data
    mov ds, ax

    ; Modo grafico 13h: 320x200, 256 colores
    mov ax, 0013h
    int 10h

    ; CREAR PILA miPila TAMANO 100
    call GRAFICAR_TODO
    ; APILAR 5 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L1
    mov ax, 5
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L1:
    call GRAFICAR_TODO
    ; APILAR 15 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L2
    mov ax, 15
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L2:
    call GRAFICAR_TODO
    ; APILAR 25 EN miPila
    cmp word ptr [miPila_top], 100
    jge GFX_L3
    mov ax, 25
    mov bx, [miPila_top]
    shl bx, 1
    mov miPila[bx], ax
    inc word ptr [miPila_top]
GFX_L3:
    call GRAFICAR_TODO
    ; TOPE EN miPila -> T1
    cmp word ptr [miPila_top], 0
    je GFX_L4
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [T1], ax
    jmp GFX_L5
GFX_L4:
    mov word ptr [T1], 0
GFX_L5:
    ; MOSTRAR T1 en modo grafico
    mov cx, 10
    mov dx, 94
    call SET_CURSOR_PIXEL
    mov al, 'T'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'O'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'P'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [T1]
    call PRINT_NUM_GRAFICO
    ; Operacion grafica pendiente: VACIA miPila  -> T2
    ; Operacion grafica pendiente: IF_FALSE T2 GOTO -> L1
    ; Operacion grafica pendiente: ERROR Estructura vacía  -> 
    ; Operacion grafica pendiente: ETIQUETA   -> L1
    ; DESAPILAR EN miPila
    cmp word ptr [miPila_top], 0
    je GFX_L6
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [gfx_ultimo_desapilado], ax
    dec word ptr [miPila_top]
    mov bx, [miPila_top]
    shl bx, 1
    mov word ptr miPila[bx], 0
    call GRAFICAR_TODO
    mov cx, 10
    mov dx, 82
    call SET_CURSOR_PIXEL
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'S'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'A'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'P'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'I'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'L'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'A'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [gfx_ultimo_desapilado]
    call PRINT_NUM_GRAFICO
    jmp GFX_L7
GFX_L6:
    call GRAFICAR_TODO
GFX_L7:
    ; TOPE EN miPila -> T3
    cmp word ptr [miPila_top], 0
    je GFX_L8
    mov bx, [miPila_top]
    dec bx
    shl bx, 1
    mov ax, miPila[bx]
    mov [T3], ax
    jmp GFX_L9
GFX_L8:
    mov word ptr [T3], 0
GFX_L9:
    ; MOSTRAR T3 en modo grafico
    mov cx, 10
    mov dx, 94
    call SET_CURSOR_PIXEL
    mov al, 'T'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'O'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'P'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [T3]
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
    mov ax, 0013h
    int 10h
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
    mov bl, [gfx_color]
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
    mov bl, [gfx_color]
    int 10h
    loop png_imprimir
png_fin:
    pop dx
    pop cx
    pop bx
    pop ax
    ret
PRINT_NUM_GRAFICO endp

PRINT_ESPACIO_GRAFICO proc
    push ax
    push bx
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    pop bx
    pop ax
    ret
PRINT_ESPACIO_GRAFICO endp

PRINT_VALOR_CORCHETES proc
    push ax
    push bx
    mov al, '['
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [gfx_valor]
    call PRINT_NUM_GRAFICO
    mov al, ']'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    pop bx
    pop ax
    ret
PRINT_VALOR_CORCHETES endp

PAUSA_GRAFICA proc
    push cx
    push dx
    mov cx, 1
pg_loop_ext:
    mov dx, 1000h
pg_loop_int:
    dec dx
    jnz pg_loop_int
    loop pg_loop_ext
    pop dx
    pop cx
    ret
PAUSA_GRAFICA endp

GRAFICAR_TODO proc
    mov byte ptr [gfx_color], 0Fh
    call LIMPIAR_PANTALLA
    call GRAFICAR_PILA_miPila
    ret
GRAFICAR_TODO endp

GRAFICAR_PILA_miPila proc
    mov cx, 0
    mov dx, 0
    mov si, 80
    mov di, 200
    mov al, 00h
    call DIBUJAR_RECTANGULO
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
    call SET_CURSOR_PIXEL
    call PRINT_VALOR_CORCHETES
    inc word ptr [gfx_i]
    jmp miPila_gp_loop
miPila_gp_fin:
    ret
GRAFICAR_PILA_miPila endp

end main
