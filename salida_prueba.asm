; ============================================
; CÓDIGO ENSAMBLADOR GENERADO - DSL
; Compilado para Arquitectura: Intel 8086
; Modo: Real Mode (DOS/BIOS compatible)
; Registros de 16 bits: AX, BX, CX, DX, SI, DI
; Segmentación: CS, DS, ES, SS
; ============================================

.model small
.stack 100h

.data
    titulo db ''EJECUCION DE PROGRAMA DSL'', 0Dh, 0Ah, ''$''
    newline db 0Dh, 0Ah, ''$''
    espacio db '' '', ''$''
    arbolVacio db ''Arbol vacio'', 0Dh, 0Ah, ''$''

.code
main proc
    mov ax, @data
    mov ds, ax
    mov es, ax
    mov dx, offset titulo
    call print_string

    ; *** ASIGNACIÓN: a = 5
    mov ax, 5
    mov [a], ax
    ; *** ASIGNACIÓN: b = 3
    mov ax, 3
    mov [b], ax
    ; *** OPERACIÓN MATEMÁTICA: c = a + b
    mov ax, [a]
    mov bx, [b]
    add ax, bx
    mov [c], ax
    ; *** PRINT: c
    mov ax, [c]
    mov dl, al
    mov ah, 02h
    int 21h
    ; *** IF_FALSE c GOTO fin
    mov ax, [c]
    cmp ax, 0
    je fin
    ; *** APILAR c EN pila1
    mov ax, [c]
    call apilar_pila1
fin:
    jmp salida

; ============================================
; SECCIÓN: RUTINAS AUXILIARES
; ============================================

print_string proc
    mov ah, 09h
    int 21h
    ret
print_string endp


; Datos adicionales
    msg_error db ''Error en la operacion'', 0Dh, 0Ah, ''$''

; ============================================
; SECCIÓN: FIN DEL PROGRAMA
; ============================================

    mov ax, 4C00h
    int 21h

main endp
end main
