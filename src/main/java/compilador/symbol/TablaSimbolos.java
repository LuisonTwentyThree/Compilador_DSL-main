package compilador.symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;

/*
 ESTA CLASE ES LA "MEMORIA" DEL COMPILADOR (TABLA DE SÍMBOLOS).
 Soporta Ámbitos (Scope) y ahora usa Herencia para no desperdiciar memoria.
 */
public class TablaSimbolos {

    private final Stack<Map<String, Simbolo>> pilaAmbitos;

    public TablaSimbolos() {
        this.pilaAmbitos = new Stack<>();
        this.pilaAmbitos.push(new HashMap<>());
    }

    public void entrarAmbito() {
        pilaAmbitos.push(new HashMap<>());
    }

    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) { 
            pilaAmbitos.pop(); 
        }
    }

    // --- NUESTRA FÁBRICA DE SÍMBOLOS ---
    public void insertar(String nombre, String tipo, Object valor) {
        Simbolo nuevoSimbolo;
        
        // Decidimos qué subclase crear dependiendo del tipo de dato
        if (tipo.equals("GRAFO")) {
            nuevoSimbolo = new SimboloGrafo(nombre, tipo, valor);
        } else if (tipo.equals("TABLA_HASH")) {
            nuevoSimbolo = new SimboloHash(nombre, tipo, valor);
        } else {
            nuevoSimbolo = new SimboloEstandar(nombre, tipo, valor);
        }
        
        // Siempre se guarda en la caja de hasta arriba
        pilaAmbitos.peek().put(nombre, nuevoSimbolo);
    }

    public boolean existe(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            if (pilaAmbitos.get(i).containsKey(nombre)) {
                return true;
            }
        }
        return false;
    }

    public Simbolo getSimbolo(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            if (pilaAmbitos.get(i).containsKey(nombre)) {
                return pilaAmbitos.get(i).get(nombre);
            }
        }
        return null;
    }

    public void limpiar() {
        pilaAmbitos.clear();
        pilaAmbitos.push(new HashMap<>());
    }

    public Map<String, Simbolo> getTodosLosSimbolos() {
        Map<String, Simbolo> todos = new HashMap<>();
        for (Map<String, Simbolo> ambito : pilaAmbitos) {
            todos.putAll(ambito);
        }
        return todos;
    }

    public void eliminar(String nombre) {
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            Map<String, Simbolo> ambito = pilaAmbitos.get(i);
            if (ambito.containsKey(nombre)) {
                ambito.remove(nombre);
                return;
            }
        }
    }

    // =================================================================
    // APLICACIÓN DE HERENCIA Y POLIMORFISMO PARA OPTIMIZAR MEMORIA
    // =================================================================

    // 1. LA CLASE PADRE (Contiene solo lo que TODOS los símbolos comparten)
    public static class Simbolo {
        public String nombre;
        public String tipo;
        public Object valor;
        public int tamano = 0;

        public Simbolo(String nombre, String tipo, Object valor) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.valor = valor;
        }
    }

    // 2. SUBCLASE PARA GRAFOS
    public static class SimboloGrafo extends Simbolo {
        public Set<String> nodosGrafo;
        public Set<String> aristasInsertadas;

        public SimboloGrafo(String nombre, String tipo, Object valor) {
            super(nombre, tipo, valor);
            this.nodosGrafo = new HashSet<>();
            this.aristasInsertadas = new HashSet<>();
        }
    }

    // 3. SUBCLASE PARA TABLAS HASH
    public static class SimboloHash extends Simbolo {
        public Set<String> valoresInsertados;

        public SimboloHash(String nombre, String tipo, Object valor) {
            super(nombre, tipo, valor);
            this.valoresInsertados = new HashSet<>();
        }
    }

    // 4. SUBCLASE PARA VARIABLES NORMALES
    public static class SimboloEstandar extends Simbolo {
        public SimboloEstandar(String nombre, String tipo, Object valor) {
            super(nombre, tipo, valor);
        }
    }
}