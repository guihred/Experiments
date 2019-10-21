package gaming.ex19;

import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.VPos;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public final class NumberButton extends Region {

    private final int number;
    private BooleanProperty over = new SimpleBooleanProperty(false);

    public NumberButton(@NamedArg("number") int i) {
        number = i;
        styleProperty().bind(
            Bindings.when(over).then("-fx-background-color: white;").otherwise("-fx-background-color: lightgray;"));
        setEffect(new InnerShadow());
        Text text = new Text(i == 0 ? "X" : Integer.toString(i));
        text.wrappingWidthProperty().bind(widthProperty());
        text.setTextOrigin(VPos.CENTER);
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(30, 30);
        getStyleClass().add("numberButton");
    }

    public int getNumber() {
        return number;
    }

    public void setOver(boolean over) {
        this.over.set(over);
    }
}