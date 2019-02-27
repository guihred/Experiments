package gaming.ex21;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public class CatanCard extends Rectangle {
	public static final int PREF_HEIGHT = 75;

	public static final int PREF_WIDTH = 50;

	private ResourceType resource;
	private DevelopmentType development;
	private final BooleanProperty selected = new SimpleBooleanProperty(false);

	public CatanCard(final DevelopmentType type) {
		development = type;
		setStandard(type.getImage());
	}

	public CatanCard(final ResourceType type) {
		resource = type;
		setStandard(type.getResource());
	}

	public DevelopmentType getDevelopment() {
		return development;
	}

	public ResourceType getResource() {
		return resource;
	}

	public boolean isSelected() {
		return selected.get();
	}

	public void setDevelopment(final DevelopmentType development) {
		this.development = development;
	}

	public void setResource(final ResourceType resource) {
		this.resource = resource;
	}

	public void setSelected(final Boolean value) {
		selected.set(value);
	}

	@Override
	public String toString() {
		return Objects.toString(resource, Objects.toString(development, ""));
	}

	private void setStandard(final String type) {
		setManaged(false);
		String externalForm = ResourceFXUtils.toExternalForm("catan/" + type);
		setFill(new ImagePattern(new Image(externalForm)));
		setWidth(PREF_WIDTH);
		setHeight(PREF_HEIGHT);
		setOnMouseClicked(e -> selected.set(!selected.get()));

		InnerShadow innerShadow = new InnerShadow(20, Color.DODGERBLUE);
		effectProperty().bind(Bindings.when(selected).then(innerShadow).otherwise((InnerShadow) null));
	}

}
