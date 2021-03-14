package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class SimpleTextBuilder extends SimpleShapeBuilder<Text, SimpleTextBuilder> {

    public SimpleTextBuilder() {
        super(new Text());
    }

    public SimpleTextBuilder size(int size) {
        Font font = node.getFont();
        node.setFont(Font.font(font.getFamily(), size));
        return this;
    }

    public SimpleTextBuilder text(final ObservableValue<? extends String> x) {
        node.textProperty().bind(x);
        return this;
    }

    public SimpleTextBuilder text(final String x) {
        node.setText(x);
        return this;
    }

    public SimpleTextBuilder textAlignment(final TextAlignment x) {
        node.setTextAlignment(x);
        return this;
    }

    public SimpleTextBuilder textOrigin(final VPos x) {
        node.setTextOrigin(x);

        return this;
    }

    public SimpleTextBuilder wrappingWidth(final double x) {
        node.setWrappingWidth(x);
        return this;
    }

    private SimpleTextBuilder font(final Font x) {
        node.setFont(x);
        return this;
    }

    public static Text newBoldText(String item) {
        Font font = Font.getDefault();
        return new SimpleTextBuilder().text(item).font(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()))
                .build();
    }

}