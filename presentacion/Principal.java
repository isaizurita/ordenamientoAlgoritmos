package presentacion;

import control.*;

import java.util.Scanner;

/**
 * Clase principal para iniciar la aplicación desde consola.
 * Pide al usuario el tamaño n y si desea generar la gráfica.
 */
public class Principal 
    {
        public static void main(String[] args) 
            {
                Scanner sc = new Scanner(System.in);
                System.out.println("*** ANALIZADOR DE EFICIENCIA DE ALGORITMOS DE ORDENAMIENTO ***");
                System.out.print("Introduce tamaño máximo del arreglo (n): ");
                int n = Integer.parseInt(sc.nextLine().trim());
                
                if (n <= 0) 
                    {
                        System.out.println("El tamaño debe ser mayor que 0. Saliendo.");
                        sc.close();
                        return;
                    }

                ControladorEjecucion controlador = new ControladorEjecucion();
                controlador.iniciarComparacion(n);

                System.out.print("¿Deseas generar resumen/gráfica (CSV y resumen) ahora? (s/n): ");
                String resp = sc.nextLine().trim().toLowerCase();
                if (resp.equals("s") || resp.equals("si")) 
                    {
                        controlador.mostrarGrafica();
                        System.out.println("Resumen y CSV de gráfica generados.");
                    } 
                else 
                    {
                        System.out.println("No se generó gráfica. Puedes usar 'resultados.csv' para graficar externamente.");
                    }

                System.out.println("Ejecución finalizada. Archivo con resultados: resultados.csv");
                sc.close();
            }
    }
