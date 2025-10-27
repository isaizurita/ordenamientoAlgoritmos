package abstraccion;

/**
 * Implementación de Selection Sort.
 */
public class SelectionSort implements EstrategiaOrdenamiento 
    {
        @Override
        public void ordenar(int[] arreglo) 
            {
                int n = arreglo.length;
                for (int i = 0; i < n - 1; i++) 
                    {
                        int minIdx = i;
                        for (int j = i + 1; j < n; j++) 
                        {
                            if (arreglo[j] < arreglo[minIdx]) 
                                {
                                    minIdx = j;
                                }
                        }
                        int tmp = arreglo[minIdx];
                        arreglo[minIdx] = arreglo[i];
                        arreglo[i] = tmp;
                    }
            }

        @Override
        public String getNombre() 
            {
                return "SelectionSort";
            }
    }
