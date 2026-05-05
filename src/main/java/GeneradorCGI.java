import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Generador de Código Intermedio (3 Direcciones)
 * 
 * Convierte el Árbol Sintáctico Abstracto (AST) a código de tres direcciones
 * usando cuádruplos para todas las operaciones del lenguaje DSL.
 */
public class GeneradorCGI {
    private List<Cuadruplo> codigo;
    private int contadorTemporales;
    private int contadorEtiquetas;
    private Stack<String> pilaTemporales;

    public GeneradorCGI() {
        this.codigo = new ArrayList<>();
        this.contadorTemporales = 1;
        this.contadorEtiquetas = 1;
        this.pilaTemporales = new Stack<>();
    }

    /**
     * Genera un nuevo temporal único
     */
    private String nuevoTemporal() {
        return "T" + (contadorTemporales++);
    }

    /**
     * Genera una nueva etiqueta única
     */
    private String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }

    /**
     * Agrega un cuádruplo a la lista de código generado
     */
    private void agregar(String op, String arg1, String arg2, String res) {
        codigo.add(new Cuadruplo(op, arg1, arg2, res));
    }

    /**
     * Punto de entrada para la generación de código
     */
    public List<Cuadruplo> generar(NodoAST raiz) {
        if (raiz != null) {
            recorrerNodo(raiz);
        }
        return codigo;
    }

    /**
     * Imprime el código generado en consola
     */
    public void imprimirCodigo() {
        System.out.println("--- CÓDIGO INTERMEDIO (3 Direcciones) ---");
        for (Cuadruplo c : codigo) {
            System.out.println(c.toString());
        }
    }

    /**
     * Método principal que recorre el AST y genera código intermedio
     * 
     * Maneja todos los tipos de nodos del lenguaje DSL
     */
    private String recorrerNodo(NodoAST nodo) {
        if (nodo == null) {
            return "";
        }

        String tipo = nodo.getTipo();
        String valor = nodo.getValor();

        // Normalizar valores nulos
        if (tipo == null) tipo = "";
        if (valor == null) valor = "";

        tipo = tipo.trim().toUpperCase();
        valor = valor.trim();

        // ===== NODOS RAÍZ Y BLOQUES =====
        if (tipo.equals("RAIZ") || tipo.equals("LISTA SENTENCIAS") || tipo.equals("BLOQUE_CODIGO")) {
            if (nodo.getHijos() != null) {
                for (NodoAST hijo : nodo.getHijos()) {
                    recorrerNodo(hijo);
                }
            }
            return "";
        }

        // ===== LITERALES Y IDENTIFICADORES =====
        if (tipo.equals("NUMERO") || tipo.equals("LITERAL_NUMERICA")) {
            return valor;
        }
        if (tipo.equals("CADENA") || tipo.equals("LITERAL_CADENA")) {
            return valor;
        }
        if (tipo.equals("BOOLEANO")) {
            return valor;
        }
        if (tipo.equals("IDENTIFICADOR") || tipo.equals("ID") || 
            tipo.equals("ID_VAR") || tipo.equals("ID_ESTRUCTURA")) {
            return valor;
        }

        // ===== DECLARACIÓN DE VARIABLES =====
        if (tipo.equals("DECLARACION")) {
            if (nodo.getHijos() == null || nodo.getHijos().size() < 2) {
                return "";
            }

            String tipoVar = nodo.getHijos().get(0).getValor().toUpperCase();
            String idVar = nodo.getHijos().get(1).getValor();

            // Si hay una inicialización o tamaño
            if (nodo.getHijos().size() > 2) {
                NodoAST nodoVal = nodo.getHijos().get(2);
                String tipoValNodo = nodoVal.getTipo().toUpperCase();

                // Es una estructura de datos con TAMANO
                if (tipoValNodo.equals("TAMANO")) {
                    String tamano = nodoVal.getValor();
                    agregar("ALLOC", tamano, "", idVar);
                } else {
                    // Es una inicialización de variable primitiva
                    String dirVal = recorrerNodo(nodoVal);
                    agregar("=", dirVal, "", idVar);
                }
            }
            return "";
        }

        // ===== ASIGNACIÓN =====
        if (tipo.equals("ASIGNACION") || tipo.equals("ACTUALIZACION")) {
            if (nodo.getHijos().size() >= 2) {
                String idAsig = recorrerNodo(nodo.getHijos().get(0));
                String dirAsig = recorrerNodo(nodo.getHijos().get(1));
                agregar("=", dirAsig, "", idAsig);
                return idAsig;
            }
            return "";
        }

        // ===== OPERACIONES MATEMÁTICAS =====
        if (tipo.equals("OPERACION")) {
            // Verificar si es una operación matemática
            if (valor.equals("+") || valor.equals("-") || valor.equals("*") || valor.equals("/") || valor.equals("%")) {
                return procesarOperacionMatematica(nodo, valor);
            }
            // Si es BORRAR
            else if (valor.equalsIgnoreCase("BORRAR")) {
                if (nodo.getHijos() != null && nodo.getHijos().size() > 0) {
                    String idEst = recorrerNodo(nodo.getHijos().get(0));
                    agregar("FREE", "", "", idEst);
                }
                return "";
            }
            // Es una operación sobre estructura de datos
            else {
                return procesarOperacionEstructura(nodo, valor);
            }
        }

        // ===== PROPIEDADES DE ESTRUCTURAS =====
        if (tipo.equals("PROPIEDAD")) {
            // TOPE EN pila, FRENTE EN cola, etc.
            if (nodo.getHijos() != null && nodo.getHijos().size() > 0) {
                String idEst = recorrerNodo(nodo.getHijos().get(0));
                String tRes = nuevoTemporal();
                agregar(valor.toUpperCase(), idEst, "", tRes);
                return tRes;
            }
            return "";
        }

        // ===== CONDICIONES Y COMPARACIONES =====
        if (tipo.equals("CONDICION")) {
            // Comparaciones: ==, !=, <, >, <=, >=
            if (nodo.getHijos().size() >= 2) {
                String cIzq = recorrerNodo(nodo.getHijos().get(0));
                String cDer = recorrerNodo(nodo.getHijos().get(1));
                String tCond = nuevoTemporal();
                agregar(valor, cIzq, cDer, tCond);
                return tCond;
            }
            return "";
        }

        if (tipo.equals("CONDICION_PROPIEDAD")) {
            // VACIA EN cola, LLENA EN pila, etc.
            if (nodo.getHijos() != null && nodo.getHijos().size() > 0) {
                String idEst = recorrerNodo(nodo.getHijos().get(0));
                String tCond = nuevoTemporal();
                agregar(valor.toUpperCase(), idEst, "", tCond);
                return tCond;
            }
            return "";
        }

        // ===== OPERACIONES DE SALIDA =====
        if (tipo.equals("SALIDA")) {
            // MOSTRAR expr
            if (nodo.getHijos() != null && nodo.getHijos().size() > 0) {
                String exprSalida = recorrerNodo(nodo.getHijos().get(0));
                agregar("PRINT", exprSalida, "", "");
            }
            return "";
        }

        // ===== CONTROL DE FLUJO: IF =====
        if (tipo.equals("CONTROL")) {
            return procesarControlFlujo(nodo);
        }

        // ===== BUCLES: WHILE, FOR, DO-WHILE =====
        if (tipo.equals("BUCLE")) {
            return procesarBucle(nodo);
        }

        // ===== BLOQUE DE CÓDIGO =====
        if (tipo.equals("BLOQUE")) {
            if (nodo.getHijos() != null) {
                for (NodoAST hijo : nodo.getHijos()) {
                    recorrerNodo(hijo);
                }
            }
            return "";
        }

        // ===== MANEJO POR DEFECTO =====
        // Para nodos desconocidos, intentamos procesar sus hijos
        if (nodo.getHijos() != null) {
            String resultado = "";
            for (NodoAST hijo : nodo.getHijos()) {
                resultado = recorrerNodo(hijo);
            }
            return resultado;
        }

        return "";
    }

    /**
     * Procesa operaciones matemáticas (+, -, *, /, %)
     */
    private String procesarOperacionMatematica(NodoAST nodo, String operador) {
        if (nodo.getHijos() == null || nodo.getHijos().size() < 2) {
            return "";
        }

        String izq = recorrerNodo(nodo.getHijos().get(0));
        String der = recorrerNodo(nodo.getHijos().get(1));
        String tRes = nuevoTemporal();
        
        agregar(operador, izq, der, tRes);
        return tRes;
    }

    /**
     * Procesa operaciones sobre estructuras de datos
     * Ej: APILAR, INSERTAR, ELIMINAR, etc.
     * 
     * Para DESENCOLAR y DESAPILAR, verifica que la estructura no esté vacía
     * Para INSERTAR en listas enlazadas y árboles binarios, requiere CLAVE y VALOR
     */
    private String procesarOperacionEstructura(NodoAST nodo, String operacion) {
        if (nodo.getHijos() == null || nodo.getHijos().isEmpty()) {
            return "";
        }

        int numHijos = nodo.getHijos().size();
        String operUpper = operacion.toUpperCase();

        // El último hijo suele ser la estructura (por diseño del parser)
        // Pero verificamos antes
        String idEstructura = "";
        int idxEstructura = -1;

        // Buscar el ID de la estructura
        for (int i = numHijos - 1; i >= 0; i--) {
            NodoAST hijo = nodo.getHijos().get(i);
            String tipoHijo = hijo.getTipo().toUpperCase();
            if (tipoHijo.equals("ID_ESTRUCTURA") || tipoHijo.equals("ID")) {
                idEstructura = hijo.getValor();
                idxEstructura = i;
                break;
            }
        }

        // Si no encontramos estructura, usamos el último hijo
        if (idxEstructura == -1 && numHijos > 0) {
            idEstructura = recorrerNodo(nodo.getHijos().get(numHijos - 1));
            idxEstructura = numHijos - 1;
        }

        // Procesar argumentos (excluyendo la estructura)
        if (idxEstructura == -1) {
            return ""; // No encontramos estructura
        }

        // Validar operaciones que requieren exactamente 2 argumentos
        if ((operUpper.equals("INSERTAR") || operUpper.equals("AGREGARNODO")) && idxEstructura < 2) {
            agregar("ERROR", "Se requieren CLAVE y VALOR para", operUpper, idEstructura);
            return "";
        }

        // Procesamos según el número de argumentos
        if (idxEstructura == 0) {
            // Solo la estructura (DESAPILAR EN pila, DESENCOLAR EN cola)
            // Para operaciones que extraen datos, verificar que no esté vacía
            if (operUpper.equals("DESAPILAR") || operUpper.equals("DESENCOLAR")) {
                String tVerificacion = nuevoTemporal();
                agregar("VACIA", idEstructura, "", tVerificacion);
                String lContinuar = nuevaEtiqueta();
                agregar("IF_FALSE", tVerificacion, "GOTO", lContinuar);
                agregar("ERROR", "Estructura vacía", "", "");
                agregar("ETIQUETA", "", "", lContinuar);
            }
            agregar(operUpper, "", "", idEstructura);
        } else if (idxEstructura == 1) {
            // Un argumento + estructura (APILAR 5 EN pila, ELIMINAR_FINAL EN lista)
            String arg1 = recorrerNodo(nodo.getHijos().get(0));
            agregar(operUpper, arg1, "", idEstructura);
        } else if (idxEstructura >= 2) {
            // Dos o más argumentos + estructura (INSERTAR clave valor EN lista, AGREGARNODO clave valor EN arbol)
            String arg1 = recorrerNodo(nodo.getHijos().get(0));
            String arg2 = recorrerNodo(nodo.getHijos().get(1));
            agregar(operUpper, arg1, arg2, idEstructura);
        }

        return "";
    }

    /**
     * Procesa control de flujo (IF, IF-ELSE)
     */
    private String procesarControlFlujo(NodoAST nodo) {
        if (nodo.getHijos() == null || nodo.getHijos().isEmpty()) {
            return "";
        }

        String condicion = recorrerNodo(nodo.getHijos().get(0));
        String lFalso = nuevaEtiqueta();
        String lFinSi = nuevaEtiqueta();

        // Generar: IF_FALSE condicion GOTO lFalso
        agregar("IF_FALSE", condicion, "GOTO", lFalso);

        // Procesar el bloque del IF
        if (nodo.getHijos().size() > 1) {
            recorrerNodo(nodo.getHijos().get(1));
        }

        // Si hay ELSE
        if (nodo.getHijos().size() > 2) {
            agregar("GOTO", "", "", lFinSi);
            agregar("ETIQUETA", "", "", lFalso);
            recorrerNodo(nodo.getHijos().get(2));
            agregar("ETIQUETA", "", "", lFinSi);
        } else {
            agregar("ETIQUETA", "", "", lFalso);
        }

        return "";
    }

    /**
     * Procesa bucles (WHILE, FOR, DO-WHILE)
     */
    private String procesarBucle(NodoAST nodo) {
        if (nodo.getHijos() == null || nodo.getHijos().isEmpty()) {
            return "";
        }

        String valorBucle = nodo.getValor().toUpperCase();

        switch (valorBucle) {
            case "WHILE":
                return procesarWhile(nodo);

            case "FOR":
                return procesarFor(nodo);

            case "DO":
            case "DO_WHILE":
                return procesarDoWhile(nodo);

            default:
                return "";
        }
    }

    /**
     * Procesa bucles WHILE
     */
    private String procesarWhile(NodoAST nodo) {
        String lInicio = nuevaEtiqueta();
        String lFin = nuevaEtiqueta();

        agregar("ETIQUETA", "", "", lInicio);

        // Procesar condición
        String condicion = recorrerNodo(nodo.getHijos().get(0));
        agregar("IF_FALSE", condicion, "GOTO", lFin);

        // Procesar cuerpo
        if (nodo.getHijos().size() > 1) {
            recorrerNodo(nodo.getHijos().get(1));
        }

        agregar("GOTO", "", "", lInicio);
        agregar("ETIQUETA", "", "", lFin);

        return "";
    }

    /**
     * Procesa bucles FOR
     * Estructura esperada: INICIALIZACIÓN, CONDICIÓN, INCREMENTO, CUERPO
     */
    private String procesarFor(NodoAST nodo) {
        if (nodo.getHijos().size() < 4) {
            return "";
        }

        String lInicio = nuevaEtiqueta();
        String lFin = nuevaEtiqueta();

        // Inicialización: i = 0
        recorrerNodo(nodo.getHijos().get(0));

        agregar("ETIQUETA", "", "", lInicio);

        // Condición: i < 10
        String condicion = recorrerNodo(nodo.getHijos().get(1));
        agregar("IF_FALSE", condicion, "GOTO", lFin);

        // Cuerpo (generalmente en posición 3)
        recorrerNodo(nodo.getHijos().get(3));

        // Incremento: i = i + 1 (posición 2)
        recorrerNodo(nodo.getHijos().get(2));

        agregar("GOTO", "", "", lInicio);
        agregar("ETIQUETA", "", "", lFin);

        return "";
    }

    /**
     * Procesa bucles DO-WHILE
     */
    private String procesarDoWhile(NodoAST nodo) {
        String lInicio = nuevaEtiqueta();

        agregar("ETIQUETA", "", "", lInicio);

        // Procesar cuerpo
        if (nodo.getHijos().size() > 0) {
            recorrerNodo(nodo.getHijos().get(0));
        }

        // Procesar condición
        if (nodo.getHijos().size() > 1) {
            String condicion = recorrerNodo(nodo.getHijos().get(1));
            agregar("IF_TRUE", condicion, "GOTO", lInicio);
        }

        return "";
    }
}