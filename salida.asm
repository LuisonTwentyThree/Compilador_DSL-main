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

    miHash_keys dw 100 dup(0)
    miHash_values dw 100 dup(0)
    miHash_count dw 0

.code
main proc
    mov ax, @data
    mov ds, ax
    mov dx, offset titulo
    call print_string

    ; CREAR TABLA_HASH miHash CAPACIDAD 100
    ; INSERTAR 101 1000 EN miHash
    cmp word ptr [miHash_count], 100
    jge ASM_L1
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 101
    mov miHash_keys[bx], ax
    mov ax, 1000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
ASM_L1:
    ; INSERTAR 102 2000 EN miHash
    cmp word ptr [miHash_count], 100
    jge ASM_L2
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 102
    mov miHash_keys[bx], ax
    mov ax, 2000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
ASM_L2:
    ; INSERTAR 103 3000 EN miHash
    cmp word ptr [miHash_count], 100
    jge ASM_L3
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 103
    mov miHash_keys[bx], ax
    mov ax, 3000
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
ASM_L3:
    ; BUSCAR EN 102
    mov ax, [102]
    mov [miHash], ax
    ; INSERTAR 102 2500 EN miHash
    cmp word ptr [miHash_count], 100
    jge ASM_L4
    mov bx, [miHash_count]
    shl bx, 1
    mov ax, 102
    mov miHash_keys[bx], ax
    mov ax, 2500
    mov miHash_values[bx], ax
    inc word ptr [miHash_count]
ASM_L4:

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

