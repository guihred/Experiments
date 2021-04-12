package simplebuilder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.map.ReferenceMap;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;

public final class ListHelper {
    private ListHelper() {
    }

    public static <T, D> ObservableList<D> mapping(ObservableList<T> center1, FunctionEx<T, D> map) {
        ObservableList<D> observableArrayList = center1.stream().map(FunctionEx.makeFunction(map))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        addMapping(center1, map, observableArrayList);
        return observableArrayList;

    }

    public static <T extends Node> VBox newDeal(VBox vBox, ObservableList<T> deal, Predicate<? super T> disableIf,
            Consumer<T> onAction, Observable... a) {
        deal.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                List<? extends T> addedSubList = c.getAddedSubList();
                for (T deal1 : addedSubList) {
                    final Node graphic = deal1;
                    Button dealButton = SimpleButtonBuilder.newButton(graphic, "", e -> onAction.accept(deal1));
                    dealButton.getStyleClass().add("accept-deal");
                    dealButton.disableProperty().bind(Bindings.createBooleanBinding(() -> disableIf.test(deal1), a));
                    vBox.getChildren().add(dealButton);
                }
                for (T deal2 : c.getRemoved()) {
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

    public static <T, D> void referenceMapping(ObservableList<T> center1, FunctionEx<T, D> map,
            ObservableList<D> finalList) {
        ReferenceMap<T, D> a = new ReferenceMap<>();
        center1.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    break;
                }
                if (c.wasAdded()) {
                    ConsumerEx.foreach(c.getAddedSubList(),
                            e1 -> finalList.add(a.computeIfAbsent(e1, FunctionEx.makeFunction(map))));
                }
                if (c.wasRemoved()) {
                    ConsumerEx.foreach(c.getRemoved(),
                            e2 -> finalList.remove(a.computeIfAbsent(e2, FunctionEx.makeFunction(map))));
                }
            }
        });
    }

    private static <T, D> void addMapping(ObservableList<T> center1, FunctionEx<T, D> map,
            ObservableList<D> observableArrayList) {
        center1.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    break;
                }
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(e1 -> observableArrayList.add(FunctionEx.apply(map, e1)));
                }
                if (c.wasRemoved()) {
                    c.getRemoved()
                            .forEach(ConsumerEx.ignore(e2 -> observableArrayList.remove(FunctionEx.apply(map, e2))));
                }
            }
        });
    }

}
