import java.util.*;

/**
 * Visualizador Avanzado de Árboles Binarios
 * 
 * Genera representaciones ASCII de árboles binarios antes y después de operaciones,
 * mostrando cambios, balanceo y estructura.
 */
public class VisualizadorArboles {
    
    /**
     * Clase para representar nodos de árbol
     */
    public static class NodoABB {
        public int clave;
        public String valor;
        public NodoABB izquierda;
        public NodoABB derecha;
        public int altura;

        public NodoABB(int clave, String valor) {
            this.clave = clave;
            this.valor = valor;
            this.izquierda = null;
            this.derecha = null;
            this.altura = 1;
        }
    }

    /**
     * Inserta un nodo en el árbol binario de búsqueda
     */
    public static NodoABB insertar(NodoABB raiz, int clave, String valor) {
        if (raiz == null) {
            return new NodoABB(clave, valor);
        }

        if (clave < raiz.clave) {
            raiz.izquierda = insertar(raiz.izquierda, clave, valor);
        } else if (clave > raiz.clave) {
            raiz.derecha = insertar(raiz.derecha, clave, valor);
        }

        actualizarAltura(raiz);
        return raiz;
    }

    /**
     * Elimina un nodo del árbol
     */
    public static NodoABB eliminar(NodoABB raiz, int clave) {
        if (raiz == null) return null;

        if (clave < raiz.clave) {
            raiz.izquierda = eliminar(raiz.izquierda, clave);
        } else if (clave > raiz.clave) {
            raiz.derecha = eliminar(raiz.derecha, clave);
        } else {
            // Nodo encontrado
            if (raiz.izquierda == null) {
                return raiz.derecha;
            } else if (raiz.derecha == null) {
                return raiz.izquierda;
            }

            // Nodo con dos hijos
            NodoABB minDerecha = encontrarMinimo(raiz.derecha);
            raiz.clave = minDerecha.clave;
            raiz.valor = minDerecha.valor;
            raiz.derecha = eliminar(raiz.derecha, minDerecha.clave);
        }

        if (raiz != null) {
            actualizarAltura(raiz);
        }
        return raiz;
    }

    /**
     * Encuentra el nodo con clave mínima
     */
    private static NodoABB encontrarMinimo(NodoABB nodo) {
        while (nodo.izquierda != null) {
            nodo = nodo.izquierda;
        }
        return nodo;
    }

    /**
     * Actualiza la altura de un nodo
     */
    private static void actualizarAltura(NodoABB nodo) {
        if (nodo != null) {
            int alturaIzq = nodo.izquierda != null ? nodo.izquierda.altura : 0;
            int alturaDer = nodo.derecha != null ? nodo.derecha.altura : 0;
            nodo.altura = 1 + Math.max(alturaIzq, alturaDer);
        }
    }

    /**
     * Obtiene la altura del árbol
     */
    public static int obtenerAltura(NodoABB nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    /**
     * Obtiene el factor de balance de un nodo
     */
    public static int obtenerBalance(NodoABB nodo) {
        if (nodo == null) return 0;
        return obtenerAltura(nodo.izquierda) - obtenerAltura(nodo.derecha);
    }

    /**
     * Dibuja el árbol en formato ASCII con estructura visual
     */
    public static String dibujarArbol(NodoABB raiz) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════╗\n");
        sb.append("║     ÁRBOL BINARIO DE BÚSQUEDA          ║\n");
        sb.append("╚════════════════════════════════════════╝\n\n");

        if (raiz == null) {
            sb.append("   [ ÁRBOL VACÍO ]\n");
        } else {
            List<String> lineas = new ArrayList<>();
            dibujarNodoRecursivo(raiz, "", true, lineas);
            for (String linea : lineas) {
                sb.append(linea).append("\n");
            }

            sb.append("\n┌─ Estadísticas del Árbol ─────────────┐\n");
            sb.append(String.format("│ Altura: %d\n", obtenerAltura(raiz)));
            sb.append(String.format("│ Balance Raíz: %d\n", obtenerBalance(raiz)));
            sb.append(String.format("│ Total de Nodos: %d\n", contarNodos(raiz)));
            sb.append(String.format("│ Nodos Hoja: %d\n", contarHojas(raiz)));
            sb.append("└──────────────────────────────────────┘\n");
        }

        return sb.toString();
    }

    /**
     * Dibuja recursivamente los nodos
     */
    private static void dibujarNodoRecursivo(NodoABB nodo, String prefijo, boolean esUltimo, 
                                            List<String> lineas) {
        if (nodo == null) return;

        String rama = esUltimo ? "└──" : "├──";
        String extension = esUltimo ? "    " : "│   ";
        
        int balance = obtenerBalance(nodo);
        String balanceStr = "";
        if (balance > 1) {
            balanceStr = " (⚠ IZQUIERDO)";
        } else if (balance < -1) {
            balanceStr = " (⚠ DERECHO)";
        }

        String nodoStr = String.format("%s%s [%d : %s] h=%d%s", 
            prefijo, rama, nodo.clave, nodo.valor, nodo.altura, balanceStr);
        lineas.add(nodoStr);

        if (nodo.izquierda != null || nodo.derecha != null) {
            if (nodo.izquierda != null) {
                dibujarNodoRecursivo(nodo.izquierda, prefijo + extension, 
                                    nodo.derecha == null, lineas);
            } else if (nodo.derecha != null) {
                lineas.add(prefijo + extension + "├── [NULL]");
            }

            if (nodo.derecha != null) {
                dibujarNodoRecursivo(nodo.derecha, prefijo + extension, 
                                    true, lineas);
            } else if (nodo.izquierda != null) {
                lineas.add(prefijo + extension + "└── [NULL]");
            }
        }
    }

    /**
     * Cuenta los nodos del árbol
     */
    public static int contarNodos(NodoABB nodo) {
        if (nodo == null) return 0;
        return 1 + contarNodos(nodo.izquierda) + contarNodos(nodo.derecha);
    }

    /**
     * Cuenta las hojas del árbol
     */
    public static int contarHojas(NodoABB nodo) {
        if (nodo == null) return 0;
        if (nodo.izquierda == null && nodo.derecha == null) return 1;
        return contarHojas(nodo.izquierda) + contarHojas(nodo.derecha);
    }

    /**
     * Dibuja dos árboles lado a lado para comparación
     */
    public static String compararArboles(NodoABB antes, NodoABB despues, String operacion) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  OPERACIÓN: %-64s║\n", operacion));
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n");

        sb.append("ANTES:").append(dibujarArbol(antes));
        sb.append("\n" + "═".repeat(70) + "\n");
        sb.append("DESPUÉS:").append(dibujarArbol(despues));

        return sb.toString();
    }

    /**
     * Recorrido en preorden del árbol
     */
    public static String recorridoPreorden(NodoABB nodo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Preorden: ");
        recorridoPreordenRec(nodo, sb);
        return sb.toString();
    }

    private static void recorridoPreordenRec(NodoABB nodo, StringBuilder sb) {
        if (nodo == null) return;
        sb.append(nodo.clave).append(" ");
        recorridoPreordenRec(nodo.izquierda, sb);
        recorridoPreordenRec(nodo.derecha, sb);
    }

    /**
     * Recorrido en inorden del árbol
     */
    public static String recorridoInorden(NodoABB nodo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Inorden: ");
        recorridoInordenRec(nodo, sb);
        return sb.toString();
    }

    private static void recorridoInordenRec(NodoABB nodo, StringBuilder sb) {
        if (nodo == null) return;
        recorridoInordenRec(nodo.izquierda, sb);
        sb.append(nodo.clave).append(" ");
        recorridoInordenRec(nodo.derecha, sb);
    }

    /**
     * Recorrido en postorden del árbol
     */
    public static String recorridoPostorden(NodoABB nodo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Postorden: ");
        recorridoPostordenRec(nodo, sb);
        return sb.toString();
    }

    private static void recorridoPostordenRec(NodoABB nodo, StringBuilder sb) {
        if (nodo == null) return;
        recorridoPostordenRec(nodo.izquierda, sb);
        recorridoPostordenRec(nodo.derecha, sb);
        sb.append(nodo.clave).append(" ");
    }

    /**
     * Busca un nodo por su clave
     */
    public static NodoABB buscar(NodoABB nodo, int clave) {
        if (nodo == null) return null;
        
        if (clave == nodo.clave) {
            return nodo;
        } else if (clave < nodo.clave) {
            return buscar(nodo.izquierda, clave);
        } else {
            return buscar(nodo.derecha, clave);
        }
    }

    /**
     * Dibuja el árbol en formato nivel por nivel (BFS)
     */
    public static String dibujarPorNiveles(NodoABB raiz) {
        if (raiz == null) {
            return "[ ÁRBOL VACÍO ]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════╗\n");
        sb.append("║  VISTA POR NIVELES              ║\n");
        sb.append("╚════════════════════════════════╝\n\n");

        Queue<NodoABB> cola = new LinkedList<>();
        cola.offer(raiz);
        int nivel = 0;

        while (!cola.isEmpty()) {
            int tamanio = cola.size();
            sb.append(String.format("Nivel %d: ", nivel));

            for (int i = 0; i < tamanio; i++) {
                NodoABB nodo = cola.poll();
                sb.append(String.format("[%d:%s] ", nodo.clave, nodo.valor));

                if (nodo.izquierda != null) cola.offer(nodo.izquierda);
                if (nodo.derecha != null) cola.offer(nodo.derecha);
            }

            sb.append("\n");
            nivel++;
        }

        return sb.toString();
    }
}
