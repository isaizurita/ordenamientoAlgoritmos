package abstraccion;
/**
 * Implementación de Merge Sort.
 */
public class MergeSort implements EstrategiaOrdenamiento
 {

    @Override
    public void ordenar(int[] arreglo) 
        {
            if (arreglo == null || arreglo.length < 2) 
                return;
            mergeSort(arreglo, 0, arreglo.length - 1);
        }

    private void mergeSort(int[] a, int l, int r) 
        {
            if (l >= r) 
                return;
            int m = (l + r) / 2;
            mergeSort(a, l, m);
            mergeSort(a, m + 1, r);
            merge(a, l, m, r);
        }

    private void merge(int[] a, int l, int m, int r) 
        {
            int n1 = m - l + 1;
            int n2 = r - m;
            int[] left = new int[n1];
            int[] right = new int[n2];
            System.arraycopy(a, l, left, 0, n1);
            System.arraycopy(a, m + 1, right, 0, n2);

            int i = 0, j = 0, k = l;
            while (i < n1 && j < n2) 
                {
                    if (left[i] <= right[j]) 
                        a[k++] = left[i++];
                    else a[k++] = right[j++];
                }
            while (i < n1) 
                a[k++] = left[i++];
            while (j < n2) 
                a[k++] = right[j++];
        }

    @Override
    public String getNombre() 
        {
            return "MergeSort";
        }
}
