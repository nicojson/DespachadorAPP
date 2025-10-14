package tecnm.celaya.edu.mx.despachadorapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Controlador para la simulación del algoritmo de planificación FIFO (First-In, First-Out).
 * <p>
 * Este controlador contiene la implementación completa de la simulación y sirve como
 * plantilla base para todos los demás algoritmos del proyecto.
 */
public class FifoController {

    //<editor-fold desc="FXML-Injected Fields">
    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, Integer> pidColumn;
    @FXML private TableColumn<Process, Integer> arrivalColumn;
    @FXML private TableColumn<Process, Integer> durationColumn;

    @FXML private Label timerLabel;
    @FXML private Label cpuProcessLabel;
    @FXML private Label memoryProcessLabel;

    @FXML private TableView<Process> processStatusTable;
    @FXML private TableColumn<Process, Integer> statusPidColumn;
    @FXML private TableColumn<Process, String> statusLocationColumn;
    @FXML private TableColumn<Process, String> statusStateColumn;
    @FXML private TableColumn<Process, Integer> statusDurationColumn;

    @FXML private VBox finishedProcessesVBox;
    @FXML private Button playPauseButton;
    //</editor-fold>

    // --- Variables de Simulación ---
    private Timeline timeline; // Controla el avance automático de la simulación (1 tick por segundo).
    private int timer = 0; // El tiempo actual de la simulación en segundos.
    private boolean isPaused = true; // Controla si la simulación está en pausa.

    // --- Listas y Colas de Procesos ---
    private ObservableList<Process> processList = FXCollections.observableArrayList(); // Lista original de procesos generados.
    private ObservableList<Process> processStatusList = FXCollections.observableArrayList(); // Lista vinculada a la tabla de estados.

    /** La cola de procesos listos en memoria. Para FIFO, se usa una Queue para asegurar el comportamiento "primero en entrar, primero en salir". */
    private Queue<Process> memoryQueue = new LinkedList<>();

    /** La lista de procesos que han finalizado. Se mantiene para preservar el orden de finalización. */
    private List<Process> finishedOrderList = new ArrayList<>();

    /** El proceso que se encuentra actualmente en la CPU. Es `null` si la CPU está libre. */
    private Process cpuProcess = null;

    /**
     * Método de inicialización, se llama automáticamente después de cargar el FXML.
     * Configura las tablas y arranca la simulación en un estado inicial.
     */
    @FXML
    public void initialize() {
        // Vincula las columnas de las tablas con las propiedades del objeto Process.
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        statusPidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        statusLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusStateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        statusDurationColumn.setCellValueFactory(new PropertyValueFactory<>("remainingDuration"));

        // Asigna las listas de datos a las tablas.
        processTable.setItems(processList);
        processStatusTable.setItems(processStatusList);

        // Configura el motor de la simulación y la arranca.
        setupTimeline();
        onRestartButtonClick(); // Llama a restart para la carga inicial.
    }

    /**
     * Crea una lista de 5 procesos con tiempos de llegada y duraciones aleatorias.
     */
    private void generateRandomProcesses() {
        processList.clear();
        Random rand = new Random();
        int arrivalTime = 0;
        for (int i = 1; i <= 5; i++) {
            processList.add(new Process(i, arrivalTime, rand.nextInt(5) + 2));
            arrivalTime += rand.nextInt(3);
        }
        processStatusList.setAll(processList);
    }

    /**
     * Configura el Timeline para que ejecute el método `runSimulationStep` cada segundo.
     */
    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!isPaused) {
                runSimulationStep(true);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * El corazón de la simulación. Implementa la arquitectura de "re-simulación".
     * En cada tick, en lugar de solo avanzar un paso, recalcula todo el estado desde el segundo 0
     * hasta el tiempo actual. Esto permite que los botones de "atrás" y "adelante" funcionen fácilmente.
     *
     * @param forward `true` si la simulación avanza, `false` si retrocede.
     */
    private void runSimulationStep(boolean forward) {
        if (forward) timer++;
        else timer--;

        // 1. Resetear el estado: Se limpian todas las variables de estado para la re-simulación.
        cpuProcess = null;
        memoryQueue.clear();
        finishedOrderList.clear();
        processStatusList.forEach(p -> {
            p.setLocation("");
            p.setState("");
            p.setRemainingDuration(p.getDuration());
        });

        // 2. Bucle de Re-simulación: Se itera desde el tiempo 0 hasta el tiempo actual.
        for (int t = 0; t <= timer; t++) {
            final int currentTick = t;

            // 2a. Llegada de procesos: Se añaden a la memoria los procesos cuyo tiempo de llegada coincide con el tick actual.
            processList.stream()
                    .filter(p -> p.getArrivalTime() == currentTick)
                    .forEach(p -> {
                        if (!memoryQueue.contains(p) && !p.getState().equals("F")) {
                            p.setLocation("Memoria");
                            p.setState("W");
                            memoryQueue.add(p);
                        }
                    });

            // 2b. Lógica del Planificador: Si la CPU está libre, se elige el siguiente proceso.
            if (cpuProcess == null && !memoryQueue.isEmpty()) {
                // *** LÓGICA FIFO ***
                // Se extrae el primer elemento de la cola (el que llegó primero).
                cpuProcess = memoryQueue.poll();
                cpuProcess.setLocation("CPU");
                cpuProcess.setState("X");
            }

            // 2c. Trabajo de la CPU: Si hay un proceso en la CPU, se decrementa su duración restante.
            if (cpuProcess != null) {
                cpuProcess.setRemainingDuration(cpuProcess.getRemainingDuration() - 1);

                // 2d. Finalización de Proceso: Si un proceso termina, se mueve a la lista de finalizados.
                if (cpuProcess.getRemainingDuration() <= 0) {
                    cpuProcess.setState("F");
                    cpuProcess.setLocation("Salida");
                    finishedOrderList.add(cpuProcess); // Se añade a la lista para mantener el orden.
                    cpuProcess = null; // La CPU queda libre.
                }
            }
        }

        // 3. Actualizar la UI con el estado recién calculado.
        updateUI();
    }

    /**
     * Actualiza todos los componentes de la interfaz gráfica para reflejar el estado actual de la simulación.
     */
    private void updateUI() {
        timerLabel.setText(String.valueOf(timer));

        // Actualizar etiqueta de la CPU.
        if (cpuProcess != null) {
            cpuProcessLabel.setText("PID: " + cpuProcess.getPid());
        } else {
            cpuProcessLabel.setText("Libre");
        }

        // Actualizar etiqueta de la Memoria.
        StringBuilder memoryText = new StringBuilder();
        for (Process p : memoryQueue) {
            memoryText.append("PID: ").append(p.getPid()).append(" ");
        }
        memoryProcessLabel.setText(memoryText.length() > 0 ? memoryText.toString() : "Vacía");

        // Actualizar la caja de procesos finalizados, respetando el orden de finalización.
        finishedProcessesVBox.getChildren().clear();
        for (Process p : finishedOrderList) {
            Text textNode = new Text("PID: " + p.getPid());
            textNode.getStyleClass().add("finished-process-text");
            finishedProcessesVBox.getChildren().add(textNode);
        }

        // Forzar a la tabla de estados a redibujarse para mostrar los cambios.
        processStatusTable.refresh();

        // Comprobar si la simulación ha terminado.
        boolean allFinished = finishedOrderList.size() == processList.size();
        if (allFinished && timer > 0) {
            timeline.stop();
            playPauseButton.setText("Inicio");
            isPaused = true;
            cpuProcessLabel.setText("Finalizado");
        }
    }

    //<editor-fold desc="Event Handlers for Control Buttons">
    @FXML
    private void onPlayPauseButtonClick() {
        isPaused = !isPaused;
        if (isPaused) {
            timeline.pause();
            playPauseButton.setText("Inicio");
        } else {
            timeline.play();
            playPauseButton.setText("Pausa");
        }
    }

    @FXML
    private void onNextButtonClick() {
        if (isPaused) {
            runSimulationStep(true);
        }
    }

    @FXML
    private void onBackButtonClick() {
        if (isPaused && timer > 0) {
            runSimulationStep(false);
        }
    }

    @FXML
    private void onRestartButtonClick() {
        timeline.stop();
        timer = 0;
        isPaused = true;
        playPauseButton.setText("Inicio");

        generateRandomProcesses();

        // Resetear el estado de la simulación y la UI al estado inicial (t=0).
        cpuProcess = null;
        memoryQueue.clear();
        finishedOrderList.clear();
        finishedProcessesVBox.getChildren().clear();
        runSimulationStep(false); // Corre la simulación a t=-1 para que el estado en t=0 sea el inicial.
        timer = 0; // Restablece el timer a 0.
        updateUI();
    }
    //</editor-fold>
}
