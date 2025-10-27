package abstraccion;

/**
 * Contiene los datos de un resultado de ordenamiento para un tamaño dado.
 */
public class Resultado 
    {

        private final String algoritmo;
        private final int tamano;
        private final double tiempoMs;

        // Constructor completo
        public Resultado(String algoritmo, int tamano, double tiempoMs) 
            {
                this.algoritmo = algoritmo;
                this.tamano = tamano;
                this.tiempoMs = tiempoMs;
            }

        public String getAlgoritmo() {
            return algoritmo;
        }

        public int getTamano() {
            return tamano;
        }

        public double getTiempoMs() {
            return tiempoMs;
        }

        @Override
        public String toString() 
            {
                return String.format("Algoritmo: %s | Tamaño: %d | Tiempo: %.6f ms", algoritmo, tamano, tiempoMs);
            }
    }
