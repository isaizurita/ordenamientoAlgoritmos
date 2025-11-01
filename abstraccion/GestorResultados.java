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
        private static GestorResultados instancia; // Singleton
        private List<Resultado> resultados;

        private GestorResultados() 
            {
                resultados = new ArrayList<>(); // Aquí "prohibimos" que cualquier otra clase cree una nueva instancia de GestorResulrados
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
         * Escribe la lista completa de resultados a un archivo CSV.
         * @param nombreArchivo El nombre del archivo a crear
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

            public void limpiar()
                {
                    resultados.clear();
                    System.out.println("El gestor de resultados fue depurado");
                }
    }
