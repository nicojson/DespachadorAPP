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
import java.util.Random;

/**
 * Controlador para la simulación del algoritmo de planificación Round Robin con una cola de listos LIFO.
 * <p>
 * Este es el algoritmo más complejo. Introduce el concepto de "quantum" de tiempo y la expulsión
 * (preemption) de procesos de la CPU.
 */
public class RoundRobinLifoController {

    /** El quantum de tiempo. Un proceso solo puede estar en la CPU por este número de segundos antes de ser expulsado. */
    private static final int QUANTUM = 3;

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

    private Timeline timeline;
    private int timer = 0;
    private boolean isPaused = true;

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private ObservableList<Process> processStatusList = FXCollections.observableArrayList();

    /** La cola de memoria. Se usa una LinkedList como Pila para la política LIFO. */
    private LinkedList<Process> memoryQueue = new LinkedList<>();
    private List<Process> finishedOrderList = new ArrayList<>();
    private Process cpuProcess = null;

    /** Contador para el quantum del proceso actual en la CPU. */
    private int quantumCounter = 0;

    @FXML
    public void initialize() {
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        statusPidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        statusLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusStateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        statusDurationColumn.setCellValueFactory(new PropertyValueFactory<>("remainingDuration"));

        processTable.setItems(processList);
        processStatusTable.setItems(processStatusList);

        setupTimeline();
        onRestartButtonClick();
    }

    private void generateRandomProcesses() {
        processList.clear();
        Random rand = new Random();
        int arrivalTime = 0;
        // Generar procesos con duraciones más largas para que la expulsión del Round Robin sea más evidente.
        for (int i = 1; i <= 5; i++) {
            processList.add(new Process(i, arrivalTime, rand.nextInt(6) + 3));
            arrivalTime += rand.nextInt(3);
        }
        processStatusList.setAll(processList);
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!isPaused) {
                runSimulationStep(true);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void runSimulationStep(boolean forward) {
        if (forward) timer++;
        else timer--;

        cpuProcess = null;
        memoryQueue.clear();
        quantumCounter = 0;
        finishedOrderList.clear();
        processStatusList.forEach(p -> {
            p.setLocation("");
            p.setState("");
            p.setRemainingDuration(p.getDuration());
        });

        for (int t = 0; t <= timer; t++) {
            final int currentTick = t;

            // 1. Llegada de procesos (LIFO)
            processList.stream()
                    .filter(p -> p.getArrivalTime() == currentTick)
                    .forEach(p -> {
                        if (!p.getState().equals("F")) {
                            p.setLocation("Memoria");
                            p.setState("W");
                            memoryQueue.remove(p);
                            memoryQueue.addFirst(p);
                        }
                    });

            // *** LÓGICA ROUND ROBIN ***
            // 2. Expulsión por Quantum: Si un proceso ha agotado su quantum, es expulsado de la CPU.
            if (cpuProcess != null && quantumCounter >= QUANTUM) {
                // Solo se devuelve a memoria si aún no ha terminado.
                if (cpuProcess.getRemainingDuration() > 0) {
                    cpuProcess.setLocation("Memoria");
                    cpuProcess.setState("W");
                    memoryQueue.addFirst(cpuProcess); // Vuelve al principio de la cola LIFO.
                }
                cpuProcess = null; // La CPU queda libre.
            }

            // 3. Asignación de CPU (LIFO)
            if (cpuProcess == null && !memoryQueue.isEmpty()) {
                cpuProcess = memoryQueue.removeFirst();
                cpuProcess.setLocation("CPU");
                cpuProcess.setState("X");
                quantumCounter = 0; // Se resetea el contador de quantum para el nuevo proceso.
            }

            // 4. Trabajo de la CPU
            if (cpuProcess != null) {
                cpuProcess.setRemainingDuration(cpuProcess.getRemainingDuration() - 1);
                quantumCounter++; // Se incrementa el contador de quantum del proceso actual.

                // 5. Finalización de Proceso
                if (cpuProcess.getRemainingDuration() <= 0) {
                    cpuProcess.setState("F");
                    cpuProcess.setLocation("Salida");
                    finishedOrderList.add(cpuProcess);
                    cpuProcess = null; // La CPU queda libre.
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        timerLabel.setText(String.valueOf(timer));

        // Para Round Robin, la etiqueta de la CPU muestra el progreso del quantum.
        if (cpuProcess != null) {
            cpuProcessLabel.setText("PID: " + cpuProcess.getPid() + " (" + quantumCounter + "/" + QUANTUM + ")");
        } else {
            cpuProcessLabel.setText("Libre");
        }

        StringBuilder memoryText = new StringBuilder();
        for (Process p : memoryQueue) {
            memoryText.append("PID: ").append(p.getPid()).append(" ");
        }
        memoryProcessLabel.setText(memoryText.length() > 0 ? memoryText.toString() : "Vacía");

        finishedProcessesVBox.getChildren().clear();
        for (Process p : finishedOrderList) {
            Text textNode = new Text("PID: " + p.getPid());
            textNode.getStyleClass().add("finished-process-text");
            finishedProcessesVBox.getChildren().add(textNode);
        }

        processStatusTable.refresh();

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

        cpuProcess = null;
        memoryQueue.clear();
        quantumCounter = 0; // Resetear el contador de quantum.
        finishedOrderList.clear();
        finishedProcessesVBox.getChildren().clear();
        runSimulationStep(false);
        timer = 0;
        updateUI();
    }
    //</editor-fold>
}
