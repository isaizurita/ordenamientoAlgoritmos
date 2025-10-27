package abstraccion;

import java.util.Random;

/**
 * Representa un arreglo de enteros que se llena aleatoriamente.
 */
public class Arreglo 
    {
        private int[] datos;

        /**
         * Crea un arreglo con la cantidad de elementos especificada y lo llena aleatoriamente.
         * @param elementos número de elementos del arreglo.
         */
        public Arreglo(int elementos) 
            {
                datos = new int[elementos];
                llenarAleatorio();
            }

        /**
         * Llena el arreglo con números aleatorios entre 0 y 9999.
         */
        public void llenarAleatorio() 
            {
                Random rand = new Random();
                for (int i = 0; i < datos.length; i++) 
                    {
                        datos[i] = rand.nextInt(10000);
                    }
            }

        /**
         * Devuelve los datos almacenados en el arreglo.
         * @return arreglo.
         */
        public int[] getDatos() 
            {
                return datos;
            }
    }
