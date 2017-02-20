package gaming.ex13;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public class SolitaireCard extends Region {


	private final SolitaireNumber number;
	private final SolitaireSuit suit;
	private final BooleanProperty shown = new SimpleBooleanProperty(false);

	public SolitaireCard(SolitaireNumber number, SolitaireSuit suit) {
		this.number = number;
		this.suit = suit;
		setPadding(new Insets(10));
		setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(1))));
		setPrefSize(50, 75);

		Shape s = this.suit.getShape();
		s.setLayoutX(30);
		s.setLayoutY(15);
		s.setScaleX(0.75);
		s.setScaleY(0.75);
		s.visibleProperty().bind(shown);

		Text text = new Text(this.number.getRepresentation());
		text.setFill(this.suit.getColor());
		text.setLayoutX(10);
		text.setLayoutY(20);
		text.visibleProperty().bind(shown);

		getChildren().add(text);
		getChildren().add(s);
		setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 5;");
    }

	public void setShown(Boolean value) {
		shown.set(value);
	}

	@Override
	public String toString() {
		return getNumber().getRepresentation() + " " + suit;
	}

	public SolitaireNumber getNumber() {
		return number;
	}

	public SolitaireSuit getSuit() {
		return suit;
	}

	public boolean isShown() {
		return shown.get();
	}

}
