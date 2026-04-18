public class Cuadruplo {
    public String operador;
    public String argumento1;
    public String argumento2;
    public String resultado;

    public Cuadruplo(String operador, String argumento1, String argumento2, String resultado) {
        this.operador = operador;
        this.argumento1 = argumento1;
        this.argumento2 = argumento2;
        this.resultado = resultado;
    }

    // Método para imprimir el código generado de forma legible
    @Override
    public String toString() {
        if (operador.equals("=")) {
            return resultado + " = " + argumento1;
        } else if (operador.equals("GOTO") || operador.equals("IF_FALSE")) {
            return operador + " " + argumento1 + " " + argumento2 + " " + resultado;
        } else if (operador.equals("ETIQUETA")) {
            return resultado + ":";
        } else {
            return resultado + " = " + argumento1 + " " + operador + " " + argumento2;
        }
    }
}