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
    gfx_busqueda_resultado dw 0
    gfx_busqueda_activa dw 0
    gfx_ultimo_desapilado dw 0
    gfx_color db 0Fh
    rect_x dw 0
    rect_y dw 0
    rect_w dw 0
    rect_h dw 0
    rect_color db 0
    miHash_keys dw 100 dup(0)
    miHash_values dw 100 dup(0)
    miHash_count dw 0

.code
main proc
    mov ax, @data
    mov ds, ax

    ; Modo grafico 13h: 320x200, 256 colores
    mov ax, 0013h
    int 10h

    ; CREAR HASH miHash TAMANO 100
    call GRAFICAR_TODO
    ; INSERTAR 101 1000 EN miHash
    cmp word ptr [miHash_count], 100
    jge GFX_L1
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 101
    mov miHash_keys[bx], ax
    mov ax, 1000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
GFX_L1:
    call GRAFICAR_TODO
    ; INSERTAR 102 2000 EN miHash
    cmp word ptr [miHash_count], 100
    jge GFX_L2
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 102
    mov miHash_keys[bx], ax
    mov ax, 2000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
GFX_L2:
    call GRAFICAR_TODO
    ; INSERTAR 103 3000 EN miHash
    cmp word ptr [miHash_count], 100
    jge GFX_L3
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 103
    mov miHash_keys[bx], ax
    mov ax, 3000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
GFX_L3:
    call GRAFICAR_TODO
    ; BUSCAR 102 EN miHash
    mov ax, 102
    mov [gfx_busqueda], ax
    mov word ptr [gfx_valor], 0
    mov si, 0
GFX_L4:
    cmp si, [miHash_count]
    jge GFX_L6
    mov bx, si
    shl bx, 1
    mov ax, miHash_keys[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L5
    inc si
    jmp GFX_L4
GFX_L5:
    mov ax, miHash_values[bx]
    mov [gfx_valor], ax
    jmp GFX_L7
GFX_L6:
    mov word ptr [gfx_valor], 0
GFX_L7:
    mov ax, [gfx_valor]
    mov [gfx_busqueda_resultado], ax
    mov word ptr [gfx_busqueda_activa], 1
    call GRAFICAR_TODO
    ; ACTUALIZAR 102 2500 EN miHash
    mov ax, 102
    mov [gfx_busqueda], ax
    mov si, 0
GFX_L8:
    cmp si, [miHash_count]
    jge GFX_L10
    mov bx, si
    shl bx, 1
    mov ax, miHash_keys[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L9
    inc si
    jmp GFX_L8
GFX_L9:
    mov ax, 2500
    mov miHash_values[bx], ax
    jmp GFX_L11
GFX_L10:
    cmp word ptr [miHash_count], 100
    jge GFX_L11
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, [gfx_busqueda]
    mov miHash_keys[bx], ax
    mov ax, 2500
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
GFX_L11:
    call GRAFICAR_TODO
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
    call GRAFICAR_HASH_miHash
    call DIBUJAR_ULTIMA_BUSQUEDA
    ret
GRAFICAR_TODO endp

DIBUJAR_ULTIMA_BUSQUEDA proc
    cmp word ptr [gfx_busqueda_activa], 1
    jne DUB_FIN
    mov cx, 104
    mov dx, 88
    call SET_CURSOR_PIXEL
    mov al, 'B'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'U'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'S'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'C'
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
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [gfx_busqueda]
    call PRINT_NUM_GRAFICO
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [gfx_busqueda_resultado]
    call PRINT_NUM_GRAFICO
DUB_FIN:
    ret
DIBUJAR_ULTIMA_BUSQUEDA endp

GRAFICAR_HASH_miHash proc
    mov cx, 104
    mov dx, 104
    call SET_CURSOR_PIXEL
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov cx, 104
    mov dx, 112
    call SET_CURSOR_PIXEL
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'I'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'I'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'C'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'C'
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
    mov al, 'V'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'V'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'A'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'L'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'O'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov cx, 104
    mov dx, 120
    call SET_CURSOR_PIXEL
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov word ptr [gfx_i], 0
miHash_gh_loop:
    mov ax, [gfx_i]
    cmp ax, [miHash_count]
    jge miHash_gh_fin
    cmp ax, 8
    jge miHash_gh_fin
    mov ax, [gfx_i]
    mov bx, 8
    mul bx
    mov dx, 128
    add dx, ax
    mov cx, 104
    call SET_CURSOR_PIXEL
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '|'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov ax, [gfx_i]
    mov bx, 8
    mul bx
    mov dx, 128
    add dx, ax
    mov cx, 136
    call SET_CURSOR_PIXEL
    mov ax, [gfx_i]
    inc ax
    call PRINT_NUM_GRAFICO
    mov ax, [gfx_i]
    mov si, 8
    mul si
    mov dx, 128
    add dx, ax
    mov cx, 200
    call SET_CURSOR_PIXEL
    mov bx, [gfx_i]
    shl bx, 1
    mov ax, miHash_keys[bx]
    call PRINT_NUM_GRAFICO
    mov ax, [gfx_i]
    mov si, 8
    mul si
    mov dx, 128
    add dx, ax
    mov cx, 264
    call SET_CURSOR_PIXEL
    mov bx, [gfx_i]
    shl bx, 1
    mov ax, miHash_values[bx]
    call PRINT_NUM_GRAFICO
    inc word ptr [gfx_i]
    jmp miHash_gh_loop
miHash_gh_fin:
    mov ax, [miHash_count]
    cmp ax, 8
    jle miHash_gh_fin_borde
    mov ax, 8
miHash_gh_fin_borde:
    mov bx, 8
    mul bx
    mov dx, 128
    add dx, ax
    mov cx, 104
    call SET_CURSOR_PIXEL
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '-'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, '+'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    ret
GRAFICAR_HASH_miHash endp

end main
