package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleSliderBuilder;

public final class CommonsFX {

    private CommonsFX() {
    }

    public static Node[] createField(String nome, StringProperty propriedade) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(propriedade);
        return new Node[] { new Label(nome), textField };
    }

    public static List<Color> generateRandomColors(final int size) {
        final int maxByte = 255;
        int max = 256;
        List<Color> availableColors = new ArrayList<>();
        int cubicRoot = Integer.max((int) Math.ceil(Math.pow(size, 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(maxByte - i / cubicRoot / cubicRoot % cubicRoot * max / cubicRoot) % max,
                Math.abs(maxByte - i / cubicRoot % cubicRoot * max / cubicRoot) % max,
                Math.abs(maxByte - i % cubicRoot * max / cubicRoot) % max);

            availableColors.add(rgb);
        }
        Collections.shuffle(availableColors);
        return availableColors;
    }
    public static Button newButton(final double layoutX, final double layoutY, final String nome,
        final EventHandler<ActionEvent> onAction) {
        return newButton(layoutX, layoutY, nome, onAction);
    }

    public static Button newButton(final Node graphic, final String id, final EventHandler<ActionEvent> onAction) {
        return newButton(graphic, id, onAction);
    }

    public static Button newButton(final String nome, final EventHandler<ActionEvent> onAction) {
        return newButton(nome, onAction);
    }

    public static CheckBox newCheck(final String name, final BooleanProperty showWeight) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(showWeight.get());
        showWeight.bind(checkBox.selectedProperty());
        return checkBox;
    }

    public static CheckBox newCheckBox(final String text, final boolean disabled) {
        CheckBox build = new CheckBox(text);
        build.setDisable(disabled);
        return build;
    }

    public static TextField newFastFilter(FilteredList<?> filteredData) {
        TextField filterField = new TextField();
        filterField.textProperty().addListener((o, old, value) -> filteredData.setPredicate(row -> {
            if (value == null) {
                return true;
            }
            return StringUtils.containsIgnoreCase(row.toString(), value);

        }));
        return filterField;
    }

    public static VBox newSlider(final String string, final double min, final double max, int block,
        final Property<Number> radius) {
        return new VBox(new Text(string),
            new SimpleSliderBuilder().min(min).max(max).blocks(block).bindBidirectional(radius).build());
    }

    public static VBox newSlider(final String string, final double min, final double max,
        final Property<Number> radius) {
        return new VBox(new Text(string),
            new SimpleSliderBuilder().min(min).bindBidirectional(radius).max(max).build());
    }

    public static TextField newTextField(final String text, final int prefColumnCount) {
        TextField textField = new TextField(text);
        textField.setPrefColumnCount(prefColumnCount);
        return textField;
    }

    public static ToggleButton newToggleButton(final String id, final Node graphic,
        final EventHandler<ActionEvent> onAction) {
        ToggleButton button = new ToggleButton();
        button.setId(id);
        button.setGraphic(graphic);
        button.setOnAction(onAction);
        return button;
    }

}
