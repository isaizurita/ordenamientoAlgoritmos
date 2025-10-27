package control;

import abstraccion.*;

/**
 * Capa de control. Orquesta la ejecución de la prueba y se comunicará con la capa de presentación.
 */
public class ControladorEjecucion 
    {
        /**
         * Inicia la comparación para tamaños hasta n (divide en 10 segmentos para mejor análisis).
         *
         * @param n tamaño máximo del arreglo
         */
        public void iniciarComparacion(int n) 
            {
                System.out.println("Iniciando comparación para tamaño máximo: " + n);

                // Instanciamos la clase Analizador Edificiencia para lanzar el análisis
                AnalizadorEficiencia analizador = new AnalizadorEficiencia(n);
                // Llamamos al método ejecutar análisis de la nueva instancia analizador
                analizador.ejecutarAnalisis();
                // Usando Singleton llamamos a la única instancia de Gestor Resultados para exportarlos a "resultados.csv"
                GestorResultados.getInstancia().exportarResultadosCSV("resultados.csv");
            }

        /**
         * Muestra la gráfica / resumen usando los resultados ya recopilados.
         */
        public void mostrarGrafica() 
            {
                Grafica grafica = new Grafica();
                grafica.generarGrafica();
            }
    }