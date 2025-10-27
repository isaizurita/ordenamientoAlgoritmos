package abstraccion;

/**
 * Define el comportamiento de una estrategia de ordenamiento.
 */
public interface EstrategiaOrdenamiento 
    {
        void ordenar(int[] arreglo);
        String getNombre();
    }
