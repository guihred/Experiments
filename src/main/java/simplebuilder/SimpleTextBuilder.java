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
        node.setFont(x);
        return this;
    }

    public SimpleTextBuilder fontSmoothingType(final javafx.scene.text.FontSmoothingType x) {
        node.setFontSmoothingType(x);
        return this;
    }

    public SimpleTextBuilder size(int size) {
        Font font = node.getFont();
        node.setFont(Font.font(font.getFamily(), size));
        return this;
    }

    public SimpleTextBuilder strikethrough(final boolean x) {
        node.setStrikethrough(x);
        return this;
    }

    public SimpleTextBuilder text(final java.lang.String x) {
        node.setText(x);
        return this;
    }

    public SimpleTextBuilder text(final ObservableValue<? extends String> x) {
        node.textProperty().bind(x);
        return this;
    }

    public SimpleTextBuilder textAlignment(final javafx.scene.text.TextAlignment x) {
        node.setTextAlignment(x);
        return this;
    }

    public SimpleTextBuilder textOrigin(final javafx.geometry.VPos x) {
        node.setTextOrigin(x);

        return this;
    }

    public SimpleTextBuilder underline(final boolean x) {
        node.setUnderline(x);

        return this;
    }

    public SimpleTextBuilder wrappingWidth(final double x) {
        node.setWrappingWidth(x);
        return this;
    }

    public SimpleTextBuilder wrappingWidth(final ObservableValue<? extends Number> x) {
        node.wrappingWidthProperty().bind(x);
        return this;
    }

    public SimpleTextBuilder x(final double x) {
        node.setX(x);

        return this;
    }

    public SimpleTextBuilder y(final double x) {
        node.setY(x);
        return this;
    }

    public static Text newBoldText(String item) {
        Font font = Font.getDefault();
        return new SimpleTextBuilder().text(item).font(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()))
                .build();
    }

}