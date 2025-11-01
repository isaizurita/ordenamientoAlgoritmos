package presentacion;

import abstraccion.Resultado; // Importamos la entidad de datos
import control.ControladorEjecucion; // Importamos el Controlador

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task; // <-- IMPORTANTE para evitar que la UI se congele
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Para filtrar listas

/**
 * Clase principal de la aplicación JavaFX (Capa de Presentación).
 * --- RESPONSABILIDAD: Mostrar la UI, recibir entradas y delegar TODA la lógica
 * al ControladorEjecucion. ---
 */
public class MainApp extends Application 
    {
        // El único punto de contacto con la lógica del negocio
        private final ControladorEjecucion controlador = new ControladorEjecucion();

        private Stage escenarioPrincipal;
        
        // Almacenamos las escenas para navegar entre ellas
        private Scene escenaInicio;
        private Scene escenaResultados;
        
        // Almacenará la lista COMPLETA de resultados (incl. todos los casos)
        private List<Resultado> todosLosResultados; 

        @Override
        public void start(Stage primaryStage) 
            {
                this.escenarioPrincipal = primaryStage;
                escenarioPrincipal.setTitle("Analizador de Eficiencia");
                
                // Creamos e inicializamos la primera escena
                this.escenaInicio = crearEscenaInicio();
                escenarioPrincipal.setScene(this.escenaInicio);
                escenarioPrincipal.show();
            }

        // Pantalla de Inicio
        private Scene crearEscenaInicio() 
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox(20);
                contenidoPrincipal.setAlignment(Pos.CENTER);
                contenidoPrincipal.setPadding(new Insets(20, 50, 80, 50));

                Label tituloBienvenida = new Label("¡Bienvenido al Analizador de Eficiencia de Algoritmos!");
                tituloBienvenida.getStyleClass().add("welcome-title");

                HBox seccionEntrada = new HBox(15);
                seccionEntrada.setAlignment(Pos.CENTER);
                Label etiquetaEntrada = new Label("Por favor ingresa el tamaño del arreglo a analizar:");
                etiquetaEntrada.getStyleClass().add("input-label");
                
                TextField campoTamano = new TextField();
                campoTamano.setPromptText("Ingrese aquí");
                campoTamano.getStyleClass().add("size-field");
                campoTamano.setPrefWidth(150);
                seccionEntrada.getChildren().addAll(etiquetaEntrada, campoTamano);

                // Label para notificaciones
                Label etiquetaNotificacion = new Label();
                etiquetaNotificacion.getStyleClass().add("notification-label");
                
                Button botonInicio = new Button("Iniciar Ejecución");
                botonInicio.getStyleClass().add("action-button-blue");

                // --- LÓGICA DE EVENTOS ---
                botonInicio.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(campoTamano, etiquetaNotificacion, botonInicio);
                    });
                
                campoTamano.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(campoTamano, etiquetaNotificacion, botonInicio);
                    });
                // --- FIN LÓGICA DE EVENTOS ---

                contenidoPrincipal.getChildren().addAll(tituloBienvenida, seccionEntrada, etiquetaNotificacion, botonInicio);
                panelRaiz.setCenter(contenidoPrincipal);

                Scene escena = new Scene(panelRaiz, 800, 500);
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }
            
        /**
         * método de ayuda
         * Contiene toda la lógica de inicio de análisis, sacada del botón
         * para poder ser reutilizada por el TextField (al presionar Enter).
         */
        private void ejecutarLogicaInicio(TextField campoTamano, Label etiquetaNotificacion, Button botonInicio)
            {
                String input = campoTamano.getText().trim();
                int n;
                
                try 
                    {
                        n = Integer.parseInt(input);
                        if (n <= 0) throw new NumberFormatException();
                    } 
                catch (NumberFormatException ex) 
                    {
                        etiquetaNotificacion.setText("Error: Ingresa un número entero y positivo.");
                        etiquetaNotificacion.setStyle("-fx-text-fill: red;");
                        return;
                    }

                // ¡IMPORTANTE! Iniciamos análisis en un hilo separado para evitar que se "congelé" nuestra interfaz
                etiquetaNotificacion.setText("Analizando... esto puede tardar un poco, gracias por tu paciencia (:");
                etiquetaNotificacion.setStyle("-fx-text-fill: #007AFF;");
                botonInicio.setDisable(true);
                campoTamano.setDisable(true);

                Task<List<Resultado>> tareaAnalisis = new Task<>() 
                    {
                        @Override
                        protected List<Resultado> call() throws Exception 
                            {
                                // 1. Esto se ejecuta en el HILO DE BACKGROUND
                                controlador.iniciarComparacion(n);
                                // 2. Devuelve los resultados reales
                                return controlador.getResultadosCompletos();
                            }
                    };

                tareaAnalisis.setOnSucceeded(workerStateEvent -> 
                    {
                        // Volvemos al hilo propio de JavaFX
                        this.todosLosResultados = tareaAnalisis.getValue();
                        
                        // Creamos la nueva escena con los datos que nos brinda la capa de lógica
                        this.escenaResultados = crearEscenaResultados(); 
                        escenarioPrincipal.setScene(this.escenaResultados);

                        // Reseteamos la UI de inicio
                        botonInicio.setDisable(false);
                        campoTamano.setDisable(false);
                        campoTamano.clear();
                        etiquetaNotificacion.setText("");
                    });

                tareaAnalisis.setOnFailed(workerStateEvent -> 
                    {
                        etiquetaNotificacion.setText("Error: Ocurrió un fallo durante el análisis.");
                        etiquetaNotificacion.setStyle("-fx-text-fill: red;");
                        botonInicio.setDisable(false);
                        campoTamano.setDisable(false);
                        tareaAnalisis.getException().printStackTrace();
                    });

                new Thread(tareaAnalisis).start();
            }


        // Pantalla de resultados
        private Scene crearEscenaResultados() 
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox(20);
                contenidoPrincipal.setAlignment(Pos.TOP_CENTER);
                contenidoPrincipal.setPadding(new Insets(25, 40, 40, 40));

                Label tituloResultados = new Label("Resultados de la ejecución");
                tituloResultados.getStyleClass().add("results-title");

                // --- PANEL DE FILTROS ---
                HBox panelFiltros = new HBox(10);
                panelFiltros.setAlignment(Pos.CENTER);
                
                Label etiquetaFiltroAlgo = new Label("Ver comportamiento de:");
                etiquetaFiltroAlgo.getStyleClass().add("input-label");
                
                ComboBox<String> comboAlgoritmo = new ComboBox<>();
                comboAlgoritmo.setItems(FXCollections.observableArrayList(
                    "BubbleSort", "InsertionSort", "SelectionSort", "MergeSort", "QuickSort"
                ));
                comboAlgoritmo.setValue("BubbleSort");
                comboAlgoritmo.getStyleClass().add("filter-combo");

                // (¡NUEVO!) ComboBox para Caso
                Label etiquetaFiltroCaso = new Label("Caso:");
                etiquetaFiltroCaso.getStyleClass().add("input-label");
                ComboBox<String> comboCaso = new ComboBox<>();
                // Asumimos que la lógica devuelve estos Strings
                comboCaso.setItems(FXCollections.observableArrayList("Promedio", "Mejor", "Peor"));
                comboCaso.setValue("Promedio"); 
                comboCaso.getStyleClass().add("filter-combo");
                
                Button botonGraficaCrecimiento = new Button("Ver Gráfica de Crecimiento");
                botonGraficaCrecimiento.getStyleClass().add("action-button-green");

                // (¡CORREGIDO!) Añadimos TODOS los filtros al panel
                panelFiltros.getChildren().addAll(etiquetaFiltroAlgo, comboAlgoritmo, etiquetaFiltroCaso, comboCaso);

                // Label para notificaciones
                Label etiquetaNotificacion = new Label();
                etiquetaNotificacion.getStyleClass().add("notification-label");
                etiquetaNotificacion.setMinHeight(20); // Damos espacio

                // (¡DINÁMICO!) Creamos la tabla inicial con el caso por defecto
                GridPane tablaResultados = crearTablaResultados(comboCaso.getValue()); 
                
                contenidoPrincipal.getChildren().addAll(tituloResultados, panelFiltros, botonGraficaCrecimiento, etiquetaNotificacion, tablaResultados);
                panelRaiz.setCenter(contenidoPrincipal);

                // --- BARRA DE BOTONES INFERIOR ---
                HBox barraBotones = new HBox(20);
                barraBotones.setAlignment(Pos.CENTER);
                barraBotones.setPadding(new Insets(0, 0, 30, 0));

                Button botonExportar = new Button("Exportar archivo de datos");
                botonExportar.getStyleClass().add("action-button-blue");
                
                Button botonGraficaComparativa = new Button("Ver Gráfica Comparativa");
                botonGraficaComparativa.getStyleClass().add("action-button-blue");

                Button botonSalir = new Button("Salir");
                botonSalir.getStyleClass().add("action-button-red");

                // (¡NUEVO!) Actualiza la tabla dinámicamente cuando se cambia el caso
                comboCaso.setOnAction(e -> 
                {
                    String casoSeleccionado = comboCaso.getValue();
                    // Reemplazamos el grid viejo (índice 4) por uno nuevo filtrado
                    contenidoPrincipal.getChildren().set(4, crearTablaResultados(casoSeleccionado));
                });
                
                // Exportamos CSV (llama al controlador)
                botonExportar.setOnAction(e -> 
                {
                    String msg = controlador.exportarReportesCSV();
                    etiquetaNotificacion.setText(msg);
                    if (msg.startsWith("Error")) 
                    {
                        etiquetaNotificacion.setStyle("-fx-text-fill: red;");
                    } 
                    else 
                    {
                        etiquetaNotificacion.setStyle("-fx-text-fill: green;");
                    }
                });

                // (¡CORREGIDO!) Ver Gráfica Comparativa (lee el caso seleccionado)
                botonGraficaComparativa.setOnAction(e -> 
                {
                    String casoSeleccionado = comboCaso.getValue();
                    escenarioPrincipal.setScene(crearEscenaGraficaComparativa(casoSeleccionado));
                }); 
                
                // (¡CORREGIDO!) Ver Gráfica Individual (lee ambos combos)
                botonGraficaCrecimiento.setOnAction(e -> 
                {
                    String algoSeleccionado = comboAlgoritmo.getValue();
                    String casoSeleccionado = comboCaso.getValue();
                    escenarioPrincipal.setScene(crearEscenaGraficaIndividual(algoSeleccionado, casoSeleccionado));
                });
                
                // Salir
                botonSalir.setOnAction(e -> 
                {
                    controlador.limpiarResultados();
                    this.todosLosResultados = null; // Limpiamos la lista de la UI
                    escenarioPrincipal.setScene(this.escenaInicio); // Vuelve a la escena de inicio     
                });
                // --- FIN LÓGICA DE EVENTOS ---

                barraBotones.getChildren().addAll(botonExportar, botonGraficaComparativa, botonSalir);
                panelRaiz.setBottom(barraBotones);

                Scene escena = new Scene(panelRaiz, 800, 650); // Un poco más alto para los filtros
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }

        // Pantalla de gráficas comparativas
        private Scene crearEscenaGraficaComparativa(String caso) // Recibe el caso a filtrar
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox(25);
                contenidoPrincipal.setAlignment(Pos.TOP_CENTER);
                contenidoPrincipal.setPadding(new Insets(25, 40, 40, 40));

                Label etiquetaTituloGrafica = new Label("Comparativa de Crecimiento (" + caso + ")"); // Título dinámico
                etiquetaTituloGrafica.getStyleClass().add("results-title");

                NumberAxis ejeX = new NumberAxis();
                ejeX.setLabel("Tamaño del Arreglo (n)");
                ejeX.getStyleClass().add("axis-label");

                NumberAxis ejeY = new NumberAxis();
                ejeY.setLabel("Tiempo (ms) - Real");
                ejeY.getStyleClass().add("axis-label");

                LineChart<Number, Number> graficaLinea = new LineChart<>(ejeX, ejeY);
                graficaLinea.setTitle("Rendimiento Real de Algoritmos (" + caso + ")");
                graficaLinea.getStyleClass().add("chart-title");

                // --- LÓGICA DE GRÁFICA (Filtra por CASO) ---
                Map<String, XYChart.Series<Number, Number>> mapaSeries = new HashMap<>();
                for (Resultado r : this.todosLosResultados) // Usa la lista completa
                    {
                        // ¡FILTRO! Solo añade datos que coincidan con el caso seleccionado
                        if (r.getCaso().equals(caso)) 
                        {
                            mapaSeries.computeIfAbsent(r.getAlgoritmo(), nombre -> {
                                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                                series.setName(nombre);
                                return series;
                            });
                            
                            mapaSeries.get(r.getAlgoritmo()).getData().add(new XYChart.Data<>(r.getTamano(), r.getTiempoMs()));
                        }
                    }
                
                graficaLinea.getData().addAll(mapaSeries.values());
                // --- FIN LÓGICA DE GRÁFICA ---

                contenidoPrincipal.getChildren().addAll(etiquetaTituloGrafica, graficaLinea);
                panelRaiz.setCenter(contenidoPrincipal);

                HBox barraBotones = new HBox(20);
                barraBotones.setAlignment(Pos.CENTER);
                barraBotones.setPadding(new Insets(0, 0, 30, 0));
                Button botonVolver = new Button("Ver tabla de datos");
                botonVolver.getStyleClass().add("action-button-blue");
                botonVolver.setOnAction(e -> escenarioPrincipal.setScene(this.escenaResultados)); // Volvemos a pantalla 2
                barraBotones.getChildren().add(botonVolver);
                
                panelRaiz.setBottom(barraBotones);

                Scene escena = new Scene(panelRaiz, 800, 600);
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }
        
        // Pantalla de gráfica individual
        private Scene crearEscenaGraficaIndividual(String nombreAlgoritmo, String caso) // Recibe ALGORITMO y CASO
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox(25);
                contenidoPrincipal.setAlignment(Pos.TOP_CENTER);
                contenidoPrincipal.setPadding(new Insets(25, 40, 40, 40));

                Label etiquetaTituloGrafica = new Label("Comportamiento de: " + nombreAlgoritmo + " (" + caso + ")"); // Título dinámico
                etiquetaTituloGrafica.getStyleClass().add("results-title");

                NumberAxis ejeX = new NumberAxis();
                ejeX.setLabel("Tamaño del Arreglo (n)");
                ejeX.getStyleClass().add("axis-label");

                NumberAxis ejeY = new NumberAxis();
                ejeY.setLabel("Tiempo (ms) - Real");
                ejeY.getStyleClass().add("axis-label");

                LineChart<Number, Number> graficaLinea = new LineChart<>(ejeX, ejeY);
                graficaLinea.setTitle("Rendimiento Real: " + nombreAlgoritmo + " (" + caso + ")");
                graficaLinea.getStyleClass().add("chart-title");
                graficaLinea.setLegendVisible(false);

                // --- LÓGICA DE GRÁFICA (Filtra por ALGORITMO y CASO) ---
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(nombreAlgoritmo);
                
                for (Resultado r : this.todosLosResultados) // Usa la lista completa
                {
                    // ¡FILTRO DOBLE!
                    if (r.getAlgoritmo().equals(nombreAlgoritmo) && r.getCaso().equals(caso)) 
                    {
                        series.getData().add(new XYChart.Data<>(r.getTamano(), r.getTiempoMs()));
                    }
                }
                graficaLinea.getData().add(series);
                // --- FIN LÓGICA DE GRÁFICA ---
                
                contenidoPrincipal.getChildren().addAll(etiquetaTituloGrafica, graficaLinea);
                panelRaiz.setCenter(contenidoPrincipal);

                HBox barraBotones = new HBox(20);
                barraBotones.setAlignment(Pos.CENTER);
                barraBotones.setPadding(new Insets(0, 0, 30, 0));
                Button botonVolver = new Button("Ver tabla de datos");
                botonVolver.getStyleClass().add("action-button-blue");
                botonVolver.setOnAction(e -> escenarioPrincipal.setScene(this.escenaResultados)); // Volver a Pantalla 2
                barraBotones.getChildren().add(botonVolver);
                
                panelRaiz.setBottom(barraBotones);

                Scene escena = new Scene(panelRaiz, 800, 600);
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }

        /**
         * Método de ayuda para crear la cabecera (Título y Subtítulo)
         */
        private VBox crearEncabezado() 
            {
                VBox encabezado = new VBox();
                encabezado.setAlignment(Pos.CENTER);
                encabezado.setPadding(new Insets(10, 20, 10, 20));
                
                Label tituloApp = new Label("Analizador de Eficiencia de Algoritmos");
                tituloApp.getStyleClass().add("app-title");
                Label subtituloApp = new Label("Por Isai Zurita");
                subtituloApp.getStyleClass().add("app-subtitle");
                
                encabezado.getChildren().addAll(tituloApp, subtituloApp);

                Region espaciadorIzq = new Region();
                Region espaciadorDer = new Region();
                HBox.setHgrow(espaciadorIzq, Priority.ALWAYS);
                HBox.setHgrow(espaciadorDer, Priority.ALWAYS);
                
                HBox barraTitulo = new HBox(espaciadorIzq, encabezado, espaciadorDer);
                barraTitulo.setAlignment(Pos.CENTER);
                
                return encabezado; // Devuelve el VBox, no el HBox con spacers
            }
        
        /**
         * Método de ayuda para crear la tabla de resultados (Pantalla de Resultados)
         */
        private GridPane crearTablaResultados(String caso) // Recibe el CASO a filtrar
            {
                GridPane tablaResultados = new GridPane();
                tablaResultados.setAlignment(Pos.CENTER);
                tablaResultados.setHgap(50);
                tablaResultados.setVgap(12);
                tablaResultados.setPadding(new Insets(20, 0, 0, 0));

                Label etiquetaCabeceraAlg = new Label("Algoritmo");
                etiquetaCabeceraAlg.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraAlg, 0, 0);

                Label etiquetaCabeceraTam = new Label("Tamaño");
                etiquetaCabeceraTam.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraTam, 1, 0);

                Label etiquetaCabeceraTiempo = new Label("Tiempo");
                etiquetaCabeceraTiempo.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraTiempo, 2, 0);

                if (todosLosResultados == null || todosLosResultados.isEmpty()) 
                {
                    // No hacer nada si no hay resultados
                }
                else
                {
                    int nMaximo = 0;
                    for (Resultado r : todosLosResultados) 
                    {
                        nMaximo = Math.max(nMaximo, r.getTamano());
                    }

                    // (¡FILTRADO!)
                    final int finalNMaximo = nMaximo; 
                    List<Resultado> resultadosFinales = this.todosLosResultados.stream()
                        .filter(r -> r.getTamano() == finalNMaximo && r.getCaso().equals(caso))
                        .collect(Collectors.toList());
                    
                    int indiceFila = 1;
                    for (Resultado r : resultadosFinales)
                    {
                        tablaResultados.add(new Label(r.getAlgoritmo()), 0, indiceFila);
                        
                        tablaResultados.add(new Label(String.format("%,d", r.getTamano())), 1, indiceFila);
                        
                        tablaResultados.add(crearCeldaTiempo(r.getTiempoMs()), 2, indiceFila);
                        
                        indiceFila++;
                    }
                }
                
                return tablaResultados;
            }
        
        /**
         * Método de ayuda para crear la celda de tiempo con dos líneas
         * @param timeMs El tiempo real en milisegundos (ej. 7200.5)
         */
        private VBox crearCeldaTiempo(double timeMs) 
            {
                VBox celda = new VBox(0);
                celda.setAlignment(Pos.CENTER_LEFT);
                
                String tiempoPrincipal = String.format("%,.0f ms", timeMs);
                Label etiquetaPrincipal = new Label(tiempoPrincipal);
                etiquetaPrincipal.getStyleClass().add("time-main");
                
                celda.getChildren().add(etiquetaPrincipal);

                if (timeMs >= 1000)
                    {
                        double segundos = timeMs / 1000.0;
                        String tiempoSecundario = String.format("(%.1f seg)", segundos);
                        Label etiquetaSecundaria = new Label(tiempoSecundario);
                        etiquetaSecundaria.getStyleClass().add("time-secondary");
                        celda.getChildren().add(etiquetaSecundaria);
                    }
                
                return celda;
            }

        public static void main(String[] args) 
            {
                launch(args);
            }
    }

/*
 * Para ejecutar desde carpeta raíz
 * $ --module-path $PATH_TO_FX --add-modules javafx.controls \
   $ -d out \
 * $ abstraccion/*.java control/*.java presentacion/*.java
 * 
 * cp presentacion/styles.css out/presentacion/
 * 
 * $java --module-path $PATH_TO_FX --add-modules javafx.controls \
 * $ -cp out \
 * $ presentacion.MainApp
 */

 // *Arreglar Arreglo.java
 // *Arreglar Resultado.java
 // *Analizador eficiencia.java
 // Añadir columna GestorResultados.java y Grafica.java