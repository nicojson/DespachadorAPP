package tecnm.celaya.edu.mx.despachadorapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
 * Controlador para la simulación del algoritmo de planificación LIFO (Last-In,
 * First-Out).
 * <p>
 * La estructura es idéntica a la del FifoController, pero la lógica de
 * selección de procesos cambia.
 */
public class LifoController {

    // <editor-fold desc="FXML-Injected Fields">
    @FXML
    private TableView<Process> processTable;
    @FXML
    private TableColumn<Process, Integer> pidColumn;
    @FXML
    private TableColumn<Process, Integer> arrivalColumn;
    @FXML
    private TableColumn<Process, Integer> sizeColumn; // New Column
    @FXML
    private TableColumn<Process, Integer> durationColumn;

    @FXML
    private Label timerLabel;
    @FXML
    private Label cpuProcessLabel;

    // Pagination Table
    @FXML
    private TableView<MemoryPage> paginationTable;
    @FXML
    private TableColumn<MemoryPage, Integer> pageNumberColumn;
    @FXML
    private TableColumn<MemoryPage, Integer> frameColumn;
    @FXML
    private TableColumn<MemoryPage, String> pageProcessColumn;
    @FXML
    private TableColumn<MemoryPage, Integer> freeSpaceColumn;
    @FXML
    private TableColumn<MemoryPage, Integer> usedSpaceColumn;

    @FXML
    private TableView<Process> processStatusTable;
    @FXML
    private TableColumn<Process, Integer> statusPidColumn;
    @FXML
    private TableColumn<Process, String> statusLocationColumn;
    @FXML
    private TableColumn<Process, String> statusStateColumn;
    @FXML
    private TableColumn<Process, Integer> statusDurationColumn;

    @FXML
    private VBox finishedProcessesVBox;
    @FXML
    private Button playPauseButton;
    // </editor-fold>

    private Timeline timeline;
    private int timer = 0;
    private boolean isPaused = true;

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private ObservableList<Process> processStatusList = FXCollections.observableArrayList();
    private ObservableList<MemoryPage> memoryPages = FXCollections.observableArrayList();

    /** La pila de memoria (LIFO) para procesos LISTOS para CPU. */
    private LinkedList<Process> memoryStack = new LinkedList<>();

    /** Referencias a los procesos en las páginas de memoria (8 slots). */
    private Process[] memorySlots = new Process[8];
    private final int PAGE_SIZE = 256;

    private List<Process> finishedOrderList = new ArrayList<>();
    private Process cpuProcess = null;

    @FXML
    public void initialize() {
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        statusPidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        statusLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusStateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        statusDurationColumn.setCellValueFactory(new PropertyValueFactory<>("remainingDuration"));

        // Pagination Table Setup
        pageNumberColumn.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
        pageNumberColumn.setCellFactory(column -> new UpdatingCell<>());

        frameColumn.setCellValueFactory(new PropertyValueFactory<>("frame"));
        frameColumn.setCellFactory(column -> new UpdatingCell<>());

        pageProcessColumn.setCellValueFactory(new PropertyValueFactory<>("processPid"));
        pageProcessColumn.setCellFactory(column -> new UpdatingCell<>());

        freeSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("freeSpace"));
        freeSpaceColumn.setCellFactory(column -> new UpdatingCell<>());

        usedSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("usedSpace"));
        usedSpaceColumn.setCellFactory(column -> new UpdatingCell<>());

        // Initialize 8 Slots (Page 1: 1-4, Page 2: 1-4)
        for (int i = 0; i < 4; i++)
            memoryPages.add(new MemoryPage(1, i + 1));
        for (int i = 0; i < 4; i++)
            memoryPages.add(new MemoryPage(2, i + 1));

        paginationTable.setItems(memoryPages);

        processTable.setItems(processList);
        processStatusTable.setItems(processStatusList);

        // Ensure simulation files directory exists
        new java.io.File("simulation_files").mkdirs();

        setupTimeline();
        onRestartButtonClick();
    }

    private void generateRandomProcesses() {
        processList.clear();
        Random rand = new Random();
        int arrivalTime = 0;
        for (int i = 1; i <= 5; i++) {
            Process p = new Process(i, arrivalTime, rand.nextInt(5) + 2);
            p.setSize(rand.nextInt(500) + 10); // Tamaño aleatorio mayor para probar multi-fragmento
            processList.add(p);
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
        if (forward)
            timer++;
        else
            timer--;

        cpuProcess = null;
        memoryStack.clear();
        finishedOrderList.clear();
        for (int i = 0; i < 8; i++)
            memorySlots[i] = null;

        processStatusList.forEach(p -> {
            p.setLocation("");
            p.setState("");
            p.setRemainingDuration(p.getDuration());
        });

        for (int t = 0; t <= timer; t++) {
            final int currentTick = t;

            // 1. Arrival & Memory Allocation
            processList.stream()
                    .filter(p -> p.getArrivalTime() <= currentTick && p.getState().equals(""))
                    .forEach(p -> {
                        int fragmentsNeeded = (int) Math.ceil((double) p.getSize() / PAGE_SIZE);

                        // Find contiguous or available slots? Problem says "Busca filas vacias".
                        // Assuming any available slots.
                        List<Integer> availableIndices = new ArrayList<>();
                        for (int i = 0; i < 8; i++) {
                            if (memorySlots[i] == null)
                                availableIndices.add(i);
                        }

                        if (availableIndices.size() >= fragmentsNeeded) {
                            // Allocate
                            createPhysicalFile(p);

                            for (int i = 0; i < fragmentsNeeded; i++) {
                                int slotIndex = availableIndices.get(i);
                                memorySlots[slotIndex] = p;
                            }

                            p.setLocation("Memoria");
                            p.setState("M");
                            memoryStack.push(p);
                        }
                    });

            // 2. CPU Scheduling (LIFO from Memory)
            if (cpuProcess == null && !memoryStack.isEmpty()) {
                cpuProcess = memoryStack.pop(); // Take the last one added
                cpuProcess.setLocation("CPU");
                cpuProcess.setState("X");

                // Free memory slots
                for (int i = 0; i < 8; i++) {
                    if (memorySlots[i] == cpuProcess)
                        memorySlots[i] = null;
                }
            }

            // 3. Execution
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

    private void createPhysicalFile(Process p) {
        java.io.File file = new java.io.File("simulation_files/P" + p.getPid() + ".txt");
        if (!file.exists()) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("ID: " + p.getPid());
                writer.println("Tamaño: " + p.getSize() + " KB");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI() {
        timerLabel.setText(String.valueOf(timer));

        if (cpuProcess != null) {
            cpuProcessLabel.setText("PID: " + cpuProcess.getPid());
        } else {
            cpuProcessLabel.setText("Libre");
        }

        // Update Pagination Table
        for (int i = 0; i < 8; i++) {
            updateMemoryPage(i, memorySlots[i]);
        }
        paginationTable.refresh();

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

    private void updateMemoryPage(int index, Process p) {
        MemoryPage page = memoryPages.get(index);
        if (p != null) {
            page.setProcessPid(String.valueOf(p.getPid()));

            // Calculate used space for this specific fragment
            // We need to know WHICH fragment of the process this is.
            // But simplified logic:
            // If it's a full fragment -> 256 Used.
            // If it's the last fragment -> Remainder.
            // Complex to track exactly which fragment is which without extra state.
            // For now, let's assume simple distribution or just show full size?
            // User example: "En el primer fragmento: Ocupado=256... En el segundo:
            // Ocupado=44"

            // To do this correctly, we need to count how many fragments of this process
            // we've seen so far?
            // Or calculate based on total size and index?
            // Let's try to calculate dynamically.

            int fragmentsNeeded = (int) Math.ceil((double) p.getSize() / PAGE_SIZE);
            // Find which fragment number this is (0 to fragmentsNeeded-1)
            int currentFragmentIndex = -1;
            int count = 0;
            for (int i = 0; i <= index; i++) {
                if (memorySlots[i] == p) {
                    if (i == index)
                        currentFragmentIndex = count;
                    count++;
                }
            }

            if (currentFragmentIndex < fragmentsNeeded - 1) {
                // Full fragment
                page.setUsedSpace(PAGE_SIZE);
                page.setFreeSpace(0);
            } else {
                // Last fragment
                int remainder = p.getSize() % PAGE_SIZE;
                if (remainder == 0)
                    remainder = PAGE_SIZE;
                page.setUsedSpace(remainder);
                page.setFreeSpace(PAGE_SIZE - remainder);
            }

            page.setFilePath(new java.io.File("simulation_files/P" + p.getPid() + ".txt").getAbsolutePath());

        } else {
            page.setProcessPid("-");
            page.setUsedSpace(0);
            page.setFreeSpace(PAGE_SIZE);
            page.setFilePath("");
        }
    }

    // <editor-fold desc="Event Handlers for Control Buttons">
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
        memoryStack.clear();

        finishedOrderList.clear();
        finishedProcessesVBox.getChildren().clear();
        runSimulationStep(false);
        timer = 0;
        updateUI();
    }

    @FXML
    private void onExportButtonClick() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter("FAT.txt"))) {
            writer.println("Reporte de Tabla de Paginación (FAT) - LIFO");
            writer.println("===========================================");
            writer.printf("%-10s %-15s %-10s %-15s %-15s %-30s%n", "NO.PAG", "FRAGMENTO", "PROCESO", "OCUPADO(KB)",
                    "LIBRE (KB)", "RUTA_ARCHIVO");
            writer.println(
                    "---------------------------------------------------------------------------------------------------------");

            for (MemoryPage page : memoryPages) {
                writer.printf("%-10d %-15d %-10s %-15d %-15d %-30s%n",
                        page.getPageNumber(),
                        page.getFrame(),
                        page.getProcessPid(),
                        page.getUsedSpace(),
                        page.getFreeSpace(),
                        page.getFilePath());
            }

            System.out.println("Archivo exportado exitosamente: FAT.txt");
            // Optional: Show an alert to the user
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText(null);
            alert.setContentText("La tabla de paginación se ha exportado a 'FAT.txt'.");
            alert.showAndWait();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error de Exportación");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo exportar el archivo: " + e.getMessage());
            alert.showAndWait();
        }
    }
    // </editor-fold>

    /**
     * Modelo para una fila de la tabla de paginación.
     */
    public static class MemoryPage {
        private final SimpleIntegerProperty pageNumber;
        private final SimpleIntegerProperty frame;
        private final SimpleStringProperty processPid;
        private final SimpleIntegerProperty freeSpace;
        private final SimpleIntegerProperty usedSpace;
        private final SimpleStringProperty filePath;

        public MemoryPage(int pageNumber, int frame) {
            this.pageNumber = new SimpleIntegerProperty(pageNumber);
            this.frame = new SimpleIntegerProperty(frame);
            this.processPid = new SimpleStringProperty("-");
            this.freeSpace = new SimpleIntegerProperty(256);
            this.usedSpace = new SimpleIntegerProperty(0);
            this.filePath = new SimpleStringProperty("");
        }

        public int getPageNumber() {
            return pageNumber.get();
        }

        public SimpleIntegerProperty pageNumberProperty() {
            return pageNumber;
        }

        public int getFrame() {
            return frame.get();
        }

        public SimpleIntegerProperty frameProperty() {
            return frame;
        }

        public String getProcessPid() {
            return processPid.get();
        }

        public SimpleStringProperty processPidProperty() {
            return processPid;
        }

        public void setProcessPid(String pid) {
            this.processPid.set(pid);
        }

        public int getFreeSpace() {
            return freeSpace.get();
        }

        public SimpleIntegerProperty freeSpaceProperty() {
            return freeSpace;
        }

        public void setFreeSpace(int space) {
            this.freeSpace.set(space);
        }

        public int getUsedSpace() {
            return usedSpace.get();
        }

        public SimpleIntegerProperty usedSpaceProperty() {
            return usedSpace;
        }

        public void setUsedSpace(int space) {
            this.usedSpace.set(space);
        }

        public String getFilePath() {
            return filePath.get();
        }

        public void setFilePath(String path) {
            this.filePath.set(path);
        }
    }
}
