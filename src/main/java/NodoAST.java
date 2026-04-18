
import java.util.ArrayList;
import java.util.List;

/*
 ESTA CLASE REPRESENTA UN NODO DEL ÁRBOL DE SINTAXIS ABSTRACTA (AST)
 El AST es la estructura de datos final que produce el Analizador Sintáctico.
 Es como un organigrama jerárquico del código:
 - La raíz es el programa principal.
 - Las ramas son las sentencias (IF, WHILE, CREAR).
 - Las hojas son los detalles finales (nombres de variables, números, operadores).
 
 Esta clase es recursiva, ya que cada nodo contiene una lista de hijos que también son nodos.
 */
public class NodoAST {

    private String valor;
    private String tipo;
    private int linea;
    // Lista de sub-nodos. Al usar una lista (List), un nodo puede tener N hijos (árbol n-ario),
    // a diferencia de un árbol binario que solo tiene 2.
    private List<NodoAST> hijos;

    public NodoAST(String valor, String tipo, int linea) {
        this.valor = valor;
        this.tipo = tipo;
        this.linea = linea;
        this.hijos = new ArrayList<>();
    }

    // Método para conectar nodos. 
    // Así es como el parser va armando la estructura: padre.agregarHijo(hijo)
    public void agregarHijo(NodoAST hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }

    // --- Getters para acceder a la información desde otras clases ---
    public String getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    public List<NodoAST> getHijos() {
        return hijos;
    }
    // =================================================================
    // NUEVOS MÉTODOS AUXILIARES PARA EL GENERADOR DE CÓDIGO INTERMEDIO
    // =================================================================

    // Para Operaciones Binarias (+, -, *, /, =) y Relacionales (>, <, ==)
    // Asume que el hijo 0 es el operando izquierdo y el hijo 1 es el derecho.
    public NodoAST getHijoIzq() {
        return (hijos.size() > 0) ? hijos.get(0) : null;
    }

    public NodoAST getHijoDer() {
        return (hijos.size() > 1) ? hijos.get(1) : null;
    }

    // Para Estructuras de Control (IF, WHILE)
    // Asume el orden: 0 -> Condición, 1 -> Bloque Verdadero, 2 -> Bloque Falso (opcional)
    public NodoAST getCondicion() {
        return getHijoIzq(); // La condición siempre es el primer hijo
    }

    public NodoAST getBloqueIf() {
        return getHijoDer(); // El bloque principal siempre es el segundo hijo
    }

    public NodoAST getBloqueElse() {
        return (hijos.size() > 2) ? hijos.get(2) : null; // El else es el tercer hijo, si existe
    }
}
