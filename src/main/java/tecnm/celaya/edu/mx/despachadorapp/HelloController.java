package tecnm.celaya.edu.mx.despachadorapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML
    protected void onFIFOButtonClick() {
        openSimulationWindow("fifo-view.fxml", "Simulación FIFO");
    }

    @FXML
    protected void onSJFButtonClick() {
        openSimulationWindow("sjf-view.fxml", "Simulación SJF");
    }

    @FXML
    protected void onRoundRobinLIFOButtonClick() {
        openSimulationWindow("roundrobin-lifo-view.fxml", "Simulación Round Robin + LIFO");
    }

    @FXML
    protected void onLIFOButtonClick() {
        openSimulationWindow("lifo-view.fxml", "Simulación LIFO");
    }

    @FXML
    protected void onLJFButtonClick() {
        openSimulationWindow("ljf-view.fxml", "Simulación LJF");
    }

    private void openSimulationWindow(String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
