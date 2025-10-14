package tecnm.celaya.edu.mx.despachadorapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador para la vista del menú principal (`main-view.fxml`).
 * <p>
 * Este controlador gestiona las acciones de los botones del menú principal.
 * Cada botón está asociado a un algoritmo de planificación específico y su función
 * es abrir una nueva ventana con la simulación correspondiente.
 */
public class HelloController {

    /**
     * Se ejecuta al hacer clic en el botón "Iniciar Simulación FIFO".
     * Llama al método genérico para abrir la ventana de simulación FIFO.
     */
    @FXML
    protected void onFIFOButtonClick() {
        openSimulationWindow("fifo-view.fxml", "Simulación FIFO");
    }

    /**
     * Se ejecuta al hacer clic en el botón "Iniciar Simulación SJF".
     */
    @FXML
    protected void onSJFButtonClick() {
        openSimulationWindow("sjf-view.fxml", "Simulación SJF");
    }

    /**
     * Se ejecuta al hacer clic en el botón "Iniciar Simulación Round Robin + LIFO".
     */
    @FXML
    protected void onRoundRobinLIFOButtonClick() {
        openSimulationWindow("roundrobin-lifo-view.fxml", "Simulación Round Robin + LIFO");
    }

    /**
     * Se ejecuta al hacer clic en el botón "Iniciar Simulación LIFO".
     */
    @FXML
    protected void onLIFOButtonClick() {
        openSimulationWindow("lifo-view.fxml", "Simulación LIFO");
    }

    /**
     * Se ejecuta al hacer clic en el botón "Iniciar Simulación LJF".
     */
    @FXML
    protected void onLJFButtonClick() {
        openSimulationWindow("ljf-view.fxml", "Simulación LJF");
    }

    /**
     * Método reutilizable para abrir una nueva ventana de simulación.
     *
     * @param fxmlFile El nombre del archivo FXML que define la vista de la simulación.
     * @param title    El título que se mostrará en la barra de la nueva ventana.
     */
    private void openSimulationWindow(String fxmlFile, String title) {
        try {
            // 1. Crear un cargador para el archivo FXML especificado.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));

            // 2. Cargar el FXML para crear la escena y aplicar la hoja de estilos.
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());

            // 3. Crear una nueva ventana (Stage) para la simulación.
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);

            // 4. Mostrar la nueva ventana.
            stage.show();
        } catch (IOException e) {
            // Imprimir un error si el archivo FXML no se puede cargar.
            System.err.println("Error al cargar el archivo FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
