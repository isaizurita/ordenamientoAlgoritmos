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

/**
 * Clase principal de la aplicación JavaFX (Capa de Presentación).
 * --- RESPONSABILIDAD: Mostrar la UI, recibir entradas y delegar TODA la lógica
 * al ControladorEjecucion. ---
 */
public class MainApp extends Application 
    {
        // El único punto de contacto con la lógica del negocio
        private final ControladorEjecucion controlador = new ControladorEjecucion();

        private Stage primaryStage;
        
        // Almacenamos las escenas para navegar entre ellas
        private Scene homeScene;
        private Scene resultsScene;

        @Override
        public void start(Stage primaryStage) 
            {
                this.primaryStage = primaryStage;
                primaryStage.setTitle("Analizador de Eficiencia");
                
                // Creamos e inicializamos la primera escena
                this.homeScene = createHomeScreen();
                primaryStage.setScene(this.homeScene);
                primaryStage.show();
            }

        // Pantalla de Inicio
        private Scene createHomeScreen() 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(20);
                mainContent.setAlignment(Pos.CENTER);
                mainContent.setPadding(new Insets(20, 50, 80, 50));

                Label welcomeTitle = new Label("¡BIENVENIDO AL Analizador de Eficiencia de Algoritmos!");
                welcomeTitle.getStyleClass().add("welcome-title");

                HBox inputSection = new HBox(15);
                inputSection.setAlignment(Pos.CENTER);
                Label inputLabel = new Label("Por favor ingresa el tamaño del arreglo a analizar a continuación:");
                inputLabel.getStyleClass().add("input-label");
                
                TextField sizeField = new TextField();
                sizeField.setPromptText("Ingrese aquí");
                sizeField.getStyleClass().add("size-field");
                sizeField.setPrefWidth(150);
                inputSection.getChildren().addAll(inputLabel, sizeField);

                // Label para notificaciones
                Label notificationLabel = new Label();
                notificationLabel.getStyleClass().add("notification-label");
                
                Button startButton = new Button("Iniciar Ejecución");
                startButton.getStyleClass().add("action-button-blue");

                startButton.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(sizeField, notificationLabel, startButton);
                    });
                
                sizeField.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(sizeField, notificationLabel, startButton);
                    });

                mainContent.getChildren().addAll(welcomeTitle, inputSection, notificationLabel, startButton);
                root.setCenter(mainContent);

                Scene scene = new Scene(root, 800, 500);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }
            
        /**
         * método de ayuda
         * Contiene toda la lógica de inicio de análisis, sacada del botón
         * para poder ser reutilizada por el TextField (al presionar Enter).
         */
        private void ejecutarLogicaInicio(TextField sizeField, Label notificationLabel, Button startButton)
            {
                String input = sizeField.getText().trim();
                int n;
                
                try 
                    {
                        n = Integer.parseInt(input);
                        if (n <= 0) throw new NumberFormatException();
                    } 
                catch (NumberFormatException ex) 
                    {
                        notificationLabel.setText("Error: Ingresa un número entero y positivo.");
                        notificationLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }

                // ¡IMPORTANTE! Iniciamos análisis en un hilo separado para evitar que se "congelé" nuestra interfaz
                notificationLabel.setText("Analizando... esto puede tardar un poco");
                notificationLabel.setStyle("-fx-text-fill: #007AFF;");
                startButton.setDisable(true);
                sizeField.setDisable(true);

                Task<List<Resultado>> analysisTask = new Task<>() 
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

                analysisTask.setOnSucceeded(workerStateEvent -> 
                    {
                        // Volvemos al hilo propio de JavaFX
                        List<Resultado> resultados = analysisTask.getValue();
                        
                        // Creamos la nueva escena con los datos que nos brinda la capa de lógica
                        this.resultsScene = createResultsScreen(resultados); 
                        primaryStage.setScene(this.resultsScene);

                        startButton.setDisable(false);
                        sizeField.setDisable(false);
                        sizeField.clear();
                        notificationLabel.setText("");
                    });

                analysisTask.setOnFailed(workerStateEvent -> 
                    {
                        notificationLabel.setText("Error: Ocurrió un fallo durante el análisis.");
                        notificationLabel.setStyle("-fx-text-fill: red;");
                        startButton.setDisable(false);
                        sizeField.setDisable(false);
                        analysisTask.getException().printStackTrace();
                    });

                new Thread(analysisTask).start();
            }


        // Pantalla de resultados
        private Scene createResultsScreen(List<Resultado> resultados) 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(20);
                mainContent.setAlignment(Pos.TOP_CENTER);
                mainContent.setPadding(new Insets(25, 40, 40, 40));

                Label resultsTitle = new Label("Estos son los resultados de cada ejecución para el tamaño ingresado");
                resultsTitle.getStyleClass().add("results-title");

                HBox filterBox = new HBox(10);
                filterBox.setAlignment(Pos.CENTER);
                Label filterLabel = new Label("Ver comportamiento de:");
                filterLabel.getStyleClass().add("input-label");
                
                ComboBox<String> filterCombo = new ComboBox<>();
                filterCombo.setItems(FXCollections.observableArrayList(
                    "BubbleSort", "InsertionSort", "SelectionSort", "MergeSort", "QuickSort"
                ));
                filterCombo.setValue("BubbleSort");
                filterCombo.getStyleClass().add("filter-combo");

                Label filterLabelCase = new Label("Caso:");
                filterLabelCase.getStyleClass().add("input-label");
                ComboBox<String> casoCombo = new ComboBox<>();
                casoCombo.setItems(FXCollections.observableArrayList("Promedio", "Mejor", "Peor"));
                casoCombo.setValue("Promedio"); 
                casoCombo.getStyleClass().add("filter-combo");
                
                Button viewGrowthChartButton = new Button("Ver Gráfica de Crecimiento");
                viewGrowthChartButton.getStyleClass().add("action-button-green");

                filterBox.getChildren().addAll(filterLabel, filterCombo, viewGrowthChartButton);

                // Label para notificaciones
                Label notificationLabel = new Label();
                notificationLabel.getStyleClass().add("notification-label");
                notificationLabel.setMinHeight(20); // Damos espacio

                GridPane resultsGrid = createResultsGrid(resultados); 
                
                mainContent.getChildren().addAll(resultsTitle, filterBox, notificationLabel, resultsGrid);
                root.setCenter(mainContent);

                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));

                Button exportButton = new Button("Exportar archivo de datos");
                exportButton.getStyleClass().add("action-button-blue");
                
                Button graphsButton = new Button("Ver Gráfica Comparativa (TODOS)");
                graphsButton.getStyleClass().add("action-button-blue");

                Button exitButton = new Button("Salir");
                exitButton.getStyleClass().add("action-button-red");
                
                
                // Exportamos CSV (llama al controlador)
                exportButton.setOnAction(e -> 
                {
                    String msg = controlador.exportarReportesCSV();
                    notificationLabel.setText(msg);
                    if (msg.startsWith("Error")) {
                        notificationLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        notificationLabel.setStyle("-fx-text-fill: green;");
                    }
                });

                // Ver Gráfica Comparativa
                graphsButton.setOnAction(e -> 
                    primaryStage.setScene(createComparativeChartScreen(resultados))
                ); 
                
                // Ver Gráfica Individual
                viewGrowthChartButton.setOnAction(e -> 
                {
                    String selectedAlgorithm = filterCombo.getValue();
                    String selectedCase = casoCombo.getValue();
                    List<Resultado> filteredResults = new ArrayList<>();
                    
                    // Filtramos la lista completa de resultados
                    for (Resultado r : resultados)
                    {
                        if (r.getAlgoritmo().equals(selectedAlgorithm))
                        {
                            filteredResults.add(r);
                        }
                    }
                    
                    // Pasamos solo los resultados filtrados a la pantalla de gráfica
                    primaryStage.setScene(createIndividualChartScreen(selectedAlgorithm, filteredResults));
                });
                
                // Salir
                exitButton.setOnAction(e -> 
                {
                    controlador.limpiarResultados();
                    primaryStage.setScene(this.homeScene); // Vuelve a la escena de inicio     
                });

                buttonBar.getChildren().addAll(exportButton, graphsButton, exitButton);
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }

        // Pantalla de gráficas comparativas
        private Scene createComparativeChartScreen(List<Resultado> resultados) 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(25);
                mainContent.setAlignment(Pos.TOP_CENTER);
                mainContent.setPadding(new Insets(25, 40, 40, 40));

                Label chartTitleLabel = new Label("Comparativa de Crecimiento por Complejidad");
                chartTitleLabel.getStyleClass().add("results-title");

                NumberAxis xAxis = new NumberAxis();
                xAxis.setLabel("Tamaño del Arreglo (n)");
                xAxis.getStyleClass().add("axis-label");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Tiempo (ms) - Real");
                yAxis.getStyleClass().add("axis-label");

                LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Rendimiento Real de Algoritmos");
                lineChart.getStyleClass().add("chart-title");

                Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();
                for (Resultado r : resultados)
                    {
                        seriesMap.computeIfAbsent(r.getAlgoritmo(), name -> {
                            XYChart.Series<Number, Number> series = new XYChart.Series<>();
                            series.setName(name);
                            return series;
                        });
                        
                        seriesMap.get(r.getAlgoritmo()).getData().add(new XYChart.Data<>(r.getTamano(), r.getTiempoMs()));
                    }
                
                lineChart.getData().addAll(seriesMap.values());

                mainContent.getChildren().addAll(chartTitleLabel, lineChart);
                root.setCenter(mainContent);

                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));
                Button backButton = new Button("Ver tabla de datos");
                backButton.getStyleClass().add("action-button-blue");
                backButton.setOnAction(e -> primaryStage.setScene(this.resultsScene)); // Volvemos a pantalla 2
                buttonBar.getChildren().add(backButton);
                
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }
        
        private Scene createIndividualChartScreen(String algorithmName, List<Resultado> filteredResults) 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(25);
                mainContent.setAlignment(Pos.TOP_CENTER);
                mainContent.setPadding(new Insets(25, 40, 40, 40));

                Label chartTitleLabel = new Label("Comportamiento de: " + algorithmName);
                chartTitleLabel.getStyleClass().add("results-title");

                NumberAxis xAxis = new NumberAxis();
                xAxis.setLabel("Tamaño del Arreglo (n)");
                xAxis.getStyleClass().add("axis-label");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Tiempo (ms) - Real");
                yAxis.getStyleClass().add("axis-label");

                LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Rendimiento Real: " + algorithmName);
                lineChart.getStyleClass().add("chart-title");
                lineChart.setLegendVisible(false);

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(algorithmName);
                
                for (Resultado r : filteredResults) 
                {
                    series.getData().add(new XYChart.Data<>(r.getTamano(), r.getTiempoMs()));
                }
                lineChart.getData().add(series);
                
                mainContent.getChildren().addAll(chartTitleLabel, lineChart);
                root.setCenter(mainContent);

                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));
                Button backButton = new Button("Ver tabla de datos");
                backButton.getStyleClass().add("action-button-blue");
                backButton.setOnAction(e -> primaryStage.setScene(this.resultsScene)); // Volver a Pantalla 2
                buttonBar.getChildren().add(backButton);
                
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }

        /**
         * Método de ayuda para crear la cabecera (Título y Subtítulo)
         */
        private VBox createHeader() 
            {
                VBox header = new VBox();
                header.setAlignment(Pos.CENTER);
                header.setPadding(new Insets(10, 20, 10, 20));
                
                Label titleApp = new Label("Analizador de Eficiencia de Algoritmos");
                titleApp.getStyleClass().add("app-title");
                Label subtitleApp = new Label("Por Isai Zurita");
                subtitleApp.getStyleClass().add("app-subtitle");
                
                header.getChildren().addAll(titleApp, subtitleApp);

                Region spacerLeft = new Region();
                Region spacerRight = new Region();
                HBox.setHgrow(spacerLeft, Priority.ALWAYS);
                HBox.setHgrow(spacerRight, Priority.ALWAYS);
                
                HBox titleBar = new HBox(spacerLeft, header, spacerRight);
                titleBar.setAlignment(Pos.CENTER);
                
                return header;
            }
        
        /**
         * Método de ayuda para crear la tabla de resultados (Pantalla de Resultados)
         */
        private GridPane createResultsGrid(List<Resultado> resultados) 
            {
                GridPane resultsGrid = new GridPane();
                resultsGrid.setAlignment(Pos.CENTER);
                resultsGrid.setHgap(50);
                resultsGrid.setVgap(12);
                resultsGrid.setPadding(new Insets(20, 0, 0, 0));

                Label algLabel = new Label("Algoritmo");
                algLabel.getStyleClass().add("grid-header");
                resultsGrid.add(algLabel, 0, 0);

                Label tamLabel = new Label("Tamaño");
                tamLabel.getStyleClass().add("grid-header");
                resultsGrid.add(tamLabel, 1, 0);

                Label tiempoLabel = new Label("Tiempo");
                tiempoLabel.getStyleClass().add("grid-header");
                resultsGrid.add(tiempoLabel, 2, 0);

                int maxN = 0;
                if (resultados == null || resultados.isEmpty()) {}
                else
                {
                    for (Resultado r : resultados) 
                    {
                        maxN = Math.max(maxN, r.getTamano());
                    }

                    List<Resultado> finalResults = new ArrayList<>();
                    for (Resultado r : resultados) 
                    {
                        if (r.getTamano() == maxN) 
                        {
                            finalResults.add(r);
                        }
                    }
                    
                    int rowIndex = 1;
                    for (Resultado r : finalResults)
                    {
                        resultsGrid.add(new Label(r.getAlgoritmo()), 0, rowIndex);
                        
                        resultsGrid.add(new Label(String.format("%,d", r.getTamano())), 1, rowIndex);
                        
                        resultsGrid.add(createTimeCell(r.getTiempoMs()), 2, rowIndex);
                        
                        rowIndex++;
                    }
                }
                
                return resultsGrid;
            }
        
        /**
         * Método de ayuda para crear la celda de tiempo con dos líneas
         * @param timeMs El tiempo real en milisegundos (ej. 7200.5)
         */
        private VBox createTimeCell(double timeMs) 
            {
                VBox cell = new VBox(0);
                cell.setAlignment(Pos.CENTER_LEFT);
                
                String mainTime = String.format("%,.0f ms", timeMs);
                Label mainLabel = new Label(mainTime);
                mainLabel.getStyleClass().add("time-main");
                
                cell.getChildren().add(mainLabel);

                if (timeMs >= 1000)
                    {
                        double seconds = timeMs / 1000.0;
                        // (ej. "(7.2 seg)")
                        String secondaryTime = String.format("(%.1f seg)", seconds);
                        Label secondaryLabel = new Label(secondaryTime);
                        secondaryLabel.getStyleClass().add("time-secondary");
                        cell.getChildren().add(secondaryLabel);
                    }
                
                return cell;
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
 // A{adir columna GestorResultados.java y Grafica.java