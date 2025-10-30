package presentacion;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
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

/**
 * Clase principal de la aplicación JavaFX de Interfaz Gráfica para el Analizador de Eficiencia.
 */
public class MainApp extends Application 
    {

        private Stage primaryStage; 
        
        // Tamaños de n para simular el creicmiento en las gráficas
        private final int[] TAMANOS_GRAFICA = {1000, 2000, 4000, 6000, 8000, 10000};
        
        // Factores de escala
        private final double FACTOR_N_LOG_N = 0.00005; // Para QuickSort, MergeSort
        private final double FACTOR_CUADRATICO = 0.000001; // Para Bubble, Selection, Insertion

        @Override
        public void start(Stage primaryStage) 
            {
                this.primaryStage = primaryStage;
                primaryStage.setTitle("Analizador de Eficiencia");
                primaryStage.setScene(createHomeScreen());
                primaryStage.show();
            }

        // Pantalla de Inicio
        private Scene createHomeScreen() 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(40);
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

                Button startButton = new Button("Iniciar Ejecución");
                startButton.getStyleClass().add("action-button-blue");

                // Asocioación con Pantalla de Resultados
                startButton.setOnAction(e -> primaryStage.setScene(createResultsScreen()));

                mainContent.getChildren().addAll(welcomeTitle, inputSection, startButton);
                root.setCenter(mainContent);

                Scene scene = new Scene(root, 800, 500);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }

        // Pantalla de Resusltados
        private Scene createResultsScreen() 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(25);
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
                filterCombo.setValue("BubbleSort"); // Valor por defecto
                filterCombo.getStyleClass().add("filter-combo");
                
                Button viewGrowthChartButton = new Button("Ver Gráfica de Crecimiento");
                viewGrowthChartButton.getStyleClass().add("action-button-green");
                
                filterBox.getChildren().addAll(filterLabel, filterCombo, viewGrowthChartButton);

                GridPane resultsGrid = createResultsGrid(); // Tabla de resultados
                
                mainContent.getChildren().addAll(resultsTitle, filterBox, resultsGrid);
                root.setCenter(mainContent);

                // Barra de botones inferiores
                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));

                Button exportButton = new Button("Exportar archivo de datos");
                exportButton.getStyleClass().add("action-button-blue");
                
                Button graphsButton = new Button("Ver Gráfica Comparativa (TODOS)");
                graphsButton.getStyleClass().add("action-button-blue");

                Button exitButton = new Button("Salir");
                exitButton.getStyleClass().add("action-button-red");
                
                // Asociación con Pantalla de gráficos generales
                graphsButton.setOnAction(e -> primaryStage.setScene(createComparativeChartScreen())); 
                // Asociación con Pantalla de gráficas individuales
                viewGrowthChartButton.setOnAction(e -> {
                    String selectedAlgorithm = filterCombo.getValue();
                    primaryStage.setScene(createIndividualChartScreen(selectedAlgorithm));
                });
                // Asociación con Pantalla de Inicio
                exitButton.setOnAction(e -> primaryStage.setScene(createHomeScreen()));     

                buttonBar.getChildren().addAll(exportButton, graphsButton, exitButton);
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }

        // Pabtalla de gráficos generales
        private Scene createComparativeChartScreen() 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(25);
                mainContent.setAlignment(Pos.TOP_CENTER);
                mainContent.setPadding(new Insets(25, 40, 40, 40));

                Label chartTitleLabel = new Label("Comparativa de Crecimiento por Complejidad");
                chartTitleLabel.getStyleClass().add("results-title");

                // Creación de gráficas de líneas
                NumberAxis xAxis = new NumberAxis();
                xAxis.setLabel("Tamaño del Arreglo (n)");
                xAxis.getStyleClass().add("axis-label");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Tiempo (ms) - Simulado");
                yAxis.getStyleClass().add("axis-label");

                LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Rendimiento Simulado O(n^2) vs O(n log n)");
                lineChart.getStyleClass().add("chart-title");


                lineChart.getData().addAll
                    (
                        createSeries("BubbleSort", true),  // true = Cuadrático
                        createSeries("QuickSort", false), // false = nlogn
                        createSeries("SelectionSort", true),
                        createSeries("MergeSort", false),
                        createSeries("InsertionSort", true)
                    );

                mainContent.getChildren().addAll(chartTitleLabel, lineChart);
                root.setCenter(mainContent);

                // Barra de botones inferiores
                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));
                Button backButton = new Button("Ver tabla de datos");
                backButton.getStyleClass().add("action-button-blue");
                backButton.setOnAction(e -> primaryStage.setScene(createResultsScreen())); // Volver a Pantalla 2
                buttonBar.getChildren().add(backButton);
                
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }
        
        // Pantallas de gráficos individuales
        private Scene createIndividualChartScreen(String algorithmName) 
            {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;");
                root.setTop(createHeader());

                VBox mainContent = new VBox(25);
                mainContent.setAlignment(Pos.TOP_CENTER);
                mainContent.setPadding(new Insets(25, 40, 40, 40));

                Label chartTitleLabel = new Label("Comportamiento de: " + algorithmName);
                chartTitleLabel.getStyleClass().add("results-title");

                // Creación de gráficos
                NumberAxis xAxis = new NumberAxis();
                xAxis.setLabel("Tamaño del Arreglo (n)");
                xAxis.getStyleClass().add("axis-label");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Tiempo (ms) - Simulado");
                yAxis.getStyleClass().add("axis-label");

                LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
                String complexity = (algorithmName.equals("QuickSort") || algorithmName.equals("MergeSort")) ? "O(n log n)" : "O(n^2)";
                lineChart.setTitle("Simulación de Complejidad: " + complexity);
                lineChart.getStyleClass().add("chart-title");
                lineChart.setLegendVisible(false);

                boolean isQuadratic = !complexity.equals("O(n log n)");
                lineChart.getData().add(createSeries(algorithmName, isQuadratic));
                
                mainContent.getChildren().addAll(chartTitleLabel, lineChart);
                root.setCenter(mainContent);

                HBox buttonBar = new HBox(20);
                buttonBar.setAlignment(Pos.CENTER);
                buttonBar.setPadding(new Insets(0, 0, 30, 0));
                Button backButton = new Button("Ver tabla de datos");
                backButton.getStyleClass().add("action-button-blue");
                backButton.setOnAction(e -> primaryStage.setScene(createResultsScreen()));
                buttonBar.getChildren().add(backButton);
                
                root.setBottom(buttonBar);

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return scene;
            }


        // --- MÉTODOS DE AYUDA PARA COMPORTAMIENTO DE GRÁFICOS

        /**
         * Genera una serie de datos (XYChart.Series) para una gráfica.
         * @param name Nombre del algoritmo.
         * @param isQuadratic true si es O(n^2), false si es O(n log n).
         * @return Una serie de datos lista para la gráfica.
         */
        private XYChart.Series<Number, Number> createSeries(String name, boolean isQuadratic) 
            {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(name);
                
                for (int n : TAMANOS_GRAFICA) 
                    {
                        double time;
                        if (isQuadratic) 
                            {
                                time = (double)n * n * FACTOR_CUADRATICO;
                            } 
                        else 
                            {
                                time = (double)n * Math.log(n) * FACTOR_N_LOG_N;
                            }
                        series.getData().add(new XYChart.Data<>(n, time));
                    }
                return series;
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
         * Método de ayuda para crear la tabla de resultados (Pantalla de Rwusltados)
         */
        private GridPane createResultsGrid() 
            {
                GridPane resultsGrid = new GridPane();
                resultsGrid.setAlignment(Pos.CENTER);
                resultsGrid.setHgap(50); // Espacio horizontal
                resultsGrid.setVgap(12); // Espacio vertical
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

                // Datos de prueba
                resultsGrid.add(new Label("QuickSort"), 0, 1);
                resultsGrid.add(new Label("100,000"), 1, 1);
                resultsGrid.add(new Label("18 ms"), 2, 1);
                
                resultsGrid.add(new Label("MergeSort"), 0, 2);
                resultsGrid.add(new Label("100,000"), 1, 2);
                resultsGrid.add(new Label("25 ms"), 2, 2);

                resultsGrid.add(new Label("InsertionSort"), 0, 3);
                resultsGrid.add(new Label("100,000"), 1, 3);
                resultsGrid.add(createTimeCell("7,200 ms", "(7.2 seg)"), 2, 3);
                
                resultsGrid.add(new Label("SelectionSort"), 0, 4);
                resultsGrid.add(new Label("100,000"), 1, 4);
                resultsGrid.add(createTimeCell("8,400 ms", "(8.4 seg)"), 2, 4);
                
                resultsGrid.add(new Label("BubbleSort"), 0, 5);
                resultsGrid.add(new Label("100,000"), 1, 5);
                resultsGrid.add(createTimeCell("10,100 ms", "(10.1 seg)"), 2, 5);
                
                return resultsGrid;
            }
        
        /**
         * Método de ayuda para crear la celda de tiempo con dos líneas
         */
        private VBox createTimeCell(String mainTime, String secondaryTime) 
            {
                VBox cell = new VBox(0);
                cell.setAlignment(Pos.CENTER_LEFT);
                Label mainLabel = new Label(mainTime);
                mainLabel.getStyleClass().add("time-main");
                
                Label secondaryLabel = new Label(secondaryTime);
                secondaryLabel.getStyleClass().add("time-secondary");
                
                cell.getChildren().addAll(mainLabel, secondaryLabel);
                return cell;
            }

        public static void main(String[] args) 
            {
                launch(args);
            }
    }
