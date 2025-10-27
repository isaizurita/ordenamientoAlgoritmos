package abstraccion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que genera un "resumen" de los resultados y prepara un CSV por algoritmo
 * para facilitar la graficación externa.
 *
 * Por ahora no genera imágenes; en su lugar crea archivos CSV por algoritmo y un resumen en consola.
 */
public class Grafica 
    {

        private final GestorResultados gestor = GestorResultados.getInstancia();

        /**
         * Genera la "gráfica" (resumen y archivos CSV por algoritmo).
         */
        public void generarGrafica() 
            {
                List<Resultado> resultados = gestor.getResultados();
                if (resultados.isEmpty()) 
                    {
                        System.out.println("No hay resultados para graficar.");
                        return;
                    }

                // Agrupar por algoritmo
                Map<String, StringBuilder> mapas = new HashMap<>();
                Map<String, Integer> cuenta = new HashMap<>();
                Map<String, Double> suma = new HashMap<>();

                for (Resultado r : resultados) 
                    {
                        String alg = r.getAlgoritmo();
                        mapas.computeIfAbsent(alg, k -> 
                            {
                                StringBuilder sb = new StringBuilder();
                                sb.append("tamano,tiempo_ms\n");
                                return sb;
                            }).append(r.getTamano()).append(",").append(String.format("%.6f", r.getTiempoMs())).append("\n");

                        cuenta.put(alg, cuenta.getOrDefault(alg, 0) + 1);
                        suma.put(alg, suma.getOrDefault(alg, 0.0) + r.getTiempoMs());
                    }

                // Escribimos archivos CSV por algoritmo
                for (Map.Entry<String, StringBuilder> e : mapas.entrySet()) 
                    {
                        String nombre = e.getKey().replaceAll("\\s+", "_");
                        String archivo = "grafica_" + nombre + ".csv";
                        try (FileWriter fw = new FileWriter(archivo)) 
                            {
                                fw.write(e.getValue().toString());
                                System.out.println("Archivo para graficar creado: " + archivo);
                            } 
                        catch (IOException ex) 
                            {
                                System.err.println("Error escribiendo " + archivo + ": " + ex.getMessage());
                            }
                    }

                // Mostrar resumen por algoritmo (promedio)
                System.out.println("\n--- Resumen por algoritmo (promedio) ---");
                for (String alg : mapas.keySet()) 
                    {
                        int c = cuenta.getOrDefault(alg, 0);
                        double avg = suma.getOrDefault(alg, 0.0) / Math.max(1, c);
                        System.out.printf("%-12s -> muestras: %2d, promedio: %8.4f ms%n", alg, c, avg);
                    }

                // También generar un CSV consolidado
                try (FileWriter fw = new FileWriter("grafica_consolidada.csv")) 
                    {
                        fw.write("tamano,algoritmo,tiempo_ms\n");
                        for (Resultado r : resultados) {
                            fw.write(r.getTamano() + "," + r.getAlgoritmo() + "," + String.format("%.6f", r.getTiempoMs()) + "\n");
                        }
                        System.out.println("Archivo consolidado para graficar: grafica_consolidada.csv");
                    } 
                catch (IOException ex) 
                    {
                        System.err.println("Error creando grafica_consolidada.csv: " + ex.getMessage());
                    }

                System.out.println("\nSe pueden usar 'grafica_<algoritmo>.csv' o 'grafica_consolidada.csv' para crear gráficas.");
            }
    }
