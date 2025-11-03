package presentacion;

import abstraccion.Resultado;
import control.ControladorEjecucion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task; 
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
import java.util.stream.Collectors;

/**
 * Clase principal de la aplicación JavaFX (Capa de Presentación).
 * --- RESPONSABILIDAD: Mostrar la UI, recibir entradas y delegar TODA la lógica
 * al ControladorEjecucion. ---
 */
public class InterfazPrincipal extends Application 
    {
        // El único punto de contacto con la lógica de control
        private final ControladorEjecucion controlador = new ControladorEjecucion();

        private Stage escenarioPrincipal;
        
        // Almacenamos las escenas para navegar entre ellas
        private Scene escenaInicio;
        private Scene escenaResultados;
        
        // Almacenará la lista compelta de resultados
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
                panelRaiz.getStyleClass().add("panel-raiz");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox();
                contenidoPrincipal.getStyleClass().add("contenido-principal");

                Label tituloBienvenida = new Label("¡Bienvenido al Analizador de Eficiencia de Algoritmos!");
                tituloBienvenida.getStyleClass().add("titulo-bienvenida");

                HBox seccionEntrada = new HBox();
                seccionEntrada.getStyleClass().add("seccion-entrada");
                
                Label etiquetaEntrada = new Label("Por favor ingresa el tamaño del arreglo a analizar:");
                etiquetaEntrada.getStyleClass().add("etiqueta-entrada");
                
                TextField campoTamano = new TextField();
                campoTamano.setPromptText("Ingrese aquí");
                campoTamano.getStyleClass().add("campo-texto");
                campoTamano.setPrefWidth(150);
                seccionEntrada.getChildren().addAll(etiquetaEntrada, campoTamano);

                Label etiquetaNotificacion = new Label();
                etiquetaNotificacion.getStyleClass().add("etiqueta-notificacion");
                
                Button botonInicio = new Button("Iniciar Ejecución");
                botonInicio.getStyleClass().addAll("button", "boton-primario");

                botonInicio.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(campoTamano, etiquetaNotificacion, botonInicio);
                    });
                
                campoTamano.setOnAction(e -> 
                    {
                        ejecutarLogicaInicio(campoTamano, etiquetaNotificacion, botonInicio);
                    });

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
                        etiquetaNotificacion.getStyleClass().remove("notificacion-exito");
                        etiquetaNotificacion.getStyleClass().add("notificacion-error");
                        return;
                    }

                etiquetaNotificacion.setText("Analizando... esto puede tardar un poco, gracias por tu paciencia (:");
                etiquetaNotificacion.getStyleClass().remove("notificacion-error");
                etiquetaNotificacion.getStyleClass().add("notificacion-exito");
                botonInicio.setDisable(true);
                campoTamano.setDisable(true);

                Task<List<Resultado>> tareaAnalisis = new Task<>() 
                    {
                        @Override
                        protected List<Resultado> call() throws Exception 
                            {
                                controlador.iniciarComparacion(n);
                                return controlador.getResultadosCompletos();
                            }
                    };

                tareaAnalisis.setOnSucceeded(workerStateEvent -> 
                    {
                        this.todosLosResultados = tareaAnalisis.getValue();
                        this.escenaResultados = crearEscenaResultados(); 
                        escenarioPrincipal.setScene(this.escenaResultados);
                        
                        botonInicio.setDisable(false);
                        campoTamano.setDisable(false);
                        campoTamano.clear();
                        etiquetaNotificacion.setText("");
                    });

                tareaAnalisis.setOnFailed(workerStateEvent -> 
                    {
                        etiquetaNotificacion.setText("Error: Ocurrió un fallo durante el análisis.");
                        etiquetaNotificacion.getStyleClass().remove("notificacion-exito");
                        etiquetaNotificacion.getStyleClass().add("notificacion-error");
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
                panelRaiz.getStyleClass().add("panel-raiz");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox();
                contenidoPrincipal.getStyleClass().add("contenido-principal");

                Label tituloResultados = new Label("Resultados de la ejecución");
                tituloResultados.getStyleClass().add("titulo-seccion");

                HBox panelFiltros = new HBox();
                panelFiltros.getStyleClass().add("panel-filtros");
                
                Label etiquetaFiltroAlgo = new Label("Ver comportamiento de:");
                etiquetaFiltroAlgo.getStyleClass().add("etiqueta-entrada");
                
                ComboBox<String> comboAlgoritmo = new ComboBox<>();
                comboAlgoritmo.setItems(FXCollections.observableArrayList(
                    "BubbleSort", "InsertionSort", "SelectionSort", "MergeSort", "QuickSort"
                ));
                comboAlgoritmo.setValue("BubbleSort");
                comboAlgoritmo.getStyleClass().add("combo-box");

                Label etiquetaFiltroCaso = new Label("Caso:");
                etiquetaFiltroCaso.getStyleClass().add("etiqueta-entrada");
                ComboBox<String> comboCaso = new ComboBox<>();
                
                comboCaso.setItems(FXCollections.observableArrayList("promedio", "mejor", "peor"));
                comboCaso.setValue("promedio"); 
                comboCaso.getStyleClass().add("combo-box");
                
                panelFiltros.getChildren().addAll(etiquetaFiltroAlgo, comboAlgoritmo, etiquetaFiltroCaso, comboCaso);

                Button botonGraficaCrecimiento = new Button("Ver Gráfica de Crecimiento");
                botonGraficaCrecimiento.getStyleClass().addAll("button", "boton-secundario");

                Label etiquetaNotificacion = new Label();
                etiquetaNotificacion.getStyleClass().add("etiqueta-notificacion");
                etiquetaNotificacion.setMinHeight(20); 

                GridPane tablaResultados = crearTablaResultados(comboCaso.getValue()); 
                
                contenidoPrincipal.getChildren().addAll(tituloResultados, panelFiltros, botonGraficaCrecimiento, etiquetaNotificacion, tablaResultados);
                panelRaiz.setCenter(contenidoPrincipal);

                HBox barraBotones = new HBox();
                barraBotones.getStyleClass().add("barra-botones");

                Button botonExportar = new Button("Exportar archivo de datos");
                botonExportar.getStyleClass().addAll("button", "boton-secundario");

                Button botonGraficaComparativa = new Button("Ver Gráfica Comparativa");
                botonGraficaComparativa.getStyleClass().addAll("button", "boton-primario");

                Button botonSalir = new Button("Salir");
                botonSalir.getStyleClass().addAll("button", "boton-peligro"); 

                comboCaso.setOnAction(e -> 
                {
                    String casoSeleccionado = comboCaso.getValue();
                    contenidoPrincipal.getChildren().set(4, crearTablaResultados(casoSeleccionado));
                });
                
                botonExportar.setOnAction(e -> 
                {
                    String msg = controlador.exportarReportesCSV();
                    etiquetaNotificacion.setText(msg);
                    if (msg.startsWith("Error")) 
                    {
                        etiquetaNotificacion.getStyleClass().remove("notificacion-exito");
                        etiquetaNotificacion.getStyleClass().add("notificacion-error");
                    } 
                    else 
                    {
                        etiquetaNotificacion.getStyleClass().remove("notificacion-error");
                        etiquetaNotificacion.getStyleClass().add("notificacion-exito");
                    }
                });

                botonGraficaComparativa.setOnAction(e -> 
                {
                    String casoSeleccionado = comboCaso.getValue();
                    escenarioPrincipal.setScene(crearEscenaGraficaComparativa(casoSeleccionado));
                }); 
                
                botonGraficaCrecimiento.setOnAction(e -> 
                {
                    String algoSeleccionado = comboAlgoritmo.getValue();
                    String casoSeleccionado = comboCaso.getValue();
                    escenarioPrincipal.setScene(crearEscenaGraficaIndividual(algoSeleccionado, casoSeleccionado));
                });
                
                botonSalir.setOnAction(e -> 
                {
                    controlador.limpiarResultados();
                    this.todosLosResultados = null; 
                    escenarioPrincipal.setScene(this.escenaInicio);    
                });

                barraBotones.getChildren().addAll(botonExportar, botonGraficaComparativa, botonSalir);
                panelRaiz.setBottom(barraBotones);

                Scene escena = new Scene(panelRaiz, 800, 650); 
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }

        private Scene crearEscenaGraficaComparativa(String caso) 
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.getStyleClass().add("panel-raiz");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox();
                contenidoPrincipal.getStyleClass().add("contenido-principal");

                Label etiquetaTituloGrafica = new Label("Comparativa de Crecimiento (" + caso + ")");
                etiquetaTituloGrafica.getStyleClass().add("titulo-seccion");

                NumberAxis ejeX = new NumberAxis();
                ejeX.setLabel("Tamaño del Arreglo (n)");
                ejeX.getStyleClass().add("eje-etiqueta");

                NumberAxis ejeY = new NumberAxis();
                ejeY.setLabel("Tiempo (ms) - Real");
                ejeY.getStyleClass().add("eje-etiqueta");

                LineChart<Number, Number> graficaLinea = new LineChart<>(ejeX, ejeY);
                graficaLinea.setTitle("Rendimiento Real de Algoritmos (" + caso + ")");
                graficaLinea.getStyleClass().add("titulo-grafica");

                Map<String, XYChart.Series<Number, Number>> mapaSeries = new HashMap<>();
                if (this.todosLosResultados != null)
                {
                    for (Resultado r : this.todosLosResultados)
                    {
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
                }
                graficaLinea.getData().addAll(mapaSeries.values());

                contenidoPrincipal.getChildren().addAll(etiquetaTituloGrafica, graficaLinea);
                panelRaiz.setCenter(contenidoPrincipal);

                HBox barraBotones = new HBox();
                barraBotones.getStyleClass().add("barra-botones");
                
                Button botonVolver = new Button("Ver tabla de datos");
                botonVolver.getStyleClass().addAll("button", "boton-primario");
                botonVolver.setOnAction(e -> escenarioPrincipal.setScene(this.escenaResultados));
                barraBotones.getChildren().add(botonVolver);
                
                panelRaiz.setBottom(barraBotones);

                Scene escena = new Scene(panelRaiz, 800, 600);
                escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                return escena;
            }
        
        private Scene crearEscenaGraficaIndividual(String nombreAlgoritmo, String caso) 
            {
                BorderPane panelRaiz = new BorderPane();
                panelRaiz.getStyleClass().add("panel-raiz");
                panelRaiz.setTop(crearEncabezado());

                VBox contenidoPrincipal = new VBox();
                contenidoPrincipal.getStyleClass().add("contenido-principal");

                Label etiquetaTituloGrafica = new Label("Comportamiento de: " + nombreAlgoritmo + " (" + caso + ")");
                etiquetaTituloGrafica.getStyleClass().add("titulo-seccion");

                NumberAxis ejeX = new NumberAxis();
                ejeX.setLabel("Tamaño del Arreglo (n)");
                ejeX.getStyleClass().add("eje-etiqueta");

                NumberAxis ejeY = new NumberAxis();
                ejeY.setLabel("Tiempo (ms) - Real");
                ejeY.getStyleClass().add("eje-etiqueta");

                LineChart<Number, Number> graficaLinea = new LineChart<>(ejeX, ejeY);
                graficaLinea.setTitle("Rendimiento Real: " + nombreAlgoritmo + " (" + caso + ")");
                graficaLinea.getStyleClass().add("titulo-grafica");
                graficaLinea.setLegendVisible(false);

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(nombreAlgoritmo);
                
                if (this.todosLosResultados != null)
                {
                    for (Resultado r : this.todosLosResultados) 
                    {
                        if (r.getAlgoritmo().equals(nombreAlgoritmo) && r.getCaso().equals(caso)) 
                        {
                            series.getData().add(new XYChart.Data<>(r.getTamano(), r.getTiempoMs()));
                        }
                    }
                }
                graficaLinea.getData().add(series);
                
                contenidoPrincipal.getChildren().addAll(etiquetaTituloGrafica, graficaLinea);
                panelRaiz.setCenter(contenidoPrincipal);

                HBox barraBotones = new HBox();
                barraBotones.getStyleClass().add("barra-botones");
                
                Button botonVolver = new Button("Ver tabla de datos");
                botonVolver.getStyleClass().addAll("button", "boton-primario");
                botonVolver.setOnAction(e -> escenarioPrincipal.setScene(this.escenaResultados));
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
                encabezado.getStyleClass().add("encabezado");
                
                Label tituloApp = new Label("Analizador de Eficiencia de Algoritmos");
                tituloApp.getStyleClass().add("app-title");
                Label subtituloApp = new Label("Por Isai Zurita");
                subtituloApp.getStyleClass().add("app-subtitle");
                
                encabezado.getChildren().addAll(tituloApp, subtituloApp);
                
                return encabezado;
            }
        
        /**
         * Método de ayuda para crear la tabla de resultados (Pantalla de Resultados)
         */
        private GridPane crearTablaResultados(String caso) 
            {
                GridPane tablaResultados = new GridPane();
                tablaResultados.getStyleClass().add("tabla-resultados");

                Label etiquetaCabeceraAlg = new Label("Algoritmo");
                etiquetaCabeceraAlg.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraAlg, 0, 0);

                Label etiquetaCabeceraTam = new Label("Tamaño");
                etiquetaCabeceraTam.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraTam, 1, 0);

                Label etiquetaCabeceraTiempo = new Label("Tiempo");
                etiquetaCabeceraTiempo.getStyleClass().add("grid-header");
                tablaResultados.add(etiquetaCabeceraTiempo, 2, 0);

                if (todosLosResultados != null && !todosLosResultados.isEmpty()) 
                    {
                        int nMaximo = 0;
                        for (Resultado r : todosLosResultados) 
                        {
                            nMaximo = Math.max(nMaximo, r.getTamano());
                        }

                        final int finalNMaximo = nMaximo; 
                        List<Resultado> resultadosFinales = this.todosLosResultados.stream()
                            .filter(r -> r.getTamano() == finalNMaximo && r.getCaso().equals(caso))
                            .collect(Collectors.toList());
                        
                        int indiceFila = 1;
                        for (Resultado r : resultadosFinales)
                        {
                            Label etiquetaAlgoritmo = new Label(r.getAlgoritmo());
                            etiquetaAlgoritmo.getStyleClass().add("celda-tabla");
                            tablaResultados.add(etiquetaAlgoritmo, 0, indiceFila);
                            
                            Label etiquetaTamano = new Label(String.format("%,d", r.getTamano()));
                            etiquetaTamano.getStyleClass().add("celda-tabla");
                            tablaResultados.add(etiquetaTamano, 1, indiceFila);
                            
                            tablaResultados.add(crearCeldaTiempo(r.getTiempoMs()), 2, indiceFila);
                            
                            indiceFila++;
                        }
                    }
                
                return tablaResultados;
            }
        
        /**
         * Método de ayuda para crear la celda de tiempo con dos líneas
         * @param timeMs El tiempo real en milisegundos
         */
        private VBox crearCeldaTiempo(double timeMs) 
            {
                VBox celda = new VBox(0);
                celda.getStyleClass().add("celda-tiempo-contenedor");
                
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
 * $ export PATH_TO_FX="/Users/isaizurita/Documents/javafx-sdk-21.0.9/lib"
 * $ javac --module-path $PATH_TO_FX --add-modules javafx.controls \
   $ -d out \
 * $ abstraccion/*.java control/*.java presentacion/*.java
 * 
 * cp presentacion/styles.css out/presentacion/
 * 
 * $ java --module-path $PATH_TO_FX --add-modules javafx.controls \
 * $ -cp out \
 * $ presentacion.MainApp
 */

 // javac --module-path $PATH_TO_FX --add-modules javafx.controls -d out abstraccion/*.java control/*.java presentacion/*.java
 // cp presentacion/styles.css out/presentacion/
 // java --module-path $PATH_TO_FX --add-modules javafx.controls -cp out presentacion.MainApp