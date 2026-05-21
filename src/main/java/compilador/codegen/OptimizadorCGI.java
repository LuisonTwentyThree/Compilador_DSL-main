package compilador.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compilador.core.Cuadruplo;

/**
 * Optimizador conservador de codigo intermedio basado en cuadruplos.
 *
 * Aplica:
 * - plegado de constantes aritmeticas y comparaciones
 * - simplificaciones algebraicas seguras
 * - propagacion de copias/constantes sin cruzar saltos ni etiquetas
 * - simplificacion de saltos con condiciones constantes
 * - eliminacion de saltos redundantes e instrucciones inalcanzables simples
 * - eliminacion de codigo muerto solo para temporales
 */
public class OptimizadorCGI {

    private static final int MAX_PASADAS = 6;

    public List<Cuadruplo> optimizar(List<Cuadruplo> codigo) {
        if (codigo == null) {
            return Collections.emptyList();
        }

        List<Cuadruplo> actual = copiarCodigo(codigo);

        for (int i = 0; i < MAX_PASADAS; i++) {
            String antes = firma(actual);

            actual = simplificarOperaciones(actual);
            actual = propagarCopiasYConstantes(actual);
            actual = simplificarOperaciones(actual);
            actual = simplificarSaltos(actual);
            actual = eliminarCodigoInalcanzable(actual);
            actual = eliminarCodigoMuerto(actual);

            if (antes.equals(firma(actual))) {
                break;
            }
        }

        return actual;
    }

    private List<Cuadruplo> simplificarOperaciones(List<Cuadruplo> codigo) {
        List<Cuadruplo> salida = new ArrayList<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);
            String op = c.operador;

            if ("=".equals(op) && mismoValor(c.resultado, c.argumento1)) {
                continue;
            }

            if (isArithmeticOperator(op)) {
                Cuadruplo simplificado = simplificarAritmetica(c);
                if (simplificado != null) {
                    salida.add(simplificado);
                    continue;
                }
            }

            if (isComparisonOperator(op) && isNumeric(c.argumento1) && isNumeric(c.argumento2)) {
                boolean resultado = calcularComparacion(op, Long.parseLong(c.argumento1), Long.parseLong(c.argumento2));
                salida.add(new Cuadruplo("=", resultado ? "1" : "0", "", c.resultado));
                continue;
            }

            if (("IF_FALSE".equals(op) || "IF_TRUE".equals(op)) && isNumeric(c.argumento1)) {
                boolean condicion = Long.parseLong(c.argumento1) != 0;
                if (("IF_FALSE".equals(op) && !condicion) || ("IF_TRUE".equals(op) && condicion)) {
                    salida.add(new Cuadruplo("GOTO", "", "", c.resultado));
                }
                continue;
            }

            salida.add(c);
        }

        return salida;
    }

    private Cuadruplo simplificarAritmetica(Cuadruplo c) {
        String op = c.operador;
        String a = c.argumento1;
        String b = c.argumento2;
        String r = c.resultado;

        if (isNumeric(a) && isNumeric(b)) {
            Long resultado = calcularAritmetica(op, Long.parseLong(a), Long.parseLong(b));
            if (resultado != null) {
                return new Cuadruplo("=", Long.toString(resultado), "", r);
            }
        }

        if ("+".equals(op)) {
            if (esCero(b)) return new Cuadruplo("=", a, "", r);
            if (esCero(a)) return new Cuadruplo("=", b, "", r);
        }

        if ("-".equals(op)) {
            if (esCero(b)) return new Cuadruplo("=", a, "", r);
            if (mismoValor(a, b)) return new Cuadruplo("=", "0", "", r);
        }

        if ("*".equals(op)) {
            if (esCero(a) || esCero(b)) return new Cuadruplo("=", "0", "", r);
            if (esUno(b)) return new Cuadruplo("=", a, "", r);
            if (esUno(a)) return new Cuadruplo("=", b, "", r);
        }

        if ("/".equals(op)) {
            if (esUno(b)) return new Cuadruplo("=", a, "", r);
            if (esCero(a) && !esCero(b)) return new Cuadruplo("=", "0", "", r);
        }

        return null;
    }

    private List<Cuadruplo> propagarCopiasYConstantes(List<Cuadruplo> codigo) {
        Map<String, String> reemplazos = new HashMap<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);

            if ("ETIQUETA".equals(c.operador)) {
                reemplazos.clear();
                salida.add(c);
                continue;
            }

            c.argumento1 = resolver(c.argumento1, reemplazos);
            c.argumento2 = resolver(c.argumento2, reemplazos);

            salida.add(c);

            if ("GOTO".equals(c.operador) || c.operador.startsWith("IF")) {
                reemplazos.clear();
                continue;
            }

            if (esEfectoSecundario(c)) {
                invalidarResultado(c.resultado, reemplazos);
                if (esBarreraPropagacion(c)) {
                    reemplazos.clear();
                }
                continue;
            }

            if (esIdentificador(c.resultado)) {
                invalidarResultado(c.resultado, reemplazos);
            }

            if ("=".equals(c.operador) && esIdentificador(c.resultado) && esValorPropagable(c.argumento1)) {
                reemplazos.put(c.resultado, c.argumento1);
            }
        }

        return salida;
    }

    private List<Cuadruplo> simplificarSaltos(List<Cuadruplo> codigo) {
        List<Cuadruplo> redirigido = redirigirEtiquetasConsecutivas(codigo);
        List<Cuadruplo> salida = new ArrayList<>();

        for (int i = 0; i < redirigido.size(); i++) {
            Cuadruplo c = redirigido.get(i);
            if ("GOTO".equals(c.operador) && i + 1 < redirigido.size()) {
                Cuadruplo siguiente = redirigido.get(i + 1);
                if ("ETIQUETA".equals(siguiente.operador) && mismoValor(c.resultado, siguiente.resultado)) {
                    continue;
                }
            }
            salida.add(c);
        }

        return salida;
    }

    private List<Cuadruplo> redirigirEtiquetasConsecutivas(List<Cuadruplo> codigo) {
        Map<String, String> reemplazoEtiquetas = new HashMap<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (int i = 0; i < codigo.size(); i++) {
            Cuadruplo c = codigo.get(i);
            if (!"ETIQUETA".equals(c.operador)) {
                salida.add(copiar(c));
                continue;
            }

            String etiquetaCanonica = c.resultado;
            salida.add(copiar(c));

            int j = i + 1;
            while (j < codigo.size() && "ETIQUETA".equals(codigo.get(j).operador)) {
                reemplazoEtiquetas.put(codigo.get(j).resultado, etiquetaCanonica);
                j++;
            }
            i = j - 1;
        }

        if (reemplazoEtiquetas.isEmpty()) {
            return salida;
        }

        List<Cuadruplo> redirigido = new ArrayList<>();
        for (Cuadruplo c : salida) {
            Cuadruplo copia = copiar(c);
            if (("GOTO".equals(copia.operador) || copia.operador.startsWith("IF"))
                    && reemplazoEtiquetas.containsKey(copia.resultado)) {
                copia.resultado = reemplazoEtiquetas.get(copia.resultado);
            }
            redirigido.add(copia);
        }

        return redirigido;
    }

    private List<Cuadruplo> eliminarCodigoInalcanzable(List<Cuadruplo> codigo) {
        List<Cuadruplo> salida = new ArrayList<>();
        boolean inalcanzable = false;

        for (Cuadruplo c : codigo) {
            if (c == null) {
                continue;
            }

            if ("ETIQUETA".equals(c.operador)) {
                inalcanzable = false;
                salida.add(c);
                continue;
            }

            if (inalcanzable) {
                continue;
            }

            salida.add(c);
            if ("GOTO".equals(c.operador)) {
                inalcanzable = true;
            }
        }

        return salida;
    }

    private List<Cuadruplo> eliminarCodigoMuerto(List<Cuadruplo> codigo) {
        Set<String> usados = new HashSet<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (int i = codigo.size() - 1; i >= 0; i--) {
            Cuadruplo c = codigo.get(i);
            if (c == null) {
                continue;
            }

            boolean conservar = esControl(c) || esEfectoSecundario(c)
                    || c.resultado == null || usados.contains(c.resultado);

            if (conservar) {
                salida.add(0, c);
                if (esIdentificador(c.resultado)) {
                    usados.remove(c.resultado);
                }
                agregarUso(c.argumento1, usados);
                agregarUso(c.argumento2, usados);
            }
        }

        return salida;
    }

    private Long calcularAritmetica(String op, long v1, long v2) {
        switch (op) {
            case "+": return v1 + v2;
            case "-": return v1 - v2;
            case "*": return v1 * v2;
            case "/": return v2 == 0 ? null : v1 / v2;
            default: return null;
        }
    }

    private boolean calcularComparacion(String op, long v1, long v2) {
        switch (op) {
            case "<": return v1 < v2;
            case ">": return v1 > v2;
            case "==": return v1 == v2;
            case "!=": return v1 != v2;
            case "<=": return v1 <= v2;
            case ">=": return v1 >= v2;
            default: return false;
        }
    }

    private void invalidarResultado(String resultado, Map<String, String> reemplazos) {
        if (!esIdentificador(resultado)) {
            return;
        }
        reemplazos.remove(resultado);
        reemplazos.values().removeIf(resultado::equals);
    }

    private void agregarUso(String valor, Set<String> usados) {
        if (esIdentificador(valor)) {
            usados.add(valor);
        }
    }

    private String resolver(String valor, Map<String, String> reemplazos) {
        String actual = valor;
        Set<String> visitados = new HashSet<>();
        while (esIdentificador(actual) && reemplazos.containsKey(actual) && visitados.add(actual)) {
            actual = reemplazos.get(actual);
        }
        return actual;
    }

    private List<Cuadruplo> copiarCodigo(List<Cuadruplo> codigo) {
        List<Cuadruplo> copia = new ArrayList<>();
        for (Cuadruplo c : codigo) {
            if (c != null) {
                copia.add(copiar(c));
            }
        }
        return copia;
    }

    private Cuadruplo copiar(Cuadruplo c) {
        return new Cuadruplo(c.operador, c.argumento1, c.argumento2, c.resultado);
    }

    private String firma(List<Cuadruplo> codigo) {
        StringBuilder sb = new StringBuilder();
        for (Cuadruplo c : codigo) {
            sb.append(valor(c.operador)).append('|')
                    .append(valor(c.argumento1)).append('|')
                    .append(valor(c.argumento2)).append('|')
                    .append(valor(c.resultado)).append('\n');
        }
        return sb.toString();
    }

    private boolean esControl(Cuadruplo c) {
        return c != null && c.operador != null
                && ("ETIQUETA".equals(c.operador) || "GOTO".equals(c.operador) || c.operador.startsWith("IF"));
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("-?\\d+");
    }

    private boolean isArithmeticOperator(String op) {
        return "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op);
    }

    private boolean isComparisonOperator(String op) {
        return "<".equals(op) || ">".equals(op) || "==".equals(op) || "!=".equals(op)
                || "<=".equals(op) || ">=".equals(op);
    }

    private boolean esValorPropagable(String valor) {
        return !vacio(valor) && (isNumeric(valor) || esCadena(valor) || esIdentificador(valor));
    }

    private boolean esIdentificador(String valor) {
        return valor != null && valor.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private boolean esTemporal(String valor) {
        return valor != null && valor.matches("T\\d+");
    }

    private boolean esCadena(String valor) {
        return valor != null && valor.length() >= 2 && valor.startsWith("\"") && valor.endsWith("\"");
    }

    private boolean esCero(String valor) {
        return "0".equals(valor);
    }

    private boolean esUno(String valor) {
        return "1".equals(valor);
    }

    private boolean mismoValor(String a, String b) {
        return valor(a).equals(valor(b));
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isEmpty();
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private boolean esEfectoSecundario(Cuadruplo c) {
        if (c == null || c.operador == null) {
            return false;
        }

        String op = c.operador.toUpperCase();
        switch (op) {
            case "PRINT":
            case "MOSTRAR":
            case "ALLOC":
            case "FREE":
            case "ERROR":
            case "INSERTAR":
            case "INSERTAR_FINAL":
            case "INSERTAR_INICIO":
            case "INSERTAR_EN_POSICION":
            case "INSERTAR_FRENTE":
            case "AGREGARNODO":
            case "ELIMINARNODO":
            case "APILAR":
            case "PUSH":
            case "DESAPILAR":
            case "POP":
            case "ENCOLAR":
            case "ENQUEUE":
            case "DESENCOLAR":
            case "DEQUEUE":
            case "ELIMINAR":
            case "ELIMINAR_INICIO":
            case "ELIMINAR_FINAL":
            case "ELIMINAR_FRENTE":
            case "ELIMINAR_POSICION":
            case "BUSCAR":
            case "RECORRER":
            case "BFS":
            case "DFS":
            case "AGREGARARISTA":
            case "ELIMINARARISTA":
            case "ACTUALIZAR":
            case "REHASH":
            case "CAMINOCORTO":
                return true;
            default:
                return false;
        }
    }

    private boolean esBarreraPropagacion(Cuadruplo c) {
        if (c == null || c.operador == null) {
            return false;
        }

        String op = c.operador.toUpperCase();
        switch (op) {
            case "PRINT":
            case "MOSTRAR":
            case "ERROR":
                return false;
            default:
                return esEfectoSecundario(c);
        }
    }
}
