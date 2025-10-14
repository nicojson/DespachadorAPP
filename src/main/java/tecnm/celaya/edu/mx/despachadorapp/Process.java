package tecnm.celaya.edu.mx.despachadorapp;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Representa un único proceso dentro del simulador.
 * <p>
 * Esta clase es el modelo de datos fundamental. Contiene toda la información
 * relevante de un proceso, como su PID, tiempo de llegada y duración.
 * <p>
 * <b>IMPORTANTE:</b> Todos los atributos son {@link javafx.beans.property.Property} de JavaFX
 * (ej. {@link SimpleIntegerProperty}). Esto es crucial para que la interfaz de usuario (TableView)
 * se actualice automáticamente cuando los valores de un proceso cambian.
 */
public class Process {

    /** Identificador único del proceso. */
    private final SimpleIntegerProperty pid;

    /** El segundo exacto en el que el proceso llega al sistema y está listo para ser admitido en la memoria. */
    private final SimpleIntegerProperty arrivalTime;

    /** La cantidad total de segundos de CPU que el proceso necesita para completarse. */
    private final SimpleIntegerProperty duration;

    /** La ubicación actual del proceso en la simulación (ej. "Memoria", "CPU", "Salida"). */
    private final SimpleStringProperty location;

    /** El estado actual del proceso (W=Waiting, X=Executing, F=Finished). */
    private final SimpleStringProperty state;

    /** El tiempo de CPU que aún le falta al proceso para completarse. Se decrementa en cada tick de la CPU. */
    private final SimpleIntegerProperty remainingDuration;

    public Process(int pid, int arrivalTime, int duration) {
        this.pid = new SimpleIntegerProperty(pid);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.duration = new SimpleIntegerProperty(duration);

        // Inicialmente, un proceso no tiene ubicación ni estado hasta que llega.
        this.location = new SimpleStringProperty("");
        this.state = new SimpleStringProperty("");
        this.remainingDuration = new SimpleIntegerProperty(duration);
    }

    // --- Getters y Setters ---
    // Los métodos `*Property()` son necesarios para que la TableView pueda vincularse a estos atributos.

    public int getPid() {
        return pid.get();
    }

    public SimpleIntegerProperty pidProperty() {
        return pid;
    }

    public int getArrivalTime() {
        return arrivalTime.get();
    }

    public SimpleIntegerProperty arrivalTimeProperty() {
        return arrivalTime;
    }

    public int getDuration() {
        return duration.get();
    }

    public SimpleIntegerProperty durationProperty() {
        return duration;
    }

    public String getLocation() {
        return location.get();
    }

    public SimpleStringProperty locationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getState() {
        return state.get();
    }

    public SimpleStringProperty stateProperty() {
        return state;
    }

    public void setState(String state) {
        this.state.set(state);
    }

    public int getRemainingDuration() {
        return remainingDuration.get();
    }

    public SimpleIntegerProperty remainingDurationProperty() {
        return remainingDuration;
    }

    public void setRemainingDuration(int remainingDuration) {
        this.remainingDuration.set(remainingDuration);
    }
}
