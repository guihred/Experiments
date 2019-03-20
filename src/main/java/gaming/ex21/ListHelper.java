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
import utils.CommonsFX;

public class ListHelper {
    public static VBox newDeal(ObservableList<Deal> deal, Predicate<? super Deal> predicate, Consumer<Deal> onAction,
	    Observable... a) {
	VBox vBox = new VBox();
	deal.addListener((ListChangeListener<Deal>) c -> {
	    while (c.next()) {
		List<? extends Deal> addedSubList = c.getAddedSubList();
		for (Deal deal1 : addedSubList) {
		    Button dealButton = CommonsFX.newButton(deal1, "", e -> onAction.accept(deal1));
		    dealButton.disableProperty().bind(Bindings.createBooleanBinding(() -> predicate.test(deal1), a));
		    vBox.getChildren().add(dealButton);
		}
		for (Deal deal2 : c.getRemoved()) {
		    vBox.getChildren().removeIf(e -> e instanceof Button && deal2.equals(((Button) e).getGraphic()));
		}
	    }

	});

	deal.stream().filter(predicate);

	return vBox;
    }

    public static ListChangeListener<CatanResource> onChangeElement(Pane center1) {
	return c -> {
	    while (c.next()) {
		List<? extends Node> addedSubList = c.getList();
		double layoutX = 0;
		double layoutY = 0;
		for (Node node : addedSubList) {
		    node.setLayoutX(layoutX);
		    node.setLayoutY(layoutY);
		    if (node.getParent() == null) {
			center1.getChildren().add(node);
		    }
		    layoutX += node.getBoundsInLocal().getWidth() + 20;
		}
	    }
	};
    }

}
