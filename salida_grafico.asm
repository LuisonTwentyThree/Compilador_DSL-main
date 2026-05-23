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
    gfx_queue dw 128 dup(0)
    gfx_q_front dw 0
    gfx_q_rear dw 0
    rect_x dw 0
    rect_y dw 0
    rect_w dw 0
    rect_h dw 0
    rect_color db 0
    miArbol_root dw 0

.code
main proc
    mov ax, @data
    mov ds, ax

    ; Modo grafico 13h: 320x200, 256 colores
    mov ax, 0013h
    int 10h

    ; CREAR ARBOL miArbol TAMANO 100
    call GRAFICAR_TODO
    ; AGREGARNODO 50 EN miArbol
    mov ax, 50
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L1
    mov [miArbol_root], si
    jmp GFX_L6
GFX_L1:
    mov bx, [miArbol_root]
GFX_L2:
    cmp ax, HEAP[bx]
    jg GFX_L3
    cmp word ptr HEAP[bx+2], 0
    je GFX_L4
    mov bx, HEAP[bx+2]
    jmp GFX_L2
GFX_L4:
    mov HEAP[bx+2], si
    jmp GFX_L6
GFX_L3:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L5
    mov bx, HEAP[bx+4]
    jmp GFX_L2
GFX_L5:
    mov HEAP[bx+4], si
GFX_L6:
    call GRAFICAR_TODO
    ; AGREGARNODO 30 EN miArbol
    mov ax, 30
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L7
    mov [miArbol_root], si
    jmp GFX_L12
GFX_L7:
    mov bx, [miArbol_root]
GFX_L8:
    cmp ax, HEAP[bx]
    jg GFX_L9
    cmp word ptr HEAP[bx+2], 0
    je GFX_L10
    mov bx, HEAP[bx+2]
    jmp GFX_L8
GFX_L10:
    mov HEAP[bx+2], si
    jmp GFX_L12
GFX_L9:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L11
    mov bx, HEAP[bx+4]
    jmp GFX_L8
GFX_L11:
    mov HEAP[bx+4], si
GFX_L12:
    call GRAFICAR_TODO
    ; AGREGARNODO 70 EN miArbol
    mov ax, 70
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L13
    mov [miArbol_root], si
    jmp GFX_L18
GFX_L13:
    mov bx, [miArbol_root]
GFX_L14:
    cmp ax, HEAP[bx]
    jg GFX_L15
    cmp word ptr HEAP[bx+2], 0
    je GFX_L16
    mov bx, HEAP[bx+2]
    jmp GFX_L14
GFX_L16:
    mov HEAP[bx+2], si
    jmp GFX_L18
GFX_L15:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L17
    mov bx, HEAP[bx+4]
    jmp GFX_L14
GFX_L17:
    mov HEAP[bx+4], si
GFX_L18:
    call GRAFICAR_TODO
    ; AGREGARNODO 20 EN miArbol
    mov ax, 20
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L19
    mov [miArbol_root], si
    jmp GFX_L24
GFX_L19:
    mov bx, [miArbol_root]
GFX_L20:
    cmp ax, HEAP[bx]
    jg GFX_L21
    cmp word ptr HEAP[bx+2], 0
    je GFX_L22
    mov bx, HEAP[bx+2]
    jmp GFX_L20
GFX_L22:
    mov HEAP[bx+2], si
    jmp GFX_L24
GFX_L21:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L23
    mov bx, HEAP[bx+4]
    jmp GFX_L20
GFX_L23:
    mov HEAP[bx+4], si
GFX_L24:
    call GRAFICAR_TODO
    ; AGREGARNODO 40 EN miArbol
    mov ax, 40
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L25
    mov [miArbol_root], si
    jmp GFX_L30
GFX_L25:
    mov bx, [miArbol_root]
GFX_L26:
    cmp ax, HEAP[bx]
    jg GFX_L27
    cmp word ptr HEAP[bx+2], 0
    je GFX_L28
    mov bx, HEAP[bx+2]
    jmp GFX_L26
GFX_L28:
    mov HEAP[bx+2], si
    jmp GFX_L30
GFX_L27:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L29
    mov bx, HEAP[bx+4]
    jmp GFX_L26
GFX_L29:
    mov HEAP[bx+4], si
GFX_L30:
    call GRAFICAR_TODO
    ; AGREGARNODO 60 EN miArbol
    mov ax, 60
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L31
    mov [miArbol_root], si
    jmp GFX_L36
GFX_L31:
    mov bx, [miArbol_root]
GFX_L32:
    cmp ax, HEAP[bx]
    jg GFX_L33
    cmp word ptr HEAP[bx+2], 0
    je GFX_L34
    mov bx, HEAP[bx+2]
    jmp GFX_L32
GFX_L34:
    mov HEAP[bx+2], si
    jmp GFX_L36
GFX_L33:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L35
    mov bx, HEAP[bx+4]
    jmp GFX_L32
GFX_L35:
    mov HEAP[bx+4], si
GFX_L36:
    call GRAFICAR_TODO
    ; AGREGARNODO 80 EN miArbol
    mov ax, 80
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L37
    mov [miArbol_root], si
    jmp GFX_L42
GFX_L37:
    mov bx, [miArbol_root]
GFX_L38:
    cmp ax, HEAP[bx]
    jg GFX_L39
    cmp word ptr HEAP[bx+2], 0
    je GFX_L40
    mov bx, HEAP[bx+2]
    jmp GFX_L38
GFX_L40:
    mov HEAP[bx+2], si
    jmp GFX_L42
GFX_L39:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L41
    mov bx, HEAP[bx+4]
    jmp GFX_L38
GFX_L41:
    mov HEAP[bx+4], si
GFX_L42:
    call GRAFICAR_TODO
    ; AGREGARNODO 10 EN miArbol
    mov ax, 10
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L43
    mov [miArbol_root], si
    jmp GFX_L48
GFX_L43:
    mov bx, [miArbol_root]
GFX_L44:
    cmp ax, HEAP[bx]
    jg GFX_L45
    cmp word ptr HEAP[bx+2], 0
    je GFX_L46
    mov bx, HEAP[bx+2]
    jmp GFX_L44
GFX_L46:
    mov HEAP[bx+2], si
    jmp GFX_L48
GFX_L45:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L47
    mov bx, HEAP[bx+4]
    jmp GFX_L44
GFX_L47:
    mov HEAP[bx+4], si
GFX_L48:
    call GRAFICAR_TODO
    ; AGREGARNODO 25 EN miArbol
    mov ax, 25
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L49
    mov [miArbol_root], si
    jmp GFX_L54
GFX_L49:
    mov bx, [miArbol_root]
GFX_L50:
    cmp ax, HEAP[bx]
    jg GFX_L51
    cmp word ptr HEAP[bx+2], 0
    je GFX_L52
    mov bx, HEAP[bx+2]
    jmp GFX_L50
GFX_L52:
    mov HEAP[bx+2], si
    jmp GFX_L54
GFX_L51:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L53
    mov bx, HEAP[bx+4]
    jmp GFX_L50
GFX_L53:
    mov HEAP[bx+4], si
GFX_L54:
    call GRAFICAR_TODO
    ; INORDEN EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 22
    call SET_CURSOR_PIXEL
    mov al, 'I'
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
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
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
    mov bx, [miArbol_root]
    call RECORRIDO_INORDEN_miArbol
    ; MOSTRAR T1 omitido: el recorrido ya se imprimio en modo grafico
    ; PREORDEN EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 10
    call SET_CURSOR_PIXEL
    mov al, 'P'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
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
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
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
    mov bx, [miArbol_root]
    call RECORRIDO_PREORDEN_miArbol
    ; MOSTRAR T2 omitido: el recorrido ya se imprimio en modo grafico
    ; POSTORDEN EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 34
    call SET_CURSOR_PIXEL
    mov al, 'P'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'O'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'S'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'T'
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
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
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
    mov bx, [miArbol_root]
    call RECORRIDO_POSTORDEN_miArbol
    ; MOSTRAR T3 omitido: el recorrido ya se imprimio en modo grafico
    ; RECORRIDOPORNIVELES EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 46
    call SET_CURSOR_PIXEL
    mov al, 'N'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'I'
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
    mov al, 'L'
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
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov bx, [miArbol_root]
    call RECORRIDO_NIVELES_miArbol
    ; MOSTRAR T4 omitido: el recorrido ya se imprimio en modo grafico
    ; Operacion grafica pendiente: BUSCAR 40 EN miArbol
    call GRAFICAR_TODO
    ; Operacion grafica pendiente: BUSCAR 99 EN miArbol
    call GRAFICAR_TODO
    ; ELIMINAR 10 EN miArbol
    mov ax, 10
    mov [gfx_busqueda], ax
    mov bx, [miArbol_root]
    cmp bx, 0
    je GFX_L66
    mov word ptr [gfx_i], 0
    mov word ptr [gfx_valor], 0
GFX_L55:
    cmp bx, 0
    je GFX_L66
    mov ax, HEAP[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L57
    jl GFX_L56
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 1
    mov bx, HEAP[bx+2]
    jmp GFX_L55
GFX_L56:
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 2
    mov bx, HEAP[bx+4]
    jmp GFX_L55
GFX_L57:
    mov ax, HEAP[bx+2]
    mov cx, HEAP[bx+4]
    cmp ax, 0
    jne GFX_L58
    mov dx, cx
    jmp GFX_L63
GFX_L58:
    cmp cx, 0
    jne GFX_L59
    mov dx, ax
    jmp GFX_L63
GFX_L59:
    mov [gfx_ultimo_desapilado], bx
    mov si, bx
    mov bx, cx
    mov cl, 2
GFX_L60:
    cmp word ptr HEAP[bx+2], 0
    je GFX_L61
    mov si, bx
    mov bx, HEAP[bx+2]
    mov cl, 1
    jmp GFX_L60
GFX_L61:
    mov ax, HEAP[bx]
    mov di, [gfx_ultimo_desapilado]
    mov HEAP[di], ax
    mov dx, HEAP[bx+4]
    cmp cl, 1
    jne GFX_L62
    mov HEAP[si+2], dx
    jmp GFX_L66
GFX_L62:
    mov HEAP[si+4], dx
    jmp GFX_L66
GFX_L63:
    cmp word ptr [gfx_i], 0
    jne GFX_L64
    mov [miArbol_root], dx
    jmp GFX_L66
GFX_L64:
    mov si, [gfx_i]
    cmp word ptr [gfx_valor], 1
    jne GFX_L65
    mov HEAP[si+2], dx
    jmp GFX_L66
GFX_L65:
    mov HEAP[si+4], dx
GFX_L66:
    call GRAFICAR_TODO
    ; ELIMINAR 25 EN miArbol
    mov ax, 25
    mov [gfx_busqueda], ax
    mov bx, [miArbol_root]
    cmp bx, 0
    je GFX_L78
    mov word ptr [gfx_i], 0
    mov word ptr [gfx_valor], 0
GFX_L67:
    cmp bx, 0
    je GFX_L78
    mov ax, HEAP[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L69
    jl GFX_L68
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 1
    mov bx, HEAP[bx+2]
    jmp GFX_L67
GFX_L68:
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 2
    mov bx, HEAP[bx+4]
    jmp GFX_L67
GFX_L69:
    mov ax, HEAP[bx+2]
    mov cx, HEAP[bx+4]
    cmp ax, 0
    jne GFX_L70
    mov dx, cx
    jmp GFX_L75
GFX_L70:
    cmp cx, 0
    jne GFX_L71
    mov dx, ax
    jmp GFX_L75
GFX_L71:
    mov [gfx_ultimo_desapilado], bx
    mov si, bx
    mov bx, cx
    mov cl, 2
GFX_L72:
    cmp word ptr HEAP[bx+2], 0
    je GFX_L73
    mov si, bx
    mov bx, HEAP[bx+2]
    mov cl, 1
    jmp GFX_L72
GFX_L73:
    mov ax, HEAP[bx]
    mov di, [gfx_ultimo_desapilado]
    mov HEAP[di], ax
    mov dx, HEAP[bx+4]
    cmp cl, 1
    jne GFX_L74
    mov HEAP[si+2], dx
    jmp GFX_L78
GFX_L74:
    mov HEAP[si+4], dx
    jmp GFX_L78
GFX_L75:
    cmp word ptr [gfx_i], 0
    jne GFX_L76
    mov [miArbol_root], dx
    jmp GFX_L78
GFX_L76:
    mov si, [gfx_i]
    cmp word ptr [gfx_valor], 1
    jne GFX_L77
    mov HEAP[si+2], dx
    jmp GFX_L78
GFX_L77:
    mov HEAP[si+4], dx
GFX_L78:
    call GRAFICAR_TODO
    ; ELIMINAR 20 EN miArbol
    mov ax, 20
    mov [gfx_busqueda], ax
    mov bx, [miArbol_root]
    cmp bx, 0
    je GFX_L90
    mov word ptr [gfx_i], 0
    mov word ptr [gfx_valor], 0
GFX_L79:
    cmp bx, 0
    je GFX_L90
    mov ax, HEAP[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L81
    jl GFX_L80
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 1
    mov bx, HEAP[bx+2]
    jmp GFX_L79
GFX_L80:
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 2
    mov bx, HEAP[bx+4]
    jmp GFX_L79
GFX_L81:
    mov ax, HEAP[bx+2]
    mov cx, HEAP[bx+4]
    cmp ax, 0
    jne GFX_L82
    mov dx, cx
    jmp GFX_L87
GFX_L82:
    cmp cx, 0
    jne GFX_L83
    mov dx, ax
    jmp GFX_L87
GFX_L83:
    mov [gfx_ultimo_desapilado], bx
    mov si, bx
    mov bx, cx
    mov cl, 2
GFX_L84:
    cmp word ptr HEAP[bx+2], 0
    je GFX_L85
    mov si, bx
    mov bx, HEAP[bx+2]
    mov cl, 1
    jmp GFX_L84
GFX_L85:
    mov ax, HEAP[bx]
    mov di, [gfx_ultimo_desapilado]
    mov HEAP[di], ax
    mov dx, HEAP[bx+4]
    cmp cl, 1
    jne GFX_L86
    mov HEAP[si+2], dx
    jmp GFX_L90
GFX_L86:
    mov HEAP[si+4], dx
    jmp GFX_L90
GFX_L87:
    cmp word ptr [gfx_i], 0
    jne GFX_L88
    mov [miArbol_root], dx
    jmp GFX_L90
GFX_L88:
    mov si, [gfx_i]
    cmp word ptr [gfx_valor], 1
    jne GFX_L89
    mov HEAP[si+2], dx
    jmp GFX_L90
GFX_L89:
    mov HEAP[si+4], dx
GFX_L90:
    call GRAFICAR_TODO
    ; ELIMINAR 30 EN miArbol
    mov ax, 30
    mov [gfx_busqueda], ax
    mov bx, [miArbol_root]
    cmp bx, 0
    je GFX_L102
    mov word ptr [gfx_i], 0
    mov word ptr [gfx_valor], 0
GFX_L91:
    cmp bx, 0
    je GFX_L102
    mov ax, HEAP[bx]
    cmp ax, [gfx_busqueda]
    je GFX_L93
    jl GFX_L92
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 1
    mov bx, HEAP[bx+2]
    jmp GFX_L91
GFX_L92:
    mov [gfx_i], bx
    mov word ptr [gfx_valor], 2
    mov bx, HEAP[bx+4]
    jmp GFX_L91
GFX_L93:
    mov ax, HEAP[bx+2]
    mov cx, HEAP[bx+4]
    cmp ax, 0
    jne GFX_L94
    mov dx, cx
    jmp GFX_L99
GFX_L94:
    cmp cx, 0
    jne GFX_L95
    mov dx, ax
    jmp GFX_L99
GFX_L95:
    mov [gfx_ultimo_desapilado], bx
    mov si, bx
    mov bx, cx
    mov cl, 2
GFX_L96:
    cmp word ptr HEAP[bx+2], 0
    je GFX_L97
    mov si, bx
    mov bx, HEAP[bx+2]
    mov cl, 1
    jmp GFX_L96
GFX_L97:
    mov ax, HEAP[bx]
    mov di, [gfx_ultimo_desapilado]
    mov HEAP[di], ax
    mov dx, HEAP[bx+4]
    cmp cl, 1
    jne GFX_L98
    mov HEAP[si+2], dx
    jmp GFX_L102
GFX_L98:
    mov HEAP[si+4], dx
    jmp GFX_L102
GFX_L99:
    cmp word ptr [gfx_i], 0
    jne GFX_L100
    mov [miArbol_root], dx
    jmp GFX_L102
GFX_L100:
    mov si, [gfx_i]
    cmp word ptr [gfx_valor], 1
    jne GFX_L101
    mov HEAP[si+2], dx
    jmp GFX_L102
GFX_L101:
    mov HEAP[si+4], dx
GFX_L102:
    call GRAFICAR_TODO
    ; INORDEN EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 22
    call SET_CURSOR_PIXEL
    mov al, 'I'
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
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
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
    mov bx, [miArbol_root]
    call RECORRIDO_INORDEN_miArbol
    ; MOSTRAR T5 omitido: el recorrido ya se imprimio en modo grafico
    ; AGREGARNODO 35 EN miArbol
    mov ax, 35
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L103
    mov [miArbol_root], si
    jmp GFX_L108
GFX_L103:
    mov bx, [miArbol_root]
GFX_L104:
    cmp ax, HEAP[bx]
    jg GFX_L105
    cmp word ptr HEAP[bx+2], 0
    je GFX_L106
    mov bx, HEAP[bx+2]
    jmp GFX_L104
GFX_L106:
    mov HEAP[bx+2], si
    jmp GFX_L108
GFX_L105:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L107
    mov bx, HEAP[bx+4]
    jmp GFX_L104
GFX_L107:
    mov HEAP[bx+4], si
GFX_L108:
    call GRAFICAR_TODO
    ; AGREGARNODO 65 EN miArbol
    mov ax, 65
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L109
    mov [miArbol_root], si
    jmp GFX_L114
GFX_L109:
    mov bx, [miArbol_root]
GFX_L110:
    cmp ax, HEAP[bx]
    jg GFX_L111
    cmp word ptr HEAP[bx+2], 0
    je GFX_L112
    mov bx, HEAP[bx+2]
    jmp GFX_L110
GFX_L112:
    mov HEAP[bx+2], si
    jmp GFX_L114
GFX_L111:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L113
    mov bx, HEAP[bx+4]
    jmp GFX_L110
GFX_L113:
    mov HEAP[bx+4], si
GFX_L114:
    call GRAFICAR_TODO
    ; AGREGARNODO 45 EN miArbol
    mov ax, 45
    mov si, [HEAP_PTR]
    add word ptr [HEAP_PTR], 6
    mov HEAP[si], ax
    mov word ptr HEAP[si+2], 0
    mov word ptr HEAP[si+4], 0
    cmp word ptr [miArbol_root], 0
    jne GFX_L115
    mov [miArbol_root], si
    jmp GFX_L120
GFX_L115:
    mov bx, [miArbol_root]
GFX_L116:
    cmp ax, HEAP[bx]
    jg GFX_L117
    cmp word ptr HEAP[bx+2], 0
    je GFX_L118
    mov bx, HEAP[bx+2]
    jmp GFX_L116
GFX_L118:
    mov HEAP[bx+2], si
    jmp GFX_L120
GFX_L117:
    cmp word ptr HEAP[bx+4], 0
    je GFX_L119
    mov bx, HEAP[bx+4]
    jmp GFX_L116
GFX_L119:
    mov HEAP[bx+4], si
GFX_L120:
    call GRAFICAR_TODO
    ; INORDEN EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 22
    call SET_CURSOR_PIXEL
    mov al, 'I'
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
    mov al, 'R'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'D'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'E'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'N'
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
    mov bx, [miArbol_root]
    call RECORRIDO_INORDEN_miArbol
    ; MOSTRAR T6 omitido: el recorrido ya se imprimio en modo grafico
    ; RECORRIDOPORNIVELES EN miArbol impreso en modo grafico
    mov cx, 10
    mov dx, 46
    call SET_CURSOR_PIXEL
    mov al, 'N'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, 'I'
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
    mov al, 'L'
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
    mov al, ':'
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov al, ' '
    mov ah, 0Eh
    mov bl, [gfx_color]
    int 10h
    mov bx, [miArbol_root]
    call RECORRIDO_NIVELES_miArbol
    ; MOSTRAR T7 omitido: el recorrido ya se imprimio en modo grafico
    ; MOSTRAR 999 en modo grafico
    mov ax, 999
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
    call GRAFICAR_ARBOL_miArbol
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

GRAFICAR_ARBOL_miArbol proc
    mov bx, [miArbol_root]
    mov cx, 150
    mov dx, 78
    mov si, 60
    call GRAFICAR_ARBOL_REC_miArbol
    ret
GRAFICAR_ARBOL_miArbol endp

GRAFICAR_ARBOL_REC_miArbol proc
    cmp bx, 0
    je GFX_L121
    push bx
    push cx
    push dx
    push si
    mov ax, HEAP[bx]
    mov [gfx_valor], ax
    mov si, 28
    call SET_CURSOR_PIXEL
    mov ax, [gfx_valor]
    call PRINT_NUM_GRAFICO
    pop si
    pop dx
    pop cx
    pop bx

    ; Hijo izquierdo
    mov ax, HEAP[bx+2]
    cmp ax, 0
    je GFX_L125
    push ax
    push bx
    push cx
    push dx
    push si
    mov ax, si
    shr ax, 1
    sub cx, ax
    add dx, 12
    call SET_CURSOR_PIXEL
    mov al, '/'
    mov ah, 0Eh
    int 10h
    pop si
    pop dx
    pop cx
    pop bx
    pop ax
    push bx
    push cx
    push dx
    push si
    mov bx, ax
    sub cx, si
    add dx, 24
    shr si, 1
    cmp si, 8
    jge GFX_L123
    mov si, 8
GFX_L123:
    call GRAFICAR_ARBOL_REC_miArbol
    pop si
    pop dx
    pop cx
    pop bx
GFX_L125:

    ; Hijo derecho
    mov ax, HEAP[bx+4]
    cmp ax, 0
    je GFX_L126
    push ax
    push bx
    push cx
    push dx
    push si
    mov ax, si
    shr ax, 1
    add cx, ax
    add dx, 12
    call SET_CURSOR_PIXEL
    mov al, '\'
    mov ah, 0Eh
    int 10h
    pop si
    pop dx
    pop cx
    pop bx
    pop ax
    push bx
    push cx
    push dx
    push si
    mov bx, ax
    add cx, si
    add dx, 24
    shr si, 1
    cmp si, 8
    jge GFX_L124
    mov si, 8
GFX_L124:
    call GRAFICAR_ARBOL_REC_miArbol
    pop si
    pop dx
    pop cx
    pop bx
GFX_L126:
    jmp GFX_L122
GFX_L121:
    ; Nodo NULL: retorno directo, sin push locales pendientes
    ret
GFX_L122:
    ret
GRAFICAR_ARBOL_REC_miArbol endp

RECORRIDO_PREORDEN_miArbol proc
    cmp bx, 0
    je GFX_L127
    mov ax, HEAP[bx]
    call PRINT_NUM_GRAFICO
    call PRINT_ESPACIO_GRAFICO
    push bx
    mov bx, HEAP[bx+2]
    call RECORRIDO_PREORDEN_miArbol
    pop bx
    mov bx, HEAP[bx+4]
    call RECORRIDO_PREORDEN_miArbol
GFX_L127:
    ret
RECORRIDO_PREORDEN_miArbol endp

RECORRIDO_INORDEN_miArbol proc
    cmp bx, 0
    je GFX_L128
    push bx
    mov bx, HEAP[bx+2]
    call RECORRIDO_INORDEN_miArbol
    pop bx
    mov ax, HEAP[bx]
    call PRINT_NUM_GRAFICO
    call PRINT_ESPACIO_GRAFICO
    mov bx, HEAP[bx+4]
    call RECORRIDO_INORDEN_miArbol
GFX_L128:
    ret
RECORRIDO_INORDEN_miArbol endp

RECORRIDO_POSTORDEN_miArbol proc
    cmp bx, 0
    je GFX_L129
    push bx
    mov bx, HEAP[bx+2]
    call RECORRIDO_POSTORDEN_miArbol
    pop bx
    push bx
    mov bx, HEAP[bx+4]
    call RECORRIDO_POSTORDEN_miArbol
    pop bx
    mov ax, HEAP[bx]
    call PRINT_NUM_GRAFICO
    call PRINT_ESPACIO_GRAFICO
GFX_L129:
    ret
RECORRIDO_POSTORDEN_miArbol endp

RECORRIDO_NIVELES_miArbol proc
    cmp bx, 0
    je GFX_L133
    mov word ptr [gfx_q_front], 0
    mov word ptr [gfx_q_rear], 0
    mov si, [gfx_q_rear]
    shl si, 1
    mov gfx_queue[si], bx
    inc word ptr [gfx_q_rear]
GFX_L130:
    mov ax, [gfx_q_front]
    cmp ax, [gfx_q_rear]
    jge GFX_L133
    mov si, ax
    shl si, 1
    mov bx, gfx_queue[si]
    inc word ptr [gfx_q_front]
    mov ax, HEAP[bx]
    call PRINT_NUM_GRAFICO
    call PRINT_ESPACIO_GRAFICO
    mov ax, HEAP[bx+2]
    cmp ax, 0
    je GFX_L131
    mov si, [gfx_q_rear]
    cmp si, 128
    jge GFX_L131
    shl si, 1
    mov gfx_queue[si], ax
    inc word ptr [gfx_q_rear]
GFX_L131:
    mov ax, HEAP[bx+4]
    cmp ax, 0
    je GFX_L132
    mov si, [gfx_q_rear]
    cmp si, 128
    jge GFX_L132
    shl si, 1
    mov gfx_queue[si], ax
    inc word ptr [gfx_q_rear]
GFX_L132:
    jmp GFX_L130
GFX_L133:
    ret
RECORRIDO_NIVELES_miArbol endp

end main
