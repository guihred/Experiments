package utils;

import static utils.ResourceFXUtils.convertToURL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

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

    public static void loadFXML(String title, File file, Object controller, Stage primaryStage, double... size) {
        RunnableEx.remap(() -> {
            Parent content = loadParent(file, controller);
            Scene scene = size.length == 2 ? new Scene(content, size[0], size[1]) : new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        }, "ERROR in file " + file);
    }

    public static void loadFXML(String title, File file, Stage primaryStage, double... size) {
        RunnableEx.remap(() -> {
            Parent content = FXMLLoader.load(convertToURL(file));
            Scene scene = size.length == 2 ? new Scene(content, size[0], size[1]) : new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        }, "ERROR in file " + file);
    }

    public static void loadFXML(String title, String file, Object controller, Stage primaryStage, double... size) {
        loadFXML(title, ResourceFXUtils.toFile(file), controller, primaryStage, size);
    }

    public static void loadFXML(String title, String file, Stage primaryStage, double... size) {
        loadFXML(title, ResourceFXUtils.toFile(file), primaryStage, size);
    }

    public static Parent loadParent(File file, Object controller) {
        return SupplierEx.remap(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(convertToURL(file));
            fxmlLoader.setController(controller);
            return fxmlLoader.load();
        }, "ERROR IN " + file);
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
        filterField.textProperty().addListener((o, old, value) -> filteredData
            .setPredicate(row -> StringUtils.isBlank(value) || StringUtils.containsIgnoreCase(row.toString(), value)));
        return filterField;
    }

    public static <T> FilteredList<T> newFastFilter(TextField filterField, FilteredList<T> filteredData) {
        filterField.textProperty().addListener((o, old, value) -> filteredData
            .setPredicate(row -> StringUtils.isBlank(value) || StringUtils.containsIgnoreCase(row.toString(), value)));
        return filteredData;
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
