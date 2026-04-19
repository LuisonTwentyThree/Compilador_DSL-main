# Ejemplos de Prueba para el Compilador DSL

Estos archivos contienen ejemplos para probar tu compilador y la generación de código intermedio.

## 📁 Archivos de Ejemplo

### 1. **Ejemplo_Simple.txt** ⭐ COMENZAR AQUÍ
- **Descripción**: Ejemplo básico perfecto para inicio
- **Contenido**: Declaraciones simples, operaciones matemáticas básicas
- **Objetivo**: Verificar que el compilador genera código intermedio correctamente
- **Tiempo de prueba**: < 1 minuto

### 2. **Ejemplo_Estructuras.txt**
- **Descripción**: Demostración de estructuras de datos
- **Contenido**: 
  - Pila (APILAR, DESAPILAR, TOPE)
  - Cola (ENCOLAR, DESENCOLAR, FRENTE)
  - Lista Enlazada (INSERTAR, ELIMINAR, TAMANO)
- **Objetivo**: Probar operaciones sobre estructuras de datos

### 3. **Ejemplo_ControlFlujo.txt**
- **Descripción**: Estructuras de control
- **Contenido**:
  - IF condicionales
  - WHILE loops
  - FOR loops
  - DO-WHILE loops
  - Expresiones comparativas (==, !=, <, >, <=, >=)
- **Objetivo**: Verificar generación de etiquetas y saltos condicionales

### 4. **Ejemplo_Avanzado.txt**
- **Descripción**: Estructuras más complejas
- **Contenido**:
  - Tabla Hash (INSERTAR, BUSCAR, ACTUALIZAR)
  - Grafo (AGREGARNODO, AGREGARARISTA)
- **Objetivo**: Probar estructuras avanzadas

### 5. **Ejemplo_Completo_CGI.txt**
- **Descripción**: Ejemplo integral y completo
- **Contenido**: Todas las características del DSL
- **Objetivo**: Prueba exhaustiva del compilador completo

## 🚀 Cómo Usar

1. **Abre tu compilador (Compilador.java)**
2. **Carga un archivo**: Archivo → Abrir
3. **Selecciona uno de los ejemplos**
4. **Ejecuta la compilación**: Botón "Compilar"
5. **Verifica el resultado**:
   - Tab "Léxico": Tokens generados
   - Tab "Sintáctico": Árbol sintáctico
   - Tab "Semántico": Tabla de símbolos
   - Tab "Código Intermedio": Cuádruplos generados

## ✅ Qué Esperar

El compilador debería:
- ✓ Reconocer todos los tokens (Léxico)
- ✓ Construir el árbol sintáctico correcto (Sintáctico)
- ✓ Validar tipos y operaciones (Semántico)
- ✓ Generar cuádruplos de 3 direcciones (Código Intermedio)

**Ejemplo de salida esperada** (Código Intermedio):
```
--- CÓDIGO INTERMEDIO (TRES DIRECCIONES) ---

T1 = 10
T2 = 5
T3 = T1 + T2
PRINT T3
```

## 🐛 Debugging

Si encuentras errores:
1. Revisa la pestaña "Errores" para ver qué falló
2. Verifica la línea indicada en el error
3. Consulta los ejemplos correctos de la carpeta

## 📝 Notas Importantes

- **NO incluir comentarios anidados**: El DSL no soporta `/* */`
- **Punto y coma obligatorio**: Todas las sentencias necesitan `;`
- **Tipos de datos**: NUMERO, TEXTO, PILA, COLA, LISTA_ENLAZADA, TABLA_HASH, GRAFO, ARBOL_BINARIO, BICOLA, LISTA_DOBLE_ENLAZADA
- **Estructura de datos con tamaño**: `CREAR PILA nombreVariable TAMANO 100;`

## 📚 Estructura de Ejemplo Mínimo

```dsl
CREAR NUMERO x = 10;
CREAR NUMERO y = 20;
CREAR NUMERO resultado = 0;

resultado = x + y;

MOSTRAR resultado;
```

¡Diviértete compilando! 🎉
