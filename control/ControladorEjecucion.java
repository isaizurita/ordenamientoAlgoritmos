package control;

import abstraccion.*;
import java.util.List;

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
                this.limpiarResultados();

                System.out.println("Iniciando comparación para tamaño máximo: " + n);

                // Instanciamos la clase Analizador Edificiencia para lanzar el análisis
                AnalizadorEficiencia analizador = new AnalizadorEficiencia(n);
                // Llamamos al método ejecutar análisis de la nueva instancia analizador
                analizador.ejecutarAnalisis();
                // Usando Singleton llamamos a la única instancia de Gestor Resultados para exportarlos a "resultados.csv"
                //GestorResultados.getInstancia().exportarResultadosCSV("resultados.csv");
            }

        /**
         * Función para devolver los resultsdos del análisis.
         * Llamado por MainApp para construir las gráficas.
         * * @return La lista de objetos Resultado que generó el análisis
         */
        public List<Resultado> getResultadosCompletos()
            {
                return GestorResultados.getInstancia().getResultados();
            }
        
        /*
         * Función que ejecuta la exportración de todos los reportes creados en los archivos .csv
         * Llamado por el botón "Exportan" de MainApp
         * * @return Un mensaje de éxito o error
         */
        public String exportarReportesCSV()
            {
                List<Resultado> resultados = getResultadosCompletos();

                if (resultados == null || resultados.isEmpty())
                    {
                        System.err.println("No hay resultados para exportar");
                        return "Error, no se encontraton datos";
                    }
                try
                    {
                        // Exportamos el .csv principal
                        GestorResultados.getInstancia().exportarResultadosCSV(("resultados_completos.csv"));

                        // Exportamos los .csv de la clase Gráfica
                        Grafica grafica = new Grafica();
                        grafica.generarGrafica();

                        return "Exportación completa!";
                    }
                catch(Exception e)
                    {
                        e.printStackTrace();
                        return "Error duranre la exportación: " + e.getMessage();
                    }
            }

        /*
         * Función para limpiar los datos de todo el sistema, se llama después de una ejecución con el botón "Salir"
         */
        public void limpiarResultados()
            {
                GestorResultados.getInstancia().limpiar();
            }
  
    }