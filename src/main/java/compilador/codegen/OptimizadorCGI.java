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
 * - reutilizacion de subexpresiones comunes dentro de bloques basicos
 * - simplificacion de saltos con condiciones constantes
 * - redireccion de saltos encadenados
 * - eliminacion de asignaciones sobrescritas antes de usarse
 * - eliminacion de saltos redundantes e instrucciones inalcanzables simples
 * - eliminacion de etiquetas sin referencias
 * - eliminacion de codigo muerto solo para temporales
 */
public class OptimizadorCGI {

    private static final int MAX_PASADAS = 6;

    public List<Cuadruplo> optimizar(List<Cuadruplo> codigo) {
        // La optimizacion trabaja sobre una copia para no modificar la lista
        // original que genero GeneradorCGI. Asi la GUI puede mostrar ambas:
        // codigo intermedio original y codigo optimizado.
        if (codigo == null) {
            return Collections.emptyList();
        }

        List<Cuadruplo> actual = copiarCodigo(codigo);

        for (int i = 0; i < MAX_PASADAS; i++) {
            // Se aplican varias pasadas porque una mejora puede habilitar otra.
            // La firma permite detenerse cuando ya no hubo cambios.
            // Ejemplo: T1 = 2 + 3 pasa a T1 = 5; despues otra pasada puede
            // reemplazar usos de T1 por 5.
            String antes = firma(actual);

            // 1) Simplifica cada cuadruplo de forma local.
            actual = simplificarOperaciones(actual);
            // 2) Reemplaza copias y constantes conocidas en instrucciones siguientes.
            actual = propagarCopiasYConstantes(actual);
            // 3) Vuelve a simplificar porque la propagacion puede crear nuevas constantes.
            actual = simplificarOperaciones(actual);
            // 4) Reutiliza calculos repetidos dentro del mismo bloque basico.
            actual = eliminarSubexpresionesComunes(actual);
            // 5) Limpia saltos innecesarios o etiquetas consecutivas.
            actual = simplificarSaltos(actual);
            // 6) Quita instrucciones que ya no pueden ejecutarse.
            actual = eliminarCodigoInalcanzable(actual);
            // 7) Quita asignaciones que se sobrescriben antes de leerse.
            actual = eliminarAsignacionesSobrescritas(actual);
            // 8) Quita etiquetas que nadie usa.
            actual = eliminarEtiquetasSinUso(actual);
            // 9) Quita temporales calculados pero nunca usados.
            actual = eliminarCodigoMuerto(actual);

            if (antes.equals(firma(actual))) {
                break;
            }
        }

        return actual;
    }

    private List<Cuadruplo> simplificarOperaciones(List<Cuadruplo> codigo) {
        // Reduce operaciones locales: constantes, algebra simple y saltos cuya
        // condicion ya se conoce. No analiza todo el programa, solo cada linea.
        // Es "local" porque decide mirando un cuadruplo a la vez.
        List<Cuadruplo> salida = new ArrayList<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);
            String op = c.operador;

            if ("=".equals(op) && mismoValor(c.resultado, c.argumento1)) {
                // x = x no cambia nada, por eso se elimina.
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
                // 5 < 8 se puede resolver en compilacion: T = 1.
                boolean resultado = calcularComparacion(op, Long.parseLong(c.argumento1), Long.parseLong(c.argumento2));
                salida.add(new Cuadruplo("=", resultado ? "1" : "0", "", c.resultado));
                continue;
            }

            if (isComparisonOperator(op) && mismoValor(c.argumento1, c.argumento2)) {
                String resultado = ("==".equals(op) || "<=".equals(op) || ">=".equals(op)) ? "1" : "0";
                salida.add(new Cuadruplo("=", resultado, "", c.resultado));
                continue;
            }

            if (("IF_FALSE".equals(op) || "IF_TRUE".equals(op)) && isNumeric(c.argumento1)) {
                // Si la condicion ya es constante, el salto se vuelve GOTO o desaparece.
                boolean condicion = Long.parseLong(c.argumento1) != 0;
                if (("IF_FALSE".equals(op) && !condicion) || ("IF_TRUE".equals(op) && condicion)) {
                    salida.add(new Cuadruplo("GOTO", "", "", c.resultado));
                }
                continue;
            }

            if (esAsignacionIdenticaConsecutiva(salida, c)) {
                continue;
            }

            salida.add(c);
        }

        return salida;
    }

    private List<Cuadruplo> eliminarSubexpresionesComunes(List<Cuadruplo> codigo) {
        // Reutiliza calculos puros repetidos dentro del mismo bloque basico.
        // Se reinicia en etiquetas, saltos y operaciones con efectos secundarios.
        // Ejemplo:
        // T1 = a + b
        // T2 = a + b
        // se convierte en T2 = T1, si a y b no cambiaron.
        Map<String, String> expresiones = new HashMap<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);

            if ("ETIQUETA".equals(c.operador) || "GOTO".equals(c.operador)
                    || c.operador.startsWith("IF") || esEfectoSecundario(c)) {
                // Una etiqueta o salto corta el bloque basico; una operacion con
                // efecto secundario puede cambiar memoria, estructuras o salida.
                expresiones.clear();
                salida.add(c);
                continue;
            }

            if (esIdentificador(c.resultado)) {
                // Si se modifica una variable, cualquier expresion que dependia
                // de ella deja de ser confiable.
                invalidarExpresionesCon(c.resultado, expresiones);
            }

            if (esOperacionPura(c) && esIdentificador(c.resultado)) {
                String clave = claveExpresion(c);
                String resultadoPrevio = expresiones.get(clave);
                if (resultadoPrevio != null && !mismoValor(resultadoPrevio, c.resultado)) {
                    salida.add(new Cuadruplo("=", resultadoPrevio, "", c.resultado));
                    continue;
                }
                expresiones.put(clave, c.resultado);
            }

            salida.add(c);
        }

        return salida;
    }

    private Cuadruplo simplificarAritmetica(Cuadruplo c) {
        // Plegado de constantes: 2 + 3 se convierte en = 5.
        // Reglas algebraicas: x + 0 -> x, x * 1 -> x, x * 0 -> 0, etc.
        // Si una regla no es segura, no se aplica. Por ejemplo, no se divide
        // entre cero durante la optimizacion.
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
        // Si sabemos que T1 = 5 o x = T1, intenta usar directamente ese valor
        // en instrucciones posteriores. Se limpia al cruzar etiquetas o saltos
        // porque ahi cambia el flujo y seria riesgoso asumir continuidad.
        // El mapa reemplazos funciona como una libreta temporal:
        // "si ves T1, puedes usar 5".
        Map<String, String> reemplazos = new HashMap<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);

            if ("ETIQUETA".equals(c.operador)) {
                // Al entrar a una etiqueta puede llegarse desde varios caminos,
                // asi que se descartan suposiciones del camino anterior.
                reemplazos.clear();
                salida.add(c);
                continue;
            }

            c.argumento1 = resolver(c.argumento1, reemplazos);
            c.argumento2 = resolver(c.argumento2, reemplazos);
            // Solo se reemplazan argumentos, no el resultado: el resultado es
            // donde se escribe, no de donde se lee.

            salida.add(c);

            if ("GOTO".equals(c.operador) || c.operador.startsWith("IF")) {
                // Despues de un salto no hay garantia de que la siguiente linea
                // sea la siguiente instruccion ejecutada.
                reemplazos.clear();
                continue;
            }

            if (esEfectoSecundario(c)) {
                // Operaciones como APILAR, INSERTAR o PRINT no son simples
                // asignaciones: pueden cambiar estructuras o producir salida.
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
                // Registra que el resultado ahora equivale a ese valor.
                reemplazos.put(c.resultado, c.argumento1);
            }
        }

        return salida;
    }

    private List<Cuadruplo> simplificarSaltos(List<Cuadruplo> codigo) {
        // Quita saltos que caen inmediatamente en la siguiente etiqueta y
        // redirige etiquetas consecutivas a una sola etiqueta canonica.
        // Ejemplo: GOTO L1 seguido de L1: no aporta nada.
        List<Cuadruplo> redirigido = redirigirSaltosEncadenados(redirigirEtiquetasConsecutivas(codigo));
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

    private List<Cuadruplo> redirigirSaltosEncadenados(List<Cuadruplo> codigo) {
        // Si un salto cae en una etiqueta cuyo primer comando real es otro GOTO,
        // apunta directo al destino final. Ejemplo: GOTO L1; L1: GOTO L2 -> GOTO L2.
        Map<String, String> destinoDirecto = new HashMap<>();

        for (int i = 0; i < codigo.size(); i++) {
            Cuadruplo c = codigo.get(i);
            if (c == null || !"ETIQUETA".equals(c.operador) || vacio(c.resultado)) {
                continue;
            }

            int j = i + 1;
            while (j < codigo.size() && codigo.get(j) != null && "ETIQUETA".equals(codigo.get(j).operador)) {
                j++;
            }

            if (j < codigo.size()) {
                Cuadruplo siguiente = codigo.get(j);
                if (siguiente != null && "GOTO".equals(siguiente.operador) && !vacio(siguiente.resultado)
                        && !mismoValor(c.resultado, siguiente.resultado)) {
                    destinoDirecto.put(c.resultado, siguiente.resultado);
                }
            }
        }

        if (destinoDirecto.isEmpty()) {
            return codigo;
        }

        List<Cuadruplo> salida = new ArrayList<>();
        for (Cuadruplo c : codigo) {
            Cuadruplo copia = copiar(c);
            if (copia != null && ("GOTO".equals(copia.operador) || copia.operador.startsWith("IF"))
                    && !vacio(copia.resultado)) {
                copia.resultado = resolverEtiqueta(copia.resultado, destinoDirecto);
            }
            salida.add(copia);
        }
        return salida;
    }

    private List<Cuadruplo> redirigirEtiquetasConsecutivas(List<Cuadruplo> codigo) {
        // Si aparecen L1: L2: seguidas, ambas apuntan al mismo lugar.
        // Los saltos a L2 se pueden redirigir a L1.
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
        // Despues de un GOTO incondicional, las instrucciones siguientes no se
        // ejecutan hasta encontrar una etiqueta. Esas lineas se pueden omitir.
        // Ejemplo:
        // GOTO L1
        // T1 = 5 + 3   <- no se ejecuta si no hay etiqueta antes
        // L1:
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
        // Recorre de atras hacia adelante para conservar solo resultados que
        // luego se usan. Mantiene siempre control y operaciones con efectos.
        // Se limita sobre todo a temporales para no borrar variables del usuario.
        // Ejemplo: T9 = a + b se borra si T9 nunca vuelve a usarse.
        Set<String> usados = new HashSet<>();
        List<Cuadruplo> salida = new ArrayList<>();

        for (int i = codigo.size() - 1; i >= 0; i--) {
            Cuadruplo c = codigo.get(i);
            if (c == null) {
                continue;
            }

            boolean conservar = esControl(c) || esEfectoSecundario(c)
                    || c.resultado == null || !esTemporal(c.resultado) || usados.contains(c.resultado);

            if (conservar) {
                salida.add(0, c);
                if (esIdentificador(c.resultado)) {
                    // Si este cuadruplo produce algo que se necesitaba, ya quedo
                    // satisfecho ese uso.
                    usados.remove(c.resultado);
                }
                // Sus argumentos pasan a ser necesarios para calcularlo.
                agregarUso(c.argumento1, usados);
                agregarUso(c.argumento2, usados);
            }
        }

        return salida;
    }

    private List<Cuadruplo> eliminarAsignacionesSobrescritas(List<Cuadruplo> codigo) {
        // Elimina escrituras como x = 0 si x vuelve a escribirse antes de leerse.
        // Es conservador: se reinicia al cruzar etiquetas, saltos o efectos en estructuras.
        List<Cuadruplo> salida = new ArrayList<>();
        Map<String, Integer> ultimaAsignacion = new HashMap<>();

        for (Cuadruplo original : codigo) {
            if (original == null || original.operador == null) {
                continue;
            }

            Cuadruplo c = copiar(original);

            if (esControl(c) || esBarreraPropagacion(c)) {
                ultimaAsignacion.clear();
                salida.add(c);
                continue;
            }

            invalidarAsignacionesLeidas(c.argumento1, ultimaAsignacion);
            invalidarAsignacionesLeidas(c.argumento2, ultimaAsignacion);

            if ("=".equals(c.operador) && esIdentificador(c.resultado) && !esTemporal(c.resultado)) {
                Integer anterior = ultimaAsignacion.get(c.resultado);
                if (anterior != null) {
                    salida.remove((int) anterior);
                    reajustarIndicesDespuesDeBorrar(anterior, ultimaAsignacion);
                }
                salida.add(c);
                ultimaAsignacion.put(c.resultado, salida.size() - 1);
                continue;
            }

            if (esIdentificador(c.resultado)) {
                ultimaAsignacion.remove(c.resultado);
            }

            salida.add(c);
        }

        return salida;
    }

    private List<Cuadruplo> eliminarEtiquetasSinUso(List<Cuadruplo> codigo) {
        // Primero se recopilan las etiquetas a las que realmente apunta algun salto.
        Set<String> etiquetasUsadas = new HashSet<>();
        for (Cuadruplo c : codigo) {
            if (c != null && c.operador != null
                    && ("GOTO".equals(c.operador) || c.operador.startsWith("IF"))
                    && !vacio(c.resultado)) {
                etiquetasUsadas.add(c.resultado);
            }
        }

        List<Cuadruplo> salida = new ArrayList<>();
        boolean anteriorFueGoto = false;
        for (Cuadruplo c : codigo) {
            if (c == null) {
                continue;
            }
            if ("ETIQUETA".equals(c.operador) && !etiquetasUsadas.contains(c.resultado) && !anteriorFueGoto) {
                // Una etiqueta no referenciada normalmente puede quitarse.
                // Si viene justo despues de un GOTO, se conserva para no romper
                // la frontera que usa eliminarCodigoInalcanzable.
                anteriorFueGoto = false;
                continue;
            }
            salida.add(c);
            anteriorFueGoto = "GOTO".equals(c.operador);
        }
        return salida;
    }

    private Long calcularAritmetica(String op, long v1, long v2) {
        switch (op) {
            case "+": return v1 + v2;
            case "-": return v1 - v2;
            case "*": return v1 * v2;
            case "/": return v2 == 0 ? null : v1 / v2;
            case "%": return v2 == 0 ? null : v1 % v2;
            default: return null;
        }
    }

    private boolean esAsignacionIdenticaConsecutiva(List<Cuadruplo> salida, Cuadruplo actual) {
        if (salida.isEmpty() || actual == null || !"=".equals(actual.operador)) {
            return false;
        }

        Cuadruplo anterior = salida.get(salida.size() - 1);
        return anterior != null
                && "=".equals(anterior.operador)
                && mismoValor(anterior.resultado, actual.resultado)
                && mismoValor(anterior.argumento1, actual.argumento1)
                && vacio(anterior.argumento2)
                && vacio(actual.argumento2);
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

    private void invalidarAsignacionesLeidas(String valor, Map<String, Integer> ultimaAsignacion) {
        if (esIdentificador(valor)) {
            ultimaAsignacion.remove(valor);
        }
    }

    private void reajustarIndicesDespuesDeBorrar(int indiceBorrado, Map<String, Integer> indices) {
        List<String> claves = new ArrayList<>(indices.keySet());
        for (String clave : claves) {
            int indice = indices.get(clave);
            if (indice == indiceBorrado) {
                indices.remove(clave);
            } else if (indice > indiceBorrado) {
                indices.put(clave, indice - 1);
            }
        }
    }

    private void agregarUso(String valor, Set<String> usados) {
        if (esIdentificador(valor)) {
            usados.add(valor);
        }
    }

    private void invalidarExpresionesCon(String valor, Map<String, String> expresiones) {
        expresiones.entrySet().removeIf(entry -> {
            String clave = entry.getKey();
            String resultado = entry.getValue();
            return mismoValor(valor, resultado) || claveContieneValor(clave, valor);
        });
    }

    private boolean claveContieneValor(String clave, String valor) {
        String buscado = "|" + valor + "|";
        return clave != null && (clave.contains(buscado) || clave.endsWith("|" + valor));
    }

    private String resolver(String valor, Map<String, String> reemplazos) {
        // Sigue la cadena de reemplazos:
        // T2 -> T1 -> 5 termina como 5.
        // El set visitados evita ciclos accidentales.
        String actual = valor;
        Set<String> visitados = new HashSet<>();
        while (esIdentificador(actual) && reemplazos.containsKey(actual) && visitados.add(actual)) {
            actual = reemplazos.get(actual);
        }
        return actual;
    }

    private String resolverEtiqueta(String etiqueta, Map<String, String> reemplazos) {
        String actual = etiqueta;
        Set<String> visitadas = new HashSet<>();
        while (!vacio(actual) && reemplazos.containsKey(actual) && visitadas.add(actual)) {
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
        // Convierte la lista a texto para comparar si una pasada cambio algo.
        // No se usa para mostrar al usuario, solo para detectar estabilidad.
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
        return "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "%".equals(op);
    }

    private boolean isComparisonOperator(String op) {
        return "<".equals(op) || ">".equals(op) || "==".equals(op) || "!=".equals(op)
                || "<=".equals(op) || ">=".equals(op);
    }

    private boolean esOperacionPura(Cuadruplo c) {
        return c != null && c.operador != null
                && (isArithmeticOperator(c.operador) || isComparisonOperator(c.operador));
    }

    private String claveExpresion(Cuadruplo c) {
        String op = c.operador;
        String a = valor(c.argumento1);
        String b = valor(c.argumento2);

        if (esConmutativa(op) && a.compareTo(b) > 0) {
            String tmp = a;
            a = b;
            b = tmp;
        }

        return op + "|" + a + "|" + b;
    }

    private boolean esConmutativa(String op) {
        return "+".equals(op) || "*".equals(op) || "==".equals(op) || "!=".equals(op);
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
        // Estas operaciones no se deben borrar aunque su resultado no se use,
        // porque modifican estructuras, imprimen, reservan memoria o reportan errores.
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

        // Las operaciones con estructuras modifican la estructura, pero no
        // reescriben variables primitivas del usuario. Por eso no deben borrar
        // constantes como x = 10 o suma = 15.
        return false;
    }
}
