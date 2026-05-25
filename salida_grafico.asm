; ============================================
; CODIGO ENSAMBLADOR GRAFICO GENERADO - DSL
; Intel 8086 / EMU8086 / MASM - Modo 13h
; ============================================

.model small
.stack 1000h

.data
    titulo db 'DSL - VISUALIZACION GRAFICA', 0

    HEAP dw 1000 dup(0)
    HEAP_PTR dw 2
    gfx_i dw 0
    gfx_valor dw 0
    gfx_busqueda dw 0
    gfx_busqueda_resultado dw 0
    gfx_busqueda_activa dw 0
    gfx_ultimo_desapilado dw 0
    gfx_color db 0Fh
    listaOpt_head dw 0
    listaOpt_tail dw 0
    listaOpt_count dw 0
    baseLista dw 0
    valorLista dw 0
    copiaLista dw 0
    i dw 0
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

    ; Modo grafico 13h: 320x200, 256 colores
    mov ax, 0013h
    int 10h

    ; CREAR LISTA listaOpt TAMANO 10
    call GRAFICAR_TODO
    mov ax, 40
    mov [baseLista], ax
    mov ax, 60
    mov [valorLista], ax
    mov ax, 60
    mov [copiaLista], ax
    ; INSERTAR_FINAL 60 EN listaOpt
    mov ax, 60
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [listaOpt_head], 0
    jne GFX_L1
    mov [listaOpt_head], si
    mov [listaOpt_tail], si
    jmp GFX_L3
GFX_L1:
    mov bx, [listaOpt_tail]
    mov HEAP[bx+2], si
    mov [listaOpt_tail], si
GFX_L3:
    inc word ptr [listaOpt_count]
    call GRAFICAR_TODO
    ; INSERTAR_FINAL 60 EN listaOpt
    mov ax, 60
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [listaOpt_head], 0
    jne GFX_L4
    mov [listaOpt_head], si
    mov [listaOpt_tail], si
    jmp GFX_L6
GFX_L4:
    mov bx, [listaOpt_tail]
    mov HEAP[bx+2], si
    mov [listaOpt_tail], si
GFX_L6:
    inc word ptr [listaOpt_count]
    call GRAFICAR_TODO
    ; INSERTAR_INICIO 10 EN listaOpt
    mov ax, 10
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [listaOpt_head], 0
    jne GFX_L8
    mov [listaOpt_head], si
    mov [listaOpt_tail], si
    jmp GFX_L9
GFX_L8:
    mov bx, [listaOpt_head]
    mov HEAP[si+2], bx
    mov [listaOpt_head], si
    jmp GFX_L9
    cmp word ptr [listaOpt_head], 0
    jne GFX_L7
    mov [listaOpt_head], si
    mov [listaOpt_tail], si
    jmp GFX_L9
GFX_L7:
    mov bx, [listaOpt_tail]
    mov HEAP[bx+2], si
    mov [listaOpt_tail], si
GFX_L9:
    inc word ptr [listaOpt_count]
    call GRAFICAR_TODO
    mov ax, 0
    mov [i], ax
L1:
    ; T3 = i < 3
    mov ax, [i]
    cmp ax, 3
    mov word ptr [T3], 0
    jl GFX_L10
    jmp GFX_L11
GFX_L10:
    mov word ptr [T3], 1
GFX_L11:
    mov ax, [T3]
    cmp ax, 0
    je L2
    ; INSERTAR_FINAL i EN listaOpt
    mov ax, [i]
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 4
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    cmp word ptr [listaOpt_head], 0
    jne GFX_L12
    mov [listaOpt_head], si
    mov [listaOpt_tail], si
    jmp GFX_L14
GFX_L12:
    mov bx, [listaOpt_tail]
    mov HEAP[bx+2], si
    mov [listaOpt_tail], si
GFX_L14:
    inc word ptr [listaOpt_count]
    call GRAFICAR_TODO
    ; T4 = i + 1
    mov ax, [i]
    add ax, 1
    mov [T4], ax
    mov ax, [T4]
    mov [i], ax
    jmp L1
L2:
    ; VACIA EN listaOpt -> T5
    mov word ptr [T5], 0
    cmp word ptr [listaOpt_count], 0
    je GFX_L15
    jmp GFX_L16
GFX_L15:
    mov word ptr [T5], 1
GFX_L16:
    ; T6 = T5 == 0
    mov ax, [T5]
    cmp ax, 0
    mov word ptr [T6], 0
    je GFX_L17
    jmp GFX_L18
GFX_L17:
    mov word ptr [T6], 1
GFX_L18:
    mov ax, [T6]
    cmp ax, 0
    je L3
    ; Operacion grafica pendiente: BUSCAR 60 EN listaOpt
    call GRAFICAR_TODO
L3:
    ; VACIA EN listaOpt -> T7
    mov word ptr [T7], 0
    cmp word ptr [listaOpt_count], 0
    je GFX_L19
    jmp GFX_L20
GFX_L19:
    mov word ptr [T7], 1
GFX_L20:
    ; T8 = T7 == 0
    mov ax, [T7]
    cmp ax, 0
    mov word ptr [T8], 0
    je GFX_L21
    jmp GFX_L22
GFX_L21:
    mov word ptr [T8], 1
GFX_L22:
    mov ax, [T8]
    cmp ax, 0
    je L6
    ; ELIMINAR_INICIO EN listaOpt
    cmp word ptr [listaOpt_head], 0
    je GFX_L23
    mov bx, [listaOpt_head]
    mov ax, HEAP[bx+2]
    mov [listaOpt_head], ax
    dec word ptr [listaOpt_count]
    cmp ax, 0
    jne GFX_L23
    mov word ptr [listaOpt_tail], 0
GFX_L23:
    call GRAFICAR_TODO
    jmp L3
L6:
    ; TAMANO EN listaOpt -> T9
    mov ax, [listaOpt_count]
    mov [T9], ax
    ; MOSTRAR T9 en modo grafico
    mov cx, 10
    mov dx, 70
    call SET_CURSOR_PIXEL
    mov al, 'T'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'A'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'M'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'A'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'O'
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
    mov ax, [T9]
    call PRINT_NUM_GRAFICO
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
    call GRAFICAR_LISTA_listaOpt
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

GRAFICAR_LISTA_listaOpt proc
    mov bx, [listaOpt_head]
    mov word ptr [gfx_i], 0
listaOpt_gl_loop:
    cmp bx, 0
    je listaOpt_gl_fin
    mov ax, HEAP[bx]
    mov [gfx_valor], ax
    push bx
    mov ax, [gfx_i]
    mov bx, 42
    mul bx
    mov cx, 10
    add cx, ax
    mov dx, 48
    call SET_CURSOR_PIXEL
    call PRINT_VALOR_CORCHETES
    pop bx
    mov bx, HEAP[bx+2]
    inc word ptr [gfx_i]
    jmp listaOpt_gl_loop
listaOpt_gl_fin:
    ret
GRAFICAR_LISTA_listaOpt endp

end main
