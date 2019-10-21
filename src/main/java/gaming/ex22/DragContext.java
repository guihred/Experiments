package gaming.ex22;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Group;

public class DragContext {
    protected final ObservableList<FreeCellCard> cards = FXCollections.observableArrayList();
    protected double x;
    protected double y;
    protected FreeCellStack stack;

    public DragContext(Group view) {
        cards.addListener((Change<? extends FreeCellCard> c) -> {
            while (c.next()) {
                view.getChildren().addAll(c.getAddedSubList());
                view.getChildren().removeAll(c.getRemoved());
            }
        });
    }

    public void reset() {
        cards.clear();
        stack = null;
    }
}