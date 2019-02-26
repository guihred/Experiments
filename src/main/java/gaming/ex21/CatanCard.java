package gaming.ex21;


import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
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
		development= type;
		setStandard(type.getImage());
	}

	public CatanCard(final ResourceType type) {
		setResource(type);
		setStandard(type.getResource());
	}

	public boolean isSelected() {
		return selected.get();
	}

	public void setSelected(final Boolean value) {
		selected.set(value);
	}

	@Override
	public String toString() {
		return Objects.toString(getResource(), Objects.toString(development, ""));
	}

	private void setStandard(final String type) {
		// setPadding(new Insets(10));
		setManaged(false);
		String externalForm = ResourceFXUtils.toExternalForm("catan/" + type);
		setFill(new ImagePattern(new Image(externalForm)));
		setWidth(PREF_WIDTH);
		setHeight(PREF_HEIGHT);
	}

	public ResourceType getResource() {
		return resource;
	}

	public void setResource(ResourceType resource) {
		this.resource = resource;
	}

	public DevelopmentType getDevelopment() {
		return development;
	}

	public void setDevelopment(DevelopmentType development) {
		this.development = development;
	}

}

