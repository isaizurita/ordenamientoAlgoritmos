package abstraccion;

/**
 * Contiene los datos de un resultado de ordenamiento para un tamaño dado.
 */
public class Resultado 
    {

        private final String algoritmo;
        private final int tamano;
        private final double tiempoMs;
        private final String caso;

        // Constructor completo
        public Resultado(String algoritmo, int tamano, double tiempoMs, String caso) 
            {
                this.algoritmo = algoritmo;
                this.tamano = tamano;
                this.tiempoMs = tiempoMs;
                this.caso = caso;
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

        public String getCaso(){
            return caso;
        }

        @Override
        public String toString() 
            {
                return String.format("Algoritmo: %s | Tamaño: %d | Tiempo: %.6f ms | Caso: %s", algoritmo, tamano, tiempoMs, caso);
            }
    }
