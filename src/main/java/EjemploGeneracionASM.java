import java.util.*;

/**
 * Ejemplo de Uso del Generador de Ensamblador con Visualización de Árboles
 * 
 * Demuestra cómo procesar cuádruplos, generar código ensamblador,
 * y visualizar árboles binarios después de cada operación.
 */
public class EjemploGeneracionASM {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║   COMPILADOR DSL - GENERADOR DE ENSAMBLADOR CON VISUALIZACIÓN       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        // Crear el integrador
        IntegradorSalidaASM integrador = new IntegradorSalidaASM("salida");

        // Simular generación de cuádruplos
        generarCuadruplosEjemplo(integrador);

        // Procesar y generar archivos
        integrador.generarArchivosCompletos();

        // Demostración interactiva
        demostracionInteractiva();
    }

    /**
     * Genera cuádruplos de ejemplo
     */
    private static void generarCuadruplosEjemplo(IntegradorSalidaASM integrador) {
        System.out.println("Generando cuádruplos...\n");

        // Simular operaciones en árbol
        integrador.procesarInsertar(50, "NODO50");
        integrador.procesarInsertar(30, "NODO30");
        integrador.procesarInsertar(70, "NODO70");
        integrador.procesarInsertar(20, "NODO20");
        integrador.procesarInsertar(40, "NODO40");
        integrador.procesarInsertar(60, "NODO60");
        integrador.procesarInsertar(80, "NODO80");

        // Operaciones de búsqueda
        integrador.procesarBuscar(40);
        integrador.procesarBuscar(100);

        // Operación de eliminación
        integrador.procesarEliminar(20);
    }

    /**
     * Demostración interactiva de visualización
     */
    private static void demostracionInteractiva() {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              DEMOSTRACIÓN INTERACTIVA                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        VisualizadorArboles.NodoABB arbol = null;

        // Paso 1: Crear árbol inicial
        System.out.println("PASO 1: Creando árbol inicial insertando: 50, 30, 70");
        arbol = VisualizadorArboles.insertar(arbol, 50, "Raíz");
        arbol = VisualizadorArboles.insertar(arbol, 30, "Izquierda");
        arbol = VisualizadorArboles.insertar(arbol, 70, "Derecha");
        System.out.println(VisualizadorArboles.dibujarArbol(arbol));
        pausar(2000);

        // Paso 2: Insertar más nodos
        System.out.println("\nPASO 2: Insertando más nodos: 20, 40, 60, 80");
        arbol = VisualizadorArboles.insertar(arbol, 20, "Sub-izq");
        arbol = VisualizadorArboles.insertar(arbol, 40, "Sub-der-izq");
        arbol = VisualizadorArboles.insertar(arbol, 60, "Sub-der-izq");
        arbol = VisualizadorArboles.insertar(arbol, 80, "Sub-der");
        System.out.println(VisualizadorArboles.dibujarArbol(arbol));
        pausar(2000);

        // Paso 3: Búsquedas
        System.out.println("\nPASO 3: Buscando nodo 40");
        VisualizadorArboles.NodoABB encontrado = VisualizadorArboles.buscar(arbol, 40);
        if (encontrado != null) {
            System.out.println("✓ Nodo encontrado: [" + encontrado.clave + " : " + encontrado.valor + "]");
        }

        System.out.println("\nPASO 3b: Buscando nodo 100 (no existe)");
        encontrado = VisualizadorArboles.buscar(arbol, 100);
        if (encontrado == null) {
            System.out.println("✗ Nodo no encontrado en el árbol");
        }
        pausar(2000);

        // Paso 4: Recorridos
        System.out.println("\nPASO 4: Recorridos del árbol");
        System.out.println(VisualizadorArboles.recorridoPreorden(arbol));
        System.out.println(VisualizadorArboles.recorridoInorden(arbol));
        System.out.println(VisualizadorArboles.recorridoPostorden(arbol));
        pausar(2000);

        // Paso 5: Vista por niveles
        System.out.println("\nPASO 5: Vista por niveles");
        System.out.println(VisualizadorArboles.dibujarPorNiveles(arbol));
        pausar(2000);

        // Paso 6: Eliminación
        System.out.println("\nPASO 6: Eliminando nodo 20");
        VisualizadorArboles.NodoABB arbolAntes = arbol;
        arbol = VisualizadorArboles.eliminar(arbol, 20);
        System.out.println(VisualizadorArboles.compararArboles(arbolAntes, arbol, "ELIMINAR 20"));
        pausar(2000);

        // Paso 7: Información final
        System.out.println("\nPASO 7: Información final del árbol");
        System.out.println("Total de nodos: " + VisualizadorArboles.contarNodos(arbol));
        System.out.println("Total de hojas: " + VisualizadorArboles.contarHojas(arbol));
        System.out.println("Altura: " + VisualizadorArboles.obtenerAltura(arbol));
        System.out.println("Balance raíz: " + VisualizadorArboles.obtenerBalance(arbol));
    }

    /**
     * Pausa la ejecución
     */
    private static void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
