package abstraccion;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestiona todos los resultados obtenidos (Patrón Singleton).
 * --- RESPONSABILIDAD: Almacenar resultados y exportar el CSV principal ---
 */
public class GestorResultados 
    {
        private static GestorResultados instancia;
        private List<Resultado> resultados;

        private GestorResultados() 
            {
                resultados = new ArrayList<>();
            }

        public static GestorResultados getInstancia() 
            {
                if (instancia == null)
                    instancia = new GestorResultados();
                return instancia;
            }

        public void agregarResultado(Resultado r) 
            {
                resultados.add(r);
            }

        public List<Resultado> getResultados() 
            {
                return resultados;
            }

        /**
         * --- NUEVO MÉTODO ---
         * Escribe la lista completa de resultados a un archivo CSV.
         * Esta es la responsabilidad que movimos desde AnalizadorEficiencia.
         * @param nombreArchivo El nombre del archivo a crear (ej. "resultados.csv")
         */
        public void exportarResultadosCSV(String nombreArchivo)
            {
                if (resultados.isEmpty())
                    {
                        System.err.println("No hay resultados para exportar.");
                        return;
                    }
                
                try (FileWriter writer = new FileWriter(nombreArchivo)) 
                    {
                        writer.write("Tamaño,Algoritmo,PromedioMs\n");

                        for (Resultado r : resultados)
                            {
                                writer.write(String.format("%d,%s,%.6f\n", 
                                    r.getTamano(), 
                                    r.getAlgoritmo(), 
                                    r.getTiempoMs()));
                            }
                        
                        System.out.println("\nResultados exportados a '" + nombreArchivo + "'.");

                    } 
                catch (IOException e) 
                    {
                        System.err.println("Error al escribir " + nombreArchivo + ": " + e.getMessage());
                        e.printStackTrace();
                    }
            }
    }
