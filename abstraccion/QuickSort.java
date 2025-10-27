package abstraccion;

import java.util.Random;

/**
 * Implementación de Quick Sort (in-place).
 */
public class QuickSort implements EstrategiaOrdenamiento 
    {
        private final Random rnd = new Random();

        @Override
        public void ordenar(int[] arreglo) 
            {
                quickSort(arreglo, 0, arreglo.length - 1);
            }

        private void quickSort(int[] a, int l, int r) 
            {
                if (l >= r) 
                    return;
                int p = partition(a, l, r);
                quickSort(a, l, p - 1);
                quickSort(a, p + 1, r);
            }

        private int partition(int[] a, int l, int r) 
            {
                // Elegir pivote aleatorio para evitar peor caso en arreglos ordenados
                int pivotIndex = l + rnd.nextInt(r - l + 1);
                int pivot = a[pivotIndex];
                swap(a, pivotIndex, r);
                int store = l;
                for (int i = l; i < r; i++) 
                    {
                        if (a[i] < pivot) 
                            {
                                swap(a, i, store);
                                store++;
                            }
                    }
                swap(a, store, r);
                return store;
            }

        private void swap(int[] a, int i, int j) 
            {
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }

        @Override
        public String getNombre() 
            {
                return "QuickSort";
            }
    }
