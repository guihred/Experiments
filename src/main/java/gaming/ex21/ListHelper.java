package gaming.ex21;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import simplebuilder.SimpleButtonBuilder;

public final class ListHelper {
    private ListHelper() {
    }

    public static VBox newDeal(ObservableList<Deal> deal, Predicate<? super Deal> disableIf, Consumer<Deal> onAction,
        Observable... a) {
        VBox vBox = new VBox();
        deal.addListener((ListChangeListener<Deal>) c -> {
            while (c.next()) {
                List<? extends Deal> addedSubList = c.getAddedSubList();
                for (Deal deal1 : addedSubList) {
                    final Node graphic = deal1;
                    Button dealButton = SimpleButtonBuilder.newButton(graphic, "", e -> onAction.accept(deal1));
                    dealButton.getStyleClass().add("accept-deal");
                    dealButton.disableProperty().bind(Bindings.createBooleanBinding(() -> disableIf.test(deal1), a));
                    vBox.getChildren().add(dealButton);
                }
                for (Deal deal2 : c.getRemoved()) {
                    vBox.getChildren().removeIf(e -> e instanceof Button && deal2.equals(((Button) e).getGraphic()));
                }
            }

        });

        return vBox;
    }

    public static <T extends Node> ListChangeListener<T> onChangeElement(Pane center1) {
        return c -> {
            while (c.next()) {
                double layoutX = 0;
                for (Node node : c.getList()) {
                    node.setLayoutX(layoutX);
                    node.setLayoutY(0);
                    if (node.getParent() == null) {
                        center1.getChildren().add(node);
                    }
                    layoutX += node.getBoundsInLocal().getWidth() + 20;
                }
            }
        };
    }

}
