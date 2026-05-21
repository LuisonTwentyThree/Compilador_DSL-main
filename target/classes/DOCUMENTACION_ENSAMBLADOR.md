# 📚 DOCUMENTACIÓN - Generador de Ensamblador con Visualización de Árboles

## 📋 Descripción General

Este módulo proporciona un sistema completo para:
1. **Traducir cuádruplos a código ensamblador x86-64 real**
2. **Visualizar árboles binarios en ASCII**
3. **Generar reportes de ejecución con diagramas**
4. **Rastrear operaciones en estructuras de datos**

---

## 📂 Componentes del Sistema

### 1. **GeneradorEnsamblador.java**
Traduce cuádruplos intermedio a código ensamblador ejecutable.

**Características:**
- Soporte para operaciones matemáticas (+, -, *, /)
- Operaciones en árboles (INSERTAR, ELIMINAR)
- Operaciones en colas (ENCOLAR, DESENCOLAR)
- Operaciones en pilas (APILAR, DESAPILAR)
- Generación de rutinas de dibujo
- Control de flujo (IF, GOTO, ETIQUETAS)

**Ejemplo de uso:**
```java
GeneradorEnsamblador gen = new GeneradorEnsamblador();
Cuadruplo c = new Cuadruplo("AGREGARNODO", "10", "valor1", "arbol");
gen.traducirCuadruplo(c);
System.out.println(gen.obtenerCodigoEnsamblador());
```

### 2. **VisualizadorArboles.java**
Gestiona árboles binarios de búsqueda y proporciona visualizaciones.

**Operaciones soportadas:**
- `insertar(raiz, clave, valor)` - Inserta un nodo
- `eliminar(raiz, clave)` - Elimina un nodo
- `buscar(raiz, clave)` - Busca un nodo
- `dibujarArbol(raiz)` - Dibuja ASCII del árbol
- `dibujarPorNiveles(raiz)` - Vista BFS
- `recorridoPreorden/Inorden/Postorden` - Recorridos

**Ejemplo de uso:**
```java
VisualizadorArboles.NodoABB arbol = null;
arbol = VisualizadorArboles.insertar(arbol, 50, "raiz");
arbol = VisualizadorArboles.insertar(arbol, 30, "izq");
System.out.println(VisualizadorArboles.dibujarArbol(arbol));
```

### 3. **IntegradorSalidaASM.java**
Integra ambos componentes y genera archivos de salida.

**Métodos principales:**
- `procesarInsertar(clave, valor)` - Procesa inserción
- `procesarEliminar(clave)` - Procesa eliminación
- `procesarBuscar(clave)` - Procesa búsqueda
- `generarArchivoASM()` - Genera código ensamblador
- `generarArchivoVisualizacion()` - Genera reporte visual

**Ejemplo de uso:**
```java
IntegradorSalidaASM integrador = new IntegradorSalidaASM("salida");
integrador.procesarInsertar(50, "raiz");
integrador.procesarInsertar(30, "izq");
integrador.generarArchivosCompletos();
```

### 4. **EjemploGeneracionASM.java**
Demostración completa del sistema con casos de uso reales.

---

## 🎯 Flujo de Trabajo

### Paso 1: Generar Cuádruplos
```
Código DSL → Analizador → Cuádruplos
```

### Paso 2: Traducir a Ensamblador
```
Cuádruplos → GeneradorEnsamblador → Código ASM
```

### Paso 3: Visualizar Estructuras
```
Operaciones → VisualizadorArboles → Diagramas ASCII
```

### Paso 4: Generar Archivos
```
ASM + Visualización → IntegradorSalidaASM → Archivos .asm y .txt
```

---

## 📊 Ejemplo de Salida Visual

### Árbol Binario Inicial
```
╔════════════════════════════════════════╗
║     ÁRBOL BINARIO DE BÚSQUEDA          ║
╚════════════════════════════════════════╝

   ├── [50 : Raíz] h=3
   │   ├── [30 : Izquierda] h=2
   │   │   ├── [20 : Sub-izq] h=1
   │   │   └── [40 : Sub-der-izq] h=1
   │   └── [70 : Derecha] h=2
   │       ├── [60 : Sub-der-izq] h=1
   │       └── [80 : Sub-der] h=1

┌─ Estadísticas del Árbol ─────────────┐
│ Altura: 3
│ Balance Raíz: 0
│ Total de Nodos: 7
│ Nodos Hoja: 4
└──────────────────────────────────────┘
```

---

## 🔧 Compilación y Ejecución

### Compilar todos los archivos:
```bash
javac *.java
```

### Ejecutar el ejemplo:
```bash
java EjemploGeneracionASM
```

### Archivos generados:
- `salida.asm` - Código ensamblador completo
- `salida_visualizacion.txt` - Reporte con visualizaciones

---

## 💻 Código Ensamblador Generado

Ejemplo de código ASM generado:

```asm
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
    
    ; AGREGARNODO: clave=50, valor=raiz EN arbol
    mov rax, [50]
    mov rbx, [raiz]
    call agregar_nodo_arbol
    ; Dibujar árbol después de la inserción
    call dibujar_arbol
    
    ; ... más instrucciones ...
    
    mov rax, 60        ; exit syscall
    mov rdi, 0         ; código de salida
    syscall
```

---

## 🎨 Operaciones Soportadas

### Matemáticas
- `+` Suma
- `-` Resta  
- `*` Multiplicación
- `/` División

### Estructuras
- `INSERTAR` - Inserta clave-valor
- `ELIMINAR` - Elimina por clave
- `APILAR` - Apila un valor
- `DESAPILAR` - Desapila
- `ENCOLAR` - Encola un valor
- `DESENCOLAR` - Desencola

### Consultas
- `TOPE` - Obtiene tope de pila
- `FRENTE` - Obtiene frente de cola
- `VACIA` - Verifica si está vacía

### Control
- `IF_FALSE` - Condicional falso
- `GOTO` - Salto incondicional
- `ETIQUETA` - Marca de salto

---

## 📈 Recorridos del Árbol

### Preorden
```
Salida: 50 30 20 40 70 60 80
Orden: Raiz → Izquierda → Derecha
```

### Inorden
```
Salida: 20 30 40 50 60 70 80
Orden: Izquierda → Raiz → Derecha
Resultado: ÁRBOL ORDENADO
```

### Postorden
```
Salida: 20 40 30 60 80 70 50
Orden: Izquierda → Derecha → Raiz
```

### Por Niveles (BFS)
```
Nivel 0: [50:Raíz]
Nivel 1: [30:Izquierda] [70:Derecha]
Nivel 2: [20:Sub-izq] [40:Sub-der-izq] [60:Sub-der-izq] [80:Sub-der]
```

---

## ⚠️ Detección de Desequilibrio

El visualizador detecta automáticamente:
- **⚠ IZQUIERDO** - Árbol desbalanceado a la izquierda
- **⚠ DERECHO** - Árbol desbalanceado a la derecha

Balance = altura_izquierda - altura_derecha

---

## 📝 Notas Importantes

1. **Validación de Parámetros**: Los métodos INSERTAR y AGREGARNODO verifican que reciban CLAVE y VALOR
2. **Verificación de Vacío**: DESENCOLAR y DESAPILAR verifican que la estructura no esté vacía
3. **Generación de Rutinas**: Se incluyen rutinas para dibujo dinámico en ensamblador
4. **Recorridos Automáticos**: Se generan todos los recorridos posibles

---

## 🚀 Extensiones Futuras

- [ ] Rotaciones para balanceo AVL
- [ ] Visualización de listas enlazadas
- [ ] Visualización de grafos
- [ ] Optimización de código ASM
- [ ] Generación de pseudocódigo
- [ ] Análisis de complejidad

---

## 📞 Contacto y Soporte

Para reportar problemas o sugerencias, revise:
- `GeneradorCGI.java` - Generador intermedio
- `MotorSemantico.java` - Motor semántico
- `Compilador.java` - Clase principal

---

**Última actualización:** Abril 2026
**Versión:** 1.0
**Estado:** ✓ Funcional
