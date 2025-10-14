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
import java.util.List;
import java.util.Random;

public abstract class BaseController {

    @FXML protected TableView<Process> processTable;
    @FXML protected TableColumn<Process, Integer> pidColumn;
    @FXML protected TableColumn<Process, Integer> arrivalColumn;
    @FXML protected TableColumn<Process, Integer> durationColumn;

    @FXML protected Label timerLabel;
    @FXML protected Label cpuProcessLabel;
    @FXML protected Label memoryProcessLabel;

    @FXML protected TableView<Process> processStatusTable;
    @FXML protected TableColumn<Process, Integer> statusPidColumn;
    @FXML protected TableColumn<Process, String> statusLocationColumn;
    @FXML protected TableColumn<Process, String> statusStateColumn;
    @FXML protected TableColumn<Process, Integer> statusDurationColumn;

    @FXML protected VBox finishedProcessesVBox;
    @FXML protected Button playPauseButton;

    protected Timeline timeline;
    protected int timer = 0;
    protected boolean isPaused = true;

    protected ObservableList<Process> processList = FXCollections.observableArrayList();
    protected ObservableList<Process> processStatusList = FXCollections.observableArrayList();
    protected List<Process> finishedOrderList = new ArrayList<>();
    protected Process cpuProcess = null;

    @FXML
    public void initialize() {
        setupTables();
        setupTimeline();
        onRestartButtonClick();
    }

    protected void setupTables() {
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        statusPidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        statusLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusStateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        statusDurationColumn.setCellValueFactory(new PropertyValueFactory<>("remainingDuration"));

        statusLocationColumn.setCellFactory(col -> new UpdatingCell<>());
        statusStateColumn.setCellFactory(col -> new UpdatingCell<>());
        statusDurationColumn.setCellFactory(col -> new UpdatingCell<>());

        processTable.setItems(processList);
        processStatusTable.setItems(processStatusList);
    }

    protected void generateRandomProcesses() {
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

    protected abstract void runSimulationStep(boolean forward);

    protected String getCpuLabelText() {
        if (cpuProcess != null) {
            return "PID: " + cpuProcess.getPid();
        } else {
            return "Libre";
        }
    }

    protected abstract String getMemoryLabelText();

    protected void updateUI() {
        timerLabel.setText(String.valueOf(timer));
        cpuProcessLabel.setText(getCpuLabelText());
        memoryProcessLabel.setText(getMemoryLabelText());

        finishedProcessesVBox.getChildren().clear();
        for (Process p : finishedOrderList) {
            Text textNode = new Text("PID: " + p.getPid());
            textNode.getStyleClass().add("finished-process-text");
            finishedProcessesVBox.getChildren().add(textNode);
        }

        // Do NOT call refresh(). The properties will update the table automatically.

        boolean allFinished = finishedOrderList.size() == processList.size();
        if (allFinished && timer > 0) {
            timeline.stop();
            playPauseButton.setText("Inicio");
            isPaused = true;
            cpuProcessLabel.setText("Finalizado");
        }
    }

    @FXML
    protected void onPlayPauseButtonClick() {
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
    protected void onNextButtonClick() {
        if (isPaused) {
            runSimulationStep(true);
        }
    }

    @FXML
    protected void onBackButtonClick() {
        if (isPaused && timer > 0) {
            runSimulationStep(false);
        }
    }

    @FXML
    protected void onRestartButtonClick() {
        timeline.stop();
        timer = 0;
        isPaused = true;
        playPauseButton.setText("Inicio");

        generateRandomProcesses();

        cpuProcess = null;
        finishedOrderList.clear();
        finishedProcessesVBox.getChildren().clear();
        runSimulationStep(false);
        timer = 0;
        updateUI();
    }
}
