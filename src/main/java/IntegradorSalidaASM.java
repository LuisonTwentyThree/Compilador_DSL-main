import java.io.*;
import java.util.*;

/**
 * Integrador de Código Ensamblador y Visualización
 * 
 * Combina la generación de código ensamblador con la visualización de estructuras
 * de datos, generando un archivo de salida completo con toda la información.
 */
public class IntegradorSalidaASM {
    private GeneradorEnsamblador generador;
    private List<Cuadruplo> cuadruplos;
    private VisualizadorArboles.NodoABB arbolActual;
    private String nombreSalidaASM;
    private String nombreSalidaVIS;

    public IntegradorSalidaASM(String nombreBaseSalida) {
        this.generador = new GeneradorEnsamblador();
        this.cuadruplos = new ArrayList<>();
        this.arbolActual = null;
        this.nombreSalidaASM = nombreBaseSalida + ".asm";
        this.nombreSalidaVIS = nombreBaseSalida + "_visualizacion.txt";
    }

    /**
     * Agrega un cuádruplo para procesamiento
     */
    public void agregarCuadruplo(Cuadruplo c) {
        cuadruplos.add(c);
        generador.traducirCuadruplo(c);
    }

    /**
     * Procesa todos los cuádruplos
     */
    public void procesarTodos() {
        generador.procesarCuadruplos(cuadruplos);
    }

    /**
     * Maneja operación INSERTAR en árbol
     */
    public void procesarInsertar(int clave, String valor) {
        arbolActual = VisualizadorArboles.insertar(arbolActual, clave, valor);
        String operacion = String.format("INSERTAR(%d, \"%s\")", clave, valor);
        agregarRegistroOperacion(operacion, arbolActual);
    }

    /**
     * Maneja operación ELIMINAR del árbol
     */
    public void procesarEliminar(int clave) {
        VisualizadorArboles.NodoABB arbolAntes = arbolActual;
        arbolActual = VisualizadorArboles.eliminar(arbolActual, clave);
        String operacion = String.format("ELIMINAR(%d)", clave);
        agregarRegistroOperacion(operacion, arbolActual);
    }

    /**
     * Maneja operación BUSCAR en árbol
     */
    public boolean procesarBuscar(int clave) {
        VisualizadorArboles.NodoABB resultado = VisualizadorArboles.buscar(arbolActual, clave);
        String operacion = String.format("BUSCAR(%d) - %s", clave, resultado != null ? "ENCONTRADO" : "NO ENCONTRADO");
        agregarRegistroOperacion(operacion, arbolActual);
        return resultado != null;
    }

    /**
     * Agrega un registro de operación realizada
     */
    private void agregarRegistroOperacion(String operacion, VisualizadorArboles.NodoABB arbol) {
        // Este método podría generar visualización incremental
    }

    /**
     * Genera archivo ASM completo
     */
    public void generarArchivoASM() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreSalidaASM))) {
            writer.println(generador.obtenerCodigoEnsamblador());
            System.out.println("[✓] Archivo ASM generado: " + nombreSalidaASM);
        } catch (IOException e) {
            System.err.println("[✗] Error al generar archivo ASM: " + e.getMessage());
        }
    }

    /**
     * Genera archivo de visualización completo
     */
    public void generarArchivoVisualizacion() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreSalidaVIS))) {
            writer.println("╔════════════════════════════════════════════════════════════════════╗");
            writer.println("║          REPORTE DE EJECUCIÓN - DSL COMPILADOR                    ║");
            writer.println("║          Visualización de Estructuras de Datos                    ║");
            writer.println("╚════════════════════════════════════════════════════════════════════╝\n");

            writer.println("CUÁDRUPLOS GENERADOS:");
            writer.println("─".repeat(70));
            for (int i = 0; i < cuadruplos.size(); i++) {
                Cuadruplo c = cuadruplos.get(i);
                writer.printf("%3d: %-15s %-20s %-20s -> %s\n", 
                    i, c.operador, c.argumento1, c.argumento2, c.resultado);
            }

            writer.println("\n" + "═".repeat(70) + "\n");
            writer.println("ESTADO FINAL DEL ÁRBOL BINARIO:");
            writer.println(VisualizadorArboles.dibujarArbol(arbolActual));

            writer.println("RECORRIDOS DEL ÁRBOL:");
            writer.println(VisualizadorArboles.recorridoPreorden(arbolActual));
            writer.println(VisualizadorArboles.recorridoInorden(arbolActual));
            writer.println(VisualizadorArboles.recorridoPostorden(arbolActual));

            writer.println("\nVISTA POR NIVELES:");
            writer.println(VisualizadorArboles.dibujarPorNiveles(arbolActual));

            System.out.println("[✓] Archivo de visualización generado: " + nombreSalidaVIS);
        } catch (IOException e) {
            System.err.println("[✗] Error al generar archivo de visualización: " + e.getMessage());
        }
    }

    /**
     * Genera ambos archivos
     */
    public void generarArchivosCompletos() {
        procesarTodos();
        generarArchivoASM();
        generarArchivoVisualizacion();
        imprimirResumen();
    }

    /**
     * Imprime un resumen de la ejecución
     */
    public void imprimirResumen() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          RESUMEN DE GENERACIÓN         ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("\n✓ Cuádruplos generados: " + cuadruplos.size());
        System.out.println("✓ Instrucciones ensamblador: " + 
            generador.obtenerCodigoEnsamblador().split("\n").length);
        System.out.println("✓ Nodos en árbol final: " + 
            VisualizadorArboles.contarNodos(arbolActual));
        System.out.println("✓ Altura del árbol: " + 
            VisualizadorArboles.obtenerAltura(arbolActual));
        System.out.println("\nArchivos generados:");
        System.out.println("  • " + nombreSalidaASM);
        System.out.println("  • " + nombreSalidaVIS);
    }

    /**
     * Obtiene el código ensamblador
     */
    public String obtenerCodigoEnsamblador() {
        return generador.obtenerCodigoEnsamblador();
    }

    /**
     * Obtiene la visualización completa
     */
    public String obtenerVisualizacionCompleta() {
        return VisualizadorArboles.dibujarArbol(arbolActual);
    }

    /**
     * Imprime todo en consola
     */
    public void imprimirTodo() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("CÓDIGO ENSAMBLADOR GENERADO");
        System.out.println("═".repeat(70));
        System.out.println(obtenerCodigoEnsamblador());

        System.out.println("\n" + "═".repeat(70));
        System.out.println("VISUALIZACIÓN DE ÁRBOL");
        System.out.println("═".repeat(70));
        System.out.println(obtenerVisualizacionCompleta());
    }
}
