package paintexp.tool;

import static utils.ResourceFXUtils.convertToURL;

import java.util.Locale;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simplebuilder.SimpleConverter;
import simplebuilder.SimpleSliderBuilder;
import utils.ClassReflectionUtils;
import utils.FunctionEx;
import utils.StringSigaUtils;
import utils.SupplierEx;

public final class PaintToolHelper {
    private PaintToolHelper() {
    }

    public static void addOptionsAccordingly(Object selectedItem, ObservableList<Node> children,
        Map<Object, Double> maxMap, ObservableList<?> effects) {
        children.clear();
        if (selectedItem == null) {
            return;
        }
        ClassReflectionUtils.properties(selectedItem, selectedItem.getClass())
            .forEach((k, v) -> addOptions(selectedItem, children, maxMap, effects, k, v));
    }

    public static Image getClipboardImage() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        return SupplierEx.orElse(systemClipboard.getImage(), () -> systemClipboard.getFiles().stream().findFirst()
            .map(FunctionEx.makeFunction(f -> new Image(convertToURL(f).toExternalForm()))).orElse(null));
    }

    public static void setClipboardContent(Object imageSelected2) {
        Map<DataFormat, Object> content = FXCollections.observableHashMap();
        content.put(DataFormat.IMAGE, imageSelected2);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addOptions(Object selectedItem, ObservableList<Node> options, Map<Object, Double> maxMap,
        ObservableList<?> effects, String fieldName, Property property) {
        String changeCase = StringSigaUtils.changeCase(fieldName);
        Text text2 = new Text(changeCase);
        text2.textProperty().bind(Bindings.createStringBinding(() -> propValue(property, changeCase), property));

        VBox vBox = new VBox(text2);
        options.add(vBox);
        ObservableList<Node> effectsOptions = vBox.getChildren();
        Object value = property.getValue();
        if (value instanceof Number) {
            double value3 = ((Number) value).doubleValue();
            Double max = maxMap.computeIfAbsent(property, v -> getMax(value3));
            SimpleSliderBuilder builder = new SimpleSliderBuilder(0, max, value3);
            builder.build().valueProperty().bindBidirectional(property);
            effectsOptions.add(builder.build());
            return;
        }
        if (value instanceof Boolean) {
            CheckBox e = new CheckBox();
            e.selectedProperty().bindBidirectional(property);
            effectsOptions.add(e);
            return;
        }
        if (value instanceof Color) {
            ColorPicker colorPicker = new ColorPicker((Color) value);
            ((Property<Color>) property).bind(colorPicker.valueProperty());
            effectsOptions.add(colorPicker);
            return;
        }
        if (value instanceof Enum<?>) {
            Enum<?> value3 = (Enum<?>) value;
            ComboBox comboBox = new ComboBox<>(FXCollections.observableArrayList(value3.getClass().getEnumConstants()));
            property.bind(comboBox.getSelectionModel().selectedItemProperty());
            comboBox.setValue(value3);
            effectsOptions.add(comboBox);
            return;
        }
        if (value != null && ClassReflectionUtils.hasSetterMethods(value.getClass(), "color")) {
            Map<String, Property> properties = ClassReflectionUtils.properties(value, value.getClass());
            Property colorProperty = properties.get("color");
            Color value3 = (Color) colorProperty.getValue();
            ColorPicker colorPicker = new ColorPicker(value3);
            colorProperty.bind(colorPicker.valueProperty());
            effectsOptions.add(colorPicker);
            return;
        }
        Class<?> setterType = ClassReflectionUtils.getSetterType(selectedItem.getClass(), fieldName);
        if (setterType.isAssignableFrom(selectedItem.getClass())) {
            ComboBox comboBox = new ComboBox<>(effects.filtered(v -> selectedItem != v));
            comboBox.setConverter(new SimpleConverter("class.simpleName"));
            property.bind(comboBox.getSelectionModel().selectedItemProperty());
            effectsOptions.add(comboBox);
        }
    }

    private static double getMax(double value3) {
        return value3 == 1 ? 1 : Math.max(50., value3);
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

}
