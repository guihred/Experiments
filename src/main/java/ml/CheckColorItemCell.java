package ml;

import java.util.Map.Entry;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.StringConverter;

final class CheckColorItemCell extends CheckBoxListCell<Entry<String, Color>> {
    CheckColorItemCell(Callback<Entry<String, Color>, ObservableValue<Boolean>> getSelectedProperty,
            StringConverter<Entry<String, Color>> converter) {
        super(getSelectedProperty, converter);
    }

    @Override
    public void updateItem(Entry<String, Color> item, boolean empty) {
        super.updateItem(item, empty);
        Node graphic = getGraphic();
        if (!empty && graphic != null) {
            Rectangle rectangle = new Rectangle(20, 20);
            rectangle.setFill(item.getValue());
            setGraphic(new HBox(graphic, rectangle));
        }

    }
}