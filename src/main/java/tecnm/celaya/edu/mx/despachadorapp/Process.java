package tecnm.celaya.edu.mx.despachadorapp;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Process {
    private final SimpleIntegerProperty pid;
    private final SimpleIntegerProperty arrivalTime;
    private final SimpleIntegerProperty duration;
    private final SimpleStringProperty location;
    private final SimpleStringProperty state;
    private final SimpleIntegerProperty remainingDuration;

    public Process(int pid, int arrivalTime, int duration) {
        this.pid = new SimpleIntegerProperty(pid);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.duration = new SimpleIntegerProperty(duration);
        this.location = new SimpleStringProperty("");
        this.state = new SimpleStringProperty("");
        this.remainingDuration = new SimpleIntegerProperty(duration);
    }

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
