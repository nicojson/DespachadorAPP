package tecnm.celaya.edu.mx.despachadorapp;

import javafx.animation.PauseTransition;
import javafx.scene.control.TableCell;
import javafx.util.Duration;

/**
 * A custom TableCell that adds a CSS class to flash when its item is updated.
 * @param <S> The type of the TableView S-parameter
 * @param <T> The type of the item contained in the cell
 */
public class UpdatingCell<S, T> extends TableCell<S, T> {

    private final PauseTransition flashTransition;

    public UpdatingCell() {
        // Create a transition to remove the flash style class after the animation is done.
        flashTransition = new PauseTransition(Duration.millis(800));
        flashTransition.setOnFinished(event -> getStyleClass().remove("cell-updated"));
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        T oldItem = getItem();
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.toString());

            // Only flash if the cell is not empty and the item has actually changed.
            if (oldItem != null && !item.equals(oldItem)) {
                flashTransition.stop(); // Stop any previous animation
                getStyleClass().add("cell-updated");
                flashTransition.playFromStart();
            }
        }
    }
}
