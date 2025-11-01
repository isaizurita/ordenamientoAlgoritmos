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

                // Agrupar por algoritmo Y CASO
                Map<String, StringBuilder> mapas = new HashMap<>();
                Map<String, Integer> cuenta = new HashMap<>();
                Map<String, Double> suma = new HashMap<>();

                for (Resultado r : resultados) 
                    {
                        // La clave única ahora incluye el algoritmo y el caso
                        String claveUnica = r.getAlgoritmo() + "_" + r.getCaso();
                        
                        mapas.computeIfAbsent(claveUnica, k -> 
                            {
                                StringBuilder sb = new StringBuilder();
                                sb.append("tamano,tiempo_ms\n"); // Cabecera para los archivos individuales
                                return sb;
                            }).append(r.getTamano()).append(",").append(String.format("%.6f", r.getTiempoMs())).append("\n");

                        cuenta.put(claveUnica, cuenta.getOrDefault(claveUnica, 0) + 1);
                        suma.put(claveUnica, suma.getOrDefault(claveUnica, 0.0) + r.getTiempoMs());
                    }

                // Escribimos archivos CSV por algoritmo Y CASO
                for (Map.Entry<String, StringBuilder> e : mapas.entrySet()) 
                    {
                        // La clave (ej. "BubbleSort_PROMEDIO") ya es un nombre de archivo seguro
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

                // Mostrar resumen por algoritmo Y CASO
                System.out.println("\n--- Resumen por algoritmo y caso (promedio) ---");
                for (String claveUnica : mapas.keySet()) 
                    {
                        int c = cuenta.getOrDefault(claveUnica, 0);
                        double avg = suma.getOrDefault(claveUnica, 0.0) / Math.max(1, c);
                        // Ajustamos el espacio de formato para la clave más larga
                        System.out.printf("%-24s -> muestras: %2d, promedio: %8.4f ms%n", claveUnica, c, avg);
                    }

                // También generar un CSV consolidado (CON CASO)
                try (FileWriter fw = new FileWriter("grafica_consolidada.csv")) 
                    {
                        // Añadimos la columna "caso"
                        fw.write("tamano,algoritmo,caso,tiempo_ms\n");
                        for (Resultado r : resultados) 
                            {
                                fw.write(r.getTamano() + "," + 
                                         r.getAlgoritmo() + "," +
                                         r.getCaso() + "," + // <-- CAMPO AÑADIDO
                                         String.format("%.6f", r.getTiempoMs()) + "\n");
                            }
                        System.out.println("Archivo consolidado para graficar: grafica_consolidada.csv");
                    } 
                catch (IOException ex) 
                    {
                        System.err.println("Error creando grafica_consolidada.csv: " + ex.getMessage());
                    }

                System.out.println("\nSe pueden usar 'grafica_<algoritmo>_<caso>.csv' o 'grafica_consolidada.csv' para crear gráficas.");
            }
    }
