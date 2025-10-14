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
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SjfController {

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

    private Timeline timeline;
    private int timer = 0;
    private boolean isPaused = true;

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private ObservableList<Process> processStatusList = FXCollections.observableArrayList();
    private List<Process> memoryQueue = new ArrayList<>();
    private List<Process> finishedOrderList = new ArrayList<>();
    private Process cpuProcess = null;

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
        for (int i = 1; i <= 5; i++) {
            processList.add(new Process(i, arrivalTime, rand.nextInt(5) + 2));
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
        finishedOrderList.clear();
        processStatusList.forEach(p -> {
            p.setLocation("");
            p.setState("");
            p.setRemainingDuration(p.getDuration());
        });

        for (int t = 0; t <= timer; t++) {
            final int currentTick = t;
            processList.stream()
                    .filter(p -> p.getArrivalTime() == currentTick)
                    .forEach(p -> {
                        if (!memoryQueue.contains(p) && !p.getState().equals("F")) {
                            p.setLocation("Memoria");
                            p.setState("W");
                            memoryQueue.add(p);
                        }
                    });

            if (cpuProcess == null && !memoryQueue.isEmpty()) {
                memoryQueue.sort(Comparator.comparingInt(Process::getDuration));
                cpuProcess = memoryQueue.remove(0);
                cpuProcess.setLocation("CPU");
                cpuProcess.setState("X");
            }

            if (cpuProcess != null) {
                cpuProcess.setRemainingDuration(cpuProcess.getRemainingDuration() - 1);

                if (cpuProcess.getRemainingDuration() <= 0) {
                    cpuProcess.setState("F");
                    cpuProcess.setLocation("Salida");
                    finishedOrderList.add(cpuProcess);
                    cpuProcess = null;
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        timerLabel.setText(String.valueOf(timer));

        if (cpuProcess != null) {
            cpuProcessLabel.setText("PID: " + cpuProcess.getPid());
        } else {
            cpuProcessLabel.setText("Libre");
        }

        StringBuilder memoryText = new StringBuilder();
        memoryQueue.sort(Comparator.comparingInt(Process::getDuration));
        for (Process p : memoryQueue) {
            memoryText.append("PID: ").append(p.getPid()).append("(").append(p.getDuration()).append(") ");
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
        finishedOrderList.clear();
        finishedProcessesVBox.getChildren().clear();
        runSimulationStep(false);
        timer = 0;
        updateUI();
    }
}
