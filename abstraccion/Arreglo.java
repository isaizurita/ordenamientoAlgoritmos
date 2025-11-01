package abstraccion;

import java.util.Arrays;
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

        /*
         * Devuelve una copia del arreglo ordenado ascendentemente, que es el mejro caso para el análisis.
         * @return Un nuevo arreglo ordenado.
         */
        public int[] getDatosOrdenados()
            {
                int [] copiaOrdenada = Arrays.copyOf(this.datos, this.datos.length);
                Arrays.sort(copiaOrdenada);
                return copiaOrdenada;
            }
        
        /*
         * Devuelve una copia del arreglo pero ordenado descendientemente, que es el peor caso para el análisis.
         * @return Un nuevo arreglo ordenado "al revés"
         */
        public int[] getDatosOrdenadosInversos()
            {
                int[] copiaInversa = this.getDatosOrdenados();

                for (int i = 0; i < copiaInversa.length / 2; i++) 
                    {
                        int temp = copiaInversa[i];
                        copiaInversa[i] = copiaInversa[copiaInversa.length - 1 - i];
                        copiaInversa[copiaInversa.length - 1 - i] = temp;
                    }
                
                return copiaInversa;
            }
    }
