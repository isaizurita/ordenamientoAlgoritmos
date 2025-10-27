package abstraccion;

import java.util.Arrays;
import java.util.List;

/**
 * Analizador de eficiencia con medición en nanosegundos,
 * warming-up y presentación dinámica en µs/ms.
 */
public class AnalizadorEficiencia 
    {

        private final List<EstrategiaOrdenamiento> algoritmos = Arrays.asList
            (
                new BubbleSort(),
                new InsertionSort(),
                new SelectionSort(),
                new MergeSort(),
                new QuickSort()
            );

        // Número de repeticiones por tamaño
        private static final int REPETICIONES = 50;

        // Tamaños que se usarán
        private final int[] tamanios;

        /**
         * Constructor que genera los tamaños basado en 'n' (10 segmentos).
         * @param n Tamaño máximo.
         */
        public AnalizadorEficiencia(int n) 
            {
                int segmentos = 10;
                
                // Ajustar segmentos si n es pequeño
                if (n < 1000 && n > 0) 
                    {
                        segmentos = Math.max(1, n / 100);
                    } 
                else if (n <= 0) 
                    {
                        segmentos = 1; // Evitar división por cero
                        n = 1; // Asegurar un tamaño válido
                    }
                
                int incremento = n / segmentos;

                if (incremento == 0) 
                    {
                        // Caso donde n es muy pequeño (e.g., n=50)
                        // Asignación ÚNICA en esta rama
                        this.tamanios = new int[] {n};
                    } 
                else 
                    {
                        // Asignación ÚNICA en esta rama
                        this.tamanios = new int[segmentos];
                        
                        // Llenamos el arreglo ya creado
                        for (int i = 0; i < segmentos; i++) 
                            {
                                // Genera [n/10, 2n/10, 3n/10, ..., n]
                                this.tamanios[i] = (i + 1) * incremento;
                            }
                    }
            }


        /**
         * Ejecuta el análisis para los tamaños definidos.
         */
        public void ejecutarAnalisis() 
            {
                GestorResultados gestor = GestorResultados.getInstancia();

                // Warm-up
                System.out.println("Realizando warm-up (JVM/JIT)...");
                int warmUpSize = Math.max(1000, (tamanios.length > 0 ? tamanios[0] : 1000));
                Arreglo warm = new Arreglo(warmUpSize);

                for (EstrategiaOrdenamiento estrategia : algoritmos) 
                    {
                        int[] copia = Arrays.copyOf(warm.getDatos(), warm.getDatos().length);
                        estrategia.ordenar(copia); // sin medir
                    }
                
                // Itera sobre los tamaños generados
                for (int n : tamanios) 
                    {
                        // Evitar tamaños 0 si 'n' era muy pequeño
                        if (n == 0) continue; 
                        
                        System.out.println("\n🔹 Tamaño del arreglo: " + n);

                        // Generar un arreglo base (se reutiliza la misma muestra para todas las repeticiones)
                        Arreglo base = new Arreglo(n);
                        int[] datosBase = base.getDatos();

                        for (EstrategiaOrdenamiento algoritmo : algoritmos) 
                            {
                                String nombre = algoritmo.getNombre();

                                long totalNs = 0L;

                                // Repeticiones medida
                                for (int r = 0; r < REPETICIONES; r++) 
                                    {
                                        int[] copia = Arrays.copyOf(datosBase, datosBase.length);

                                        long inicio = System.nanoTime();
                                        algoritmo.ordenar(copia);
                                        long fin = System.nanoTime();

                                        long deltaNs = fin - inicio;
                                        totalNs += deltaNs;
                                    }

                                // Promedio en nanosegundos (double para evitar truncamiento)
                                double promedioNs = totalNs / (double) REPETICIONES;
                                double promedioMs = promedioNs / 1_000_000.0; // para almacenar/CSV

                                // Impresión amigable (se mantiene)
                                if (promedioNs < 1_000_000.0) 
                                    {
                                        double promedioUs = promedioNs / 1000.0;
                                        System.out.printf("   %-12s → %8.3f µs (promedio de %d ejecuciones)%n",
                                                nombre, promedioUs, REPETICIONES);
                                    } 
                                else 
                                    {
                                        System.out.printf("   %-12s → %8.3f ms (promedio de %d ejecuciones)%n",
                                            nombre, promedioMs, REPETICIONES);
                                    }

                                // Guardar en gestor (se mantiene)
                                gestor.agregarResultado(new Resultado(nombre, n, promedioMs));
                            }
                    }

                System.out.println("\nAnálisis completado. Resultados en memoria.");

            }
    }

