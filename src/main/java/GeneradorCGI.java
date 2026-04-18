import java.util.ArrayList;
import java.util.List;

public class GeneradorCGI {
    private List<Cuadruplo> codigo;
    private int contadorTemporales;
    private int contadorEtiquetas;

    public GeneradorCGI() {
        this.codigo = new ArrayList<>();
        this.contadorTemporales = 1;
        this.contadorEtiquetas = 1;
    }

    private String nuevoTemporal() {
        return "T" + (contadorTemporales++);
    }

    private String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }

    private void agregar(String op, String arg1, String arg2, String res) {
        codigo.add(new Cuadruplo(op, arg1, arg2, res));
    }

    public List<Cuadruplo> generar(NodoAST raiz) {
        recorrerNodo(raiz);
        return codigo;
    }

    public void imprimirCodigo() {
        System.out.println("--- CÓDIGO INTERMEDIO (3 Direcciones) ---");
        for (Cuadruplo c : codigo) {
            System.out.println(c.toString());
        }
    }

    // --- RECORRIDO DEL ÁRBOL ADAPTADO A TU ANALIZADOR SINTÁCTICO ---
    private String recorrerNodo(NodoAST nodo) {
        if (nodo == null) return "";

        String tipo = nodo.getTipo(); 
        String valor = nodo.getValor();

        // Manejo nulos por seguridad
        if (tipo == null) tipo = "";
        if (valor == null) valor = "";

        switch (tipo) {
            case "RAIZ":
            case "Lista Sentencias": // Equivalente a BLOQUE_CODIGO en tu AST
                for (NodoAST hijo : nodo.getHijos()) {
                    recorrerNodo(hijo);
                }
                return "";

            case "NUMERO":
            case "CADENA":
            case "BOOLEANO":
            case "IDENTIFICADOR":
            case "ID":
            case "ID_VAR":
            case "ID_ESTRUCTURA":
            case "GRAFO":
                return valor;

            case "DECLARACION":
                // hijos: 0=TIPO, 1=ID, 2=(opcional) EXPRESION o TAMANO
                String idVar = nodo.getHijos().get(1).getValor();
                if (nodo.getHijos().size() > 2) {
                    NodoAST exprNodo = nodo.getHijos().get(2);
                    if (exprNodo.getTipo().equals("TAMANO")) {
                        // CREAR PILA miPila TAMANO 100;
                        agregar("ALLOC", exprNodo.getValor(), "", idVar);
                    } else {
                        // CREAR NUMERO x = 10;
                        String dirExpr = recorrerNodo(exprNodo);
                        agregar("=", dirExpr, "", idVar);
                    }
                }
                return "";

            case "ASIGNACION":
            case "ACTUALIZACION": // Usado en los FOR
                String asigId = recorrerNodo(nodo.getHijos().get(0));
                String asigDir = recorrerNodo(nodo.getHijos().get(1));
                agregar("=", asigDir, "", asigId);
                return asigId;

            case "OPERACION":
                // Tu AST agrupa sumas matemáticas y comandos de estructuras de datos aquí
                // Discriminamos por el valor del nodo: si es +, -, *, / es matemática
                if (valor.equals("+") || valor.equals("-") || valor.equals("*") || valor.equals("/")) {
                    String tIzq = recorrerNodo(nodo.getHijos().get(0));
                    String tDer = recorrerNodo(nodo.getHijos().get(1));
                    String tRes = nuevoTemporal();
                    agregar(valor, tIzq, tDer, tRes);
                    return tRes;
                } 
                else if (valor.equals("BORRAR")) {
                    String idEstructura = recorrerNodo(nodo.getHijos().get(0));
                    agregar("FREE", "", "", idEstructura);
                    return "";
                }
                else {
                    // ES UNA OPERACIÓN DE ESTRUCTURA DE DATOS (APILAR, INSERTAR, etc.)
                    int numHijos = nodo.getHijos().size();
                    // El último hijo siempre es la estructura en tu AST
                    String idEstructura = recorrerNodo(nodo.getHijos().get(numHijos - 1)); 

                    if (numHijos == 2) {
                        // Ej: APILAR 5 EN miPila (hijo0 = 5, hijo1 = miPila)
                        String arg1 = recorrerNodo(nodo.getHijos().get(0));
                        agregar(valor, arg1, "", idEstructura);
                    } else if (numHijos == 3) {
                        // Ej: AGREGARNODO 1 100 EN miGrafo (hijo0 = 1, hijo1 = 100, hijo2 = miGrafo)
                        String arg1 = recorrerNodo(nodo.getHijos().get(0));
                        String arg2 = recorrerNodo(nodo.getHijos().get(1));
                        agregar(valor, arg1, arg2, idEstructura);
                    } else {
                        // Ej: DESAPILAR EN miPila (hijo0 = miPila)
                        agregar(valor, "", "", idEstructura);
                    }
                    return "";
                }

            case "PROPIEDAD":
                // Ej: TOPE EN miPila
                String estPropiedad = recorrerNodo(nodo.getHijos().get(0));
                String tResProp = nuevoTemporal();
                agregar(valor, estPropiedad, "", tResProp);
                return tResProp;

            case "Salida":
                // Tu AST llama "Salida" al MOSTRAR
                String exprSalida = recorrerNodo(nodo.getHijos().get(0));
                agregar("PRINT", exprSalida, "", "");
                return "";

            case "CONDICION": // Operadores lógicos (==, !=, <, >)
                String cIzq = recorrerNodo(nodo.getHijos().get(0));
                String cDer = recorrerNodo(nodo.getHijos().get(1));
                String tCond = nuevoTemporal();
                agregar(valor, cIzq, cDer, tCond); 
                return tCond; // Retornamos el temporal que guarda el booleano

            case "CONDICION_PROPIEDAD":
                // Ej: VACIA EN miCola
                String estCondProp = recorrerNodo(nodo.getHijos().get(0));
                String tCondProp = nuevoTemporal();
                agregar(valor, estCondProp, "", tCondProp);
                return tCondProp;

            case "CONTROL": 
                // IF (Tu AST no implementa ELSE según veo, así que es un IF simple)
                String condIf = recorrerNodo(nodo.getHijos().get(0));
                String lFalso = nuevaEtiqueta();
                agregar("IF_FALSE", condIf, "GOTO", lFalso);
                recorrerNodo(nodo.getHijos().get(1)); // Bloque IF
                agregar("ETIQUETA", "", "", lFalso);
                return "";

            case "Bucle": 
                if (valor.equals("WHILE")) {
                    String lInicioW = nuevaEtiqueta();
                    String lFinW = nuevaEtiqueta();
                    agregar("ETIQUETA", "", "", lInicioW);
                    String condW = recorrerNodo(nodo.getHijos().get(0));
                    agregar("IF_FALSE", condW, "GOTO", lFinW);
                    recorrerNodo(nodo.getHijos().get(1)); // Bloque
                    agregar("GOTO", "", "", lInicioW);
                    agregar("ETIQUETA", "", "", lFinW);
                } 
                else if (valor.equals("FOR")) {
                    recorrerNodo(nodo.getHijos().get(0)); // Inicialización (i = 0)
                    String lInicioF = nuevaEtiqueta();
                    String lFinF = nuevaEtiqueta();
                    agregar("ETIQUETA", "", "", lInicioF);
                    String condF = recorrerNodo(nodo.getHijos().get(1)); // Condición (i < 10)
                    agregar("IF_FALSE", condF, "GOTO", lFinF);
                    recorrerNodo(nodo.getHijos().get(3)); // Bloque (Atención: tu AST lo guarda en la pos 3)
                    recorrerNodo(nodo.getHijos().get(2)); // Actualización (i = i + 1)
                    agregar("GOTO", "", "", lInicioF);
                    agregar("ETIQUETA", "", "", lFinF);
                } 
                else if (valor.equals("DO_WHILE")) {
                    String lInicioD = nuevaEtiqueta();
                    agregar("ETIQUETA", "", "", lInicioD);
                    recorrerNodo(nodo.getHijos().get(0)); // Bloque
                    String condD = recorrerNodo(nodo.getHijos().get(1)); // Condición
                    agregar("IF_TRUE", condD, "GOTO", lInicioD); // Si es cierto, regresa arriba
                }
                return "";

            default:
                // Si encontramos un nodo desconocido, intentamos bajar por sus hijos para no rompernos
                if (nodo.getHijos() != null) {
                    for (NodoAST hijo : nodo.getHijos()) {
                        recorrerNodo(hijo);
                    }
                }
                return "";
        }
    }
}