package abstraccion;

/**
 * Implementación de Insertion Sort.
 */
public class InsertionSort implements EstrategiaOrdenamiento 
    {
        @Override
        public void ordenar(int[] arreglo) 
            {
                for (int i = 1; i < arreglo.length; i++) 
                    {
                        int key = arreglo[i];
                        int j = i - 1;
                        while (j >= 0 && arreglo[j] > key) 
                            {
                                arreglo[j + 1] = arreglo[j];
                                j--;
                            }
                        arreglo[j + 1] = key;
                    }
            }

        @Override
        public String getNombre() 
            {
                return "InsertionSort";
            }
    }
