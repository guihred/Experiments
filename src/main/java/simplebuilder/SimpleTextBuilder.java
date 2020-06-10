package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class SimpleTextBuilder extends SimpleShapeBuilder<Text, SimpleTextBuilder> {

    public SimpleTextBuilder() {
        super(new Text());
    }

    public SimpleTextBuilder font(final javafx.scene.text.Font x) {
        shape.setFont(x);
        return this;
    }

    public SimpleTextBuilder fontSmoothingType(final javafx.scene.text.FontSmoothingType x) {
        shape.setFontSmoothingType(x);
        return this;
    }

    public SimpleTextBuilder size(int size) {
        Font font = shape.getFont();
        shape.setFont(Font.font(font.getFamily(), size));
        return this;
    }

    public SimpleTextBuilder strikethrough(final boolean x) {
        shape.setStrikethrough(x);
        return this;
    }

    public SimpleTextBuilder text(final java.lang.String x) {
        shape.setText(x);
        return this;
    }

    public SimpleTextBuilder text(final ObservableValue<? extends String> x) {
        shape.textProperty().bind(x);
        return this;
    }

    public SimpleTextBuilder textAlignment(final javafx.scene.text.TextAlignment x) {
        shape.setTextAlignment(x);
        return this;
    }

    public SimpleTextBuilder textOrigin(final javafx.geometry.VPos x) {
        shape.setTextOrigin(x);

        return this;
    }

    public SimpleTextBuilder underline(final boolean x) {
        shape.setUnderline(x);

        return this;
    }

    public SimpleTextBuilder wrappingWidth(final double x) {
        shape.setWrappingWidth(x);
        return this;
    }

    public SimpleTextBuilder wrappingWidth(final ObservableValue<? extends Number> x) {
        shape.wrappingWidthProperty().bind(x);
        return this;
    }

    public SimpleTextBuilder x(final double x) {
        shape.setX(x);

        return this;
    }
    public SimpleTextBuilder y(final double x) {
        shape.setY(x);
        return this;
    }

    public static Text newBoldText(String item) {
        Text text = new Text(item);
        Font font = Font.getDefault();
        text.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
        return text;
    }

}