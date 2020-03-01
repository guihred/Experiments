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
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import simplebuilder.SimpleConverter;
import simplebuilder.SimpleSliderBuilder;
import utils.ClassReflectionUtils;
import utils.FunctionEx;
import utils.StringSigaUtils;
import utils.SupplierEx;

public final class PaintToolHelper {
    public static final int N_POINTS_MULTIPLIER = 16;

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

    public static boolean isEqualImage(WritableImage image, WritableImage image2) {
        if (image.getWidth() != image2.getWidth() || image.getHeight() != image2.getHeight()) {
            return false;
        }
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getPixelReader().getArgb(i, j) != image2.getPixelReader().getArgb(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void setClipboardContent(Object imageSelected2) {
        Map<DataFormat, Object> content = FXCollections.observableHashMap();
        content.put(DataFormat.IMAGE, imageSelected2);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addOptions(Object selectedItem, ObservableList<Node> effectsOptions, Map<Object, Double> maxMap,
        ObservableList<?> effects, String fieldName, Property value2) {
        String changeCase = StringSigaUtils.changeCase(fieldName);
        Text text2 = new Text(changeCase);
        text2.textProperty().bind(Bindings.createStringBinding(() -> propValue(value2, changeCase), value2));
        effectsOptions.add(text2);
        Object value = value2.getValue();
        if (value instanceof Number) {
            double value3 = ((Number) value).doubleValue();
            Double max = maxMap.computeIfAbsent(value2, v -> getMax(value3));
            SimpleSliderBuilder builder = new SimpleSliderBuilder(0, max, value3);
            builder.build().valueProperty().bindBidirectional(value2);
            effectsOptions.add(builder.build());
            return;
        }
        if (value instanceof Boolean) {
            CheckBox e = new CheckBox();
            e.selectedProperty().bindBidirectional(value2);
            effectsOptions.add(e);
            return;
        }
        if (value instanceof Color) {
            ColorPicker colorPicker = new ColorPicker((Color) value);
            ((Property<Color>) value2).bind(colorPicker.valueProperty());
            effectsOptions.add(colorPicker);
            return;
        }
        if (value instanceof Enum<?>) {
            Enum<?> value3 = (Enum<?>) value;
            ComboBox comboBox = new ComboBox<>(FXCollections.observableArrayList(value3.getClass().getEnumConstants()));
            value2.bind(comboBox.getSelectionModel().selectedItemProperty());
            comboBox.setValue(value3);
            effectsOptions.add(comboBox);
            return;
        }
        if (value != null && ClassReflectionUtils.hasSetterMethods(value.getClass(), "color")) {
            Map<String, Property> properties = ClassReflectionUtils.properties(value, value.getClass());
            Property property = properties.get("color");
            Color value3 = (Color) property.getValue();
            ColorPicker colorPicker = new ColorPicker(value3);
            property.bind(colorPicker.valueProperty());
            effectsOptions.add(colorPicker);
            return;
        }
        Class<?> setterType = ClassReflectionUtils.getSetterType(selectedItem.getClass(), fieldName);
        if (setterType.isAssignableFrom(selectedItem.getClass())) {
            ComboBox comboBox = new ComboBox<>(effects.filtered(v -> selectedItem != v));
            comboBox.setConverter(new SimpleConverter("class.simpleName"));
            value2.bind(comboBox.getSelectionModel().selectedItemProperty());
            effectsOptions.add(comboBox);
        }
    }

    private static double getMax(double value3) {
        return value3 == 1 ? 1 : Math.max(50., value3);
    }

    private static String propValue(Property<?> value2, String changeCase) {
        Object value = value2.getValue();
        if (value == null || value.toString().matches("[\\.\\w\\$]+@[0-9a-fA-F]+")) {
            return changeCase;
        }
        if (value instanceof Double) {
            return String.format(Locale.ENGLISH, "%s %.2f", changeCase, value);
        }
        return changeCase + " " + value2.getValue();
    }

}
