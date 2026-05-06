import java.util.*;
import java.nio.file.*;

/**
 * PRUEBA SIMPLE DEL GENERADOR DE ENSAMBLADOR
 * ============================================================
 * Esta clase demuestra cómo usar GeneradorEnsamblador
 * de forma aislada sin necesidad de compilar código DSL completo
 */
public class PruebaGenerador {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║   PRUEBA: GENERADOR DE ENSAMBLADOR CON OPTIMIZACIÓN                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        // Crear generador
        GeneradorEnsamblador generador = new GeneradorEnsamblador();
        
        System.out.println("✓ GeneradorEnsamblador creado");
        System.out.println("✓ Optimización activa: true\n");

        // Crear cuádruplos de ejemplo
        List<Cuadruplo> cuadruplos = crearCuadruplosEjemplo();
        
        System.out.println("✓ Se crearon " + cuadruplos.size() + " cuádruplos\n");
        System.out.println("CUÁDRUPLOS GENERADOS:");
        System.out.println("─────────────────────");
        for (Cuadruplo c : cuadruplos) {
            System.out.println("  " + c);
        }
        
        // Procesar cuádruplos
        System.out.println("\n✓ Procesando cuádruplos...\n");
        generador.procesarCuadruplos(cuadruplos);
        
        // Obtener código generado
        String codigoASM = generador.obtenerCodigoEnsamblador();
        
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║           CÓDIGO ENSAMBLADOR GENERADO (x86-64)                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
        System.out.println(codigoASM);
        
        // Guardar a archivo
        guardarArchivo(codigoASM, "salida_prueba.asm");
        
        System.out.println("\n✓ Archivo guardado: salida_prueba.asm");
        System.out.println("\n¡Prueba completada exitosamente!");
    }

    /**
     * Crea cuádruplos de ejemplo
     * Simula: a = 5; b = 3; c = a + b; PRINT c
     */
    private static List<Cuadruplo> crearCuadruplosEjemplo() {
        List<Cuadruplo> cuadruplos = new ArrayList<>();
        
        // a = 5
        cuadruplos.add(new Cuadruplo("=", "5", "null", "a"));
        
        // b = 3
        cuadruplos.add(new Cuadruplo("=", "3", "null", "b"));
        
        // c = a + b
        cuadruplos.add(new Cuadruplo("+", "a", "b", "c"));
        
        // PRINT c
        cuadruplos.add(new Cuadruplo("PRINT", "c", "null", "null"));
        
        // IF_FALSE para bifurcación
        cuadruplos.add(new Cuadruplo("IF_FALSE", "c", "null", "fin"));
        
        // APILAR en pila1
        cuadruplos.add(new Cuadruplo("APILAR", "c", "null", "pila1"));
        
        // ETIQUETA
        cuadruplos.add(new Cuadruplo("ETIQUETA", "null", "null", "fin"));
        
        // GOTO salida
        cuadruplos.add(new Cuadruplo("GOTO", "null", "null", "salida"));
        
        return cuadruplos;
    }

    /**
     * Guarda el código en un archivo
     */
    private static void guardarArchivo(String contenido, String nombreArchivo) {
        try {
            Path ruta = Paths.get(nombreArchivo);
            Files.write(ruta, contenido.getBytes());
        } catch (Exception e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
        }
    }
}
