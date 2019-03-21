package gaming.ex21;

import java.util.EnumMap;
import java.util.function.ToLongFunction;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import simplebuilder.SimpleTextBuilder;

public class UserChart extends VBox {
    private final ImageView userImage = CatanResource.newImage("user.png", Color.BLUE, 100);
    private final Text userPoints = new SimpleTextBuilder().text("0").wrappingWidth(userImage.fitWidthProperty())
	    .textAlignment(TextAlignment.CENTER).build();
    private final Dice dice1 = new Dice();
    private final Dice dice2 = new Dice();
    private final EnumMap<PlayerColor, LongProperty> playersPoints = new EnumMap<>(PlayerColor.class);
    private final ObjectProperty<PlayerColor> color = new SimpleObjectProperty<>(PlayerColor.BLUE);

    public UserChart() {
	VBox vBox = new VBox();
	for (PlayerColor playerColor : PlayerColor.values()) {
	    SimpleLongProperty value = new SimpleLongProperty(0);
	    playersPoints.put(playerColor, value);
	    ImageView newImage = CatanResource.newImage("user.png", playerColor.getColor(), 30);
	    Text build = new SimpleTextBuilder().text(value.asString()).textOrigin(VPos.CENTER).build();
	    HBox e = new HBox(newImage, build);
	    e.visibleProperty().bind(color.isEqualTo(playerColor).not());
	    e.managedProperty().bind(e.visibleProperty());
	    vBox.getChildren().add(e);
	}

	getChildren().addAll(new HBox(vBox, new VBox(userImage, userPoints)), new HBox(dice1, dice2));
    }

    public void setColor(PlayerColor newV) {
	userImage.setImage(CatanResource.newImage("user.png", newV.getColor()));
	color.setValue(newV);
    }

    public void setPoints(long countPoints, ToLongFunction<PlayerColor> countPoint) {
	userPoints.setText(countPoints + " Points");
	for (PlayerColor playerColor : PlayerColor.values()) {
	    long points = countPoint.applyAsLong(playerColor);
	    playersPoints.get(playerColor).set(points);
	}
    }

    public int throwDice() {
	return dice1.throwDice() + dice2.throwDice();
    }

}
