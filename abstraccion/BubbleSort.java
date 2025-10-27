package abstraccion;

/**
 * Implementación de Bubble Sort.
 */
public class BubbleSort implements EstrategiaOrdenamiento 
    {
        @Override
        public void ordenar(int[] arreglo) 
            {
                int n = arreglo.length;
                boolean swapped;
                for (int i = 0; i < n - 1; i++) 
                    {
                        swapped = false;
                        for (int j = 0; j < n - 1 - i; j++) 
                            {
                                if (arreglo[j] > arreglo[j + 1]) 
                                    {
                                        int tmp = arreglo[j];
                                        arreglo[j] = arreglo[j + 1];
                                        arreglo[j + 1] = tmp;
                                        swapped = true;
                                    }
                            }
                        if (!swapped) break;
                    }
            }

        @Override
        public String getNombre() 
            {
                return "BubbleSort";
            }
    }
