package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleConverter;
import simplebuilder.SimpleSliderBuilder;
import utils.ClassReflectionUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;

public final class PaintToolHelper {
    public static final int N_POINTS_MULTIPLIER = 16;

    private PaintToolHelper() {
    }

    @SuppressWarnings({ "rawtypes" })
    public static void addOptionsAccordingly(Object selectedItem, Pane effectsOptions, Map<Object, Double> maxMap,
        ObservableList<?> effects) {

        effectsOptions.getChildren().clear();
        if (selectedItem == null) {
            return;
        }
        Map<String, Property> getters = ClassReflectionUtils.properties(selectedItem, selectedItem.getClass());
        for (Map.Entry<String, Property> method : getters.entrySet()) {
            addOptions(selectedItem, effectsOptions, maxMap, effects, method);
        }
    }

    public static void drawCircle(WritableImage image, int centerX, int centerY, double radiusX, double radiusY,
        Color color) {
        double nPoints = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t));
            int y = (int) Math.round(radiusY * Math.sin(t));
            drawPoint(image, x + centerX, y + centerY, color);
        }
    }

    public static void drawCirclePart(WritableImage image, double centerX, double centerY, double radiusX,
        double radiusY, double startAngle, Color frontColor) {
        double nPoints2 = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        double angle = Math.PI / 2;
        for (double t = 0; t < angle; t += 2 * Math.PI / nPoints2) {
            int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
            int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
            drawPoint(image, x + (int) centerX, y + (int) centerY, frontColor);
        }
    }

    public static void drawPoint(WritableImage image, int x2, int y2, Color frontColor) {
        if (withinImage(x2, y2, image)) {
            image.getPixelWriter().setColor(x2, y2, frontColor);
        }
    }

    public static void drawPointIf(WritableImage image, int x2, int y2, int color, Color backColor) {
        if (withinImage(x2, y2, image)) {
            int argb = image.getPixelReader().getArgb(x2, y2);
            if (argb == color) {
                image.getPixelWriter().setColor(x2, y2, backColor);
            }
        }
    }

    public static void drawPointTransparency(int x2, int y2, Color frontColor, double opacity, WritableImage image,
        ObservableList<WritableImage> imageVersions) {
        if (withinImage(x2, y2, image)) {
            int index = Math.max(imageVersions.size() - 1, 0);
            Color color = imageVersions.get(index).getPixelReader().getColor(x2, y2);
            Color color2 = color.interpolate(frontColor, opacity);
            image.getPixelWriter().setColor(x2, y2, color2);
        }
    }

    public static void drawSquareLine(WritableImage image, Color backColor, int x, int y, int w, int color) {
        for (int i = 0; i < w; i++) {
            drawPointIf(image, x + i, y, color, backColor);
            drawPointIf(image, x, y + i, color, backColor);
            drawPointIf(image, x + w, y + i, color, backColor);
            drawPointIf(image, x + i, y + w, color, backColor);
        }
    }

    public static void drawSquareLine(WritableImage image, int startX, int startY, int w, Color color) {
        for (int x = 0; x <= w; x++) {
            drawPoint(image, startX + x, startY, color);
            drawPoint(image, startX, startY + x, color);
            drawPoint(image, startX + x, startY + w, color);
            drawPoint(image, startX + w, startY + x, color);
        }
    }

    public static void drawSquareLine(WritableImage image, ObservableList<WritableImage> imageVersions, int startX,
        int startY, int w, Color color, double opacity) {
        for (int x = 0; x < w; x++) {
            drawPointTransparency(startX + x, startY, color, opacity, image, imageVersions);
            drawPointTransparency(startX, startY + x, color, opacity, image, imageVersions);
            drawPointTransparency(startX + x, startY + w, color, opacity, image, imageVersions);
            drawPointTransparency(startX + w, startY + x, color, opacity, image, imageVersions);
        }
    }

    public static ImageView getIconByURL(String src) {
        return getIconByURL(src, 30);

    }

    public static ImageView getIconByURL(String src, double width) {
        ImageView icon1 = new ImageView(ResourceFXUtils.toExternalForm("paint/" + src));
        icon1.setPreserveRatio(true);
        icon1.setFitWidth(width);
        icon1.setFitHeight(width);
        icon1.maxWidth(width);
        icon1.maxHeight(width);
        return icon1;

    }

    public static boolean isEqualImage(WritableImage image, WritableImage image2) {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getPixelReader().getArgb(i, j) != image2.getPixelReader().getArgb(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void setClipboardContent(Image imageSelected2) {
        Map<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.IMAGE, imageSelected2);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addOptions(Object selectedItem, Pane effectsOptions, Map<Object, Double> maxMap,
        ObservableList<?> effects, Map.Entry<String, Property> method) {
        String fieldName = method.getKey();
        Property value2 = method.getValue();
        String changeCase = StringSigaUtils.changeCase(fieldName);
        Text text2 = new Text(changeCase);
        text2.textProperty().bind(Bindings.createStringBinding(() -> propValue(value2, changeCase), value2));
        effectsOptions.getChildren().add(text2);
        Object value = value2.getValue();
        if (value instanceof Number) {
            double value3 = ((Number) value).doubleValue();
            Double max = maxMap.computeIfAbsent(value2, v -> getMax(value3));
            Slider e = new SimpleSliderBuilder(0, max, value3).build();
            e.valueProperty().bindBidirectional(value2);
            effectsOptions.getChildren().add(e);
            return;
        }
        if (value instanceof Boolean) {
            CheckBox e = new CheckBox();
            e.selectedProperty().bindBidirectional(value2);
            effectsOptions.getChildren().add(e);
            return;
        }
        if (value instanceof Color) {
            ColorPicker colorPicker = new ColorPicker((Color) value);
            ((Property<Color>) value2).bind(colorPicker.valueProperty());
            effectsOptions.getChildren().add(colorPicker);
            return;
        }
        if (value instanceof Enum<?>) {
            Enum<?> value3 = (Enum<?>) value;
            ComboBox comboBox = new ComboBox<>(FXCollections.observableArrayList(value3.getClass().getEnumConstants()));
            value2.bind(comboBox.getSelectionModel().selectedItemProperty());
            comboBox.setValue(value3);
            effectsOptions.getChildren().add(comboBox);
            return;
        }
        if (value != null && ClassReflectionUtils.hasSetterMethods(value.getClass(), "color")) {
            Map<String, Property> properties = ClassReflectionUtils.properties(value, value.getClass());
            Property property = properties.get("color");
            Color value3 = (Color) property.getValue();
            ColorPicker colorPicker = new ColorPicker(value3);

            property.bind(colorPicker.valueProperty());
            effectsOptions.getChildren().add(colorPicker);
            return;
        }
        Class<?> setterType = ClassReflectionUtils.getSetterType(selectedItem.getClass(), fieldName);
        if (setterType.isAssignableFrom(selectedItem.getClass())) {
            ComboBox comboBox = new ComboBox<>(effects.filtered(v -> selectedItem != v));
            comboBox.setConverter(new SimpleConverter("class.simpleName"));
            value2.bind(comboBox.getSelectionModel().selectedItemProperty());
            effectsOptions.getChildren().add(comboBox);
        }
    }

    private static double getMax(double value3) {
        return value3 == 1 ? 1 : Math.max(50., value3);
    }

    private static String propValue(Property<?> value2, String changeCase) {
        Object value = value2.getValue();
        String string = Objects.toString(value, "");
        if (StringUtils.isBlank(string) || string.matches("[\\.\\w\\$]+@[0-9a-fA-F]+")) {
            return changeCase;
        }
        if (value instanceof Double) {
            return String.format(Locale.ENGLISH, "%s %.2f", changeCase, value);
        }
        return changeCase + " " + value2.getValue();
    }

}
