package paintexp.tool;

import static utils.CommonsFX.bindBidirectional;

import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleConverter;
import simplebuilder.SimpleSliderBuilder;
import utils.ClassReflectionUtils;
import utils.StringSigaUtils;

public final class PaintToolHelper {
    private PaintToolHelper() {
    }

    public static void addOptionsAccordingly(Object selectedItem, ObservableList<Node> children,
            Map<String, Double> maxMap, ObservableList<?> effects) {
        children.clear();
        if (selectedItem == null) {
            return;
        }
        Map<Class<?>, List<?>> classMap = new HashMap<>();
        List<Class<?>> allClasses = ClassReflectionUtils.allClasses(selectedItem.getClass());
        for (Class<?> class1 : allClasses) {
            classMap.put(class1, effects);
        }
        ClassReflectionUtils.properties(selectedItem, selectedItem.getClass())
                .forEach((k, v) -> addOptions(selectedItem, children, maxMap, classMap, k, v));
    }

    public static void addOptionsAccordingly(Shape selectedItem, ObservableList<Node> children,
            Map<String, Double> maxMap, List<String> exclude) {
        children.clear();
        if (selectedItem == null) {
            return;
        }
        Map<Class<?>, List<?>> classMap = new HashMap<>();
        ClassReflectionUtils.simpleProperties(selectedItem, selectedItem.getClass()).entrySet().stream()
                .filter(t -> !exclude.contains(t.getKey()))
                .forEach(e -> addOptions(selectedItem, children, maxMap, classMap, e.getKey(), e.getValue()));

        addStrokeArray(selectedItem, children, "strokeDashArray", selectedItem.getStrokeDashArray());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addOptions(Object selectedItem, ObservableList<Node> options, Map<String, Double> maxMap,
            Map<Class<?>, List<?>> effects, String fieldName, Property property) {
        Object value = property.getValue();
        if (value instanceof EventHandler) {
            return;
        }

        String changeCase = StringSigaUtils.splitMergeCamelCase(StringSigaUtils.changeCase(fieldName));
        Text text2 = new Text(changeCase);
        text2.textProperty().bind(Bindings.createStringBinding(() -> propValue(property, changeCase), property));

        VBox vBox = new VBox(text2);
        options.add(vBox);
        ObservableList<Node> effectsOptions = vBox.getChildren();
        Node simpleObjectOption = simpleObjectOption(maxMap, fieldName, property, value);
        if (simpleObjectOption != null) {
            effectsOptions.add(simpleObjectOption);
            return;
        }

        if (value != null && ClassReflectionUtils.hasSetterMethods(value.getClass(), "color")) {
            Map<String, Property> properties = ClassReflectionUtils.properties(value, value.getClass());
            Property colorProperty = properties.get("color");
            Color value3 = (Color) colorProperty.getValue();
            ColorPicker colorPicker = new ColorPicker(value3);
            bindBidirectional(colorPicker.valueProperty(), colorProperty);
            effectsOptions.add(colorPicker);
            return;
        }

        Class<? extends Object> class1 = selectedItem.getClass();
        Class<?> setterType = ClassReflectionUtils.getSetterType(class1, fieldName);
        if (effects.containsKey(setterType) || ClassReflectionUtils.hasClass(effects.keySet(), class1)) {
            Optional<Class<?>> findFirst = effects.keySet().stream()
                    .filter(c -> c.isAssignableFrom(class1) || class1.isAssignableFrom(c)).findFirst();
            ComboBox comboBox =
                    new ComboBox<>(FXCollections.observableArrayList(effects.get(findFirst.orElse(setterType))));
            comboBox.setConverter(new SimpleConverter("class.simpleName"));
            comboBox.getSelectionModel().selectedItemProperty().addListener((ob, old, val) -> property.setValue(val));
            property.addListener((ob, old, val) -> comboBox.getSelectionModel().select(val));
            effectsOptions.add(comboBox);
        }
    }



    private static void addStrokeArray(Shape selectedItem, ObservableList<Node> children, String key,
            ObservableList<Double> property) {
        String changeCase = StringSigaUtils.splitMergeCamelCase(StringSigaUtils.changeCase(key));
        Text text2 = new Text(changeCase);
        VBox vBox = new VBox(text2);
        children.add(vBox);
        ObservableList<ObservableList<Double>> observableArrayList = FXCollections.observableArrayList();
        observableArrayList.add(FXCollections.observableArrayList(1.));
        observableArrayList.add(FXCollections.observableArrayList(1., 1.));
        observableArrayList.add(FXCollections.observableArrayList(1., 2.));
        observableArrayList.add(FXCollections.observableArrayList(1., 2., 3.));
        ComboBox<ObservableList<Double>> comboBox = new SimpleComboBoxBuilder<ObservableList<Double>>()
                .items(observableArrayList)
                .onChange((old, val) -> property
                        .setAll(val.stream().map(e -> e * selectedItem.getStrokeWidth()).collect(Collectors.toList())))
                .select(property).cellFactory((arr, cell) -> {
                    if (arr == null) {
                        return;
                    }
                    Line value = new Line(0, 0, vBox.getWidth() - 10, 0);
                    value.getStrokeDashArray().setAll(arr);
                    cell.setGraphic(value);
                }).build();
        selectedItem.strokeWidthProperty().addListener((ob, old, val) -> property
                .setAll(comboBox.getValue().stream().map(e -> e * val.doubleValue()).collect(Collectors.toList())));
        vBox.getChildren().add(comboBox);
    }

    private static double getMax(double value3) {
        return value3 <= 1 ? 1 : Math.max(50, value3);
    }

    private static String propValue(Property<?> value2, String changeCase) {
        Object value = value2.getValue();
        if (value instanceof Double) {
            return String.format(Locale.ENGLISH, "%s %.2f", changeCase, value);
        }

        if (value != null && !value.toString().matches("[\\.\\w\\$]+@[0-9a-fA-F]+")) {
            return changeCase + " " + value2.getValue();
        }
        return changeCase;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Node simpleObjectOption(Map<String, Double> maxMap, String fieldName, Property property,
            Object value) {
        if (value instanceof Number) {
            double value3 = ((Number) value).doubleValue();
            Double max = maxMap.computeIfAbsent(fieldName, v -> getMax(value3));
            SimpleSliderBuilder builder = new SimpleSliderBuilder(0, max, value3);
            bindBidirectional(builder.build().valueProperty(), property);
            return builder.build();
        }
        if (value instanceof Boolean) {
            CheckBox e = new CheckBox();
            e.setSelected((boolean) value);
            bindBidirectional(e.selectedProperty(), property);
            return e;
        }
        if (value instanceof String) {
            TextField e = new TextField();
            bindBidirectional(e.textProperty(), property);
            return e;
        }
        if (value instanceof Color) {
            ColorPicker colorPicker = new ColorPicker((Color) value);
            bindBidirectional(colorPicker.valueProperty(), property);
            return colorPicker;
        }
        if (value instanceof Enum<?>) {
            Enum<?> value3 = (Enum<?>) value;
            ComboBox<Enum<?>> comboBox =
                    new ComboBox<>(FXCollections.observableArrayList(value3.getClass().getEnumConstants()));
            comboBox.getSelectionModel().selectedItemProperty().addListener((ob, old, val) -> property.setValue(val));
            comboBox.setValue(value3);
            return comboBox;
        }
        return null;
    }

}
