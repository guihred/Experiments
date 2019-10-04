package gaming.ex21;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CatanCard extends Rectangle {
    public static final int PREF_HEIGHT = 75;

    public static final int PREF_WIDTH = 50;

    private ResourceType resource;
    private DevelopmentType development;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public CatanCard(DevelopmentType type, Consumer<CatanCard> object) {
        development = type;
        setStandard(type.getImage());
		mouseClicked(object);
    }

	public CatanCard(ResourceType type, Consumer<CatanCard> object) {
        resource = type;
        setStandard(type.getResource());
        mouseClicked(object);
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

	private final void mouseClicked(Consumer<CatanCard> object) {
		setOnMouseClicked(e -> object.accept(this));
	}

    private void setStandard(final String type) {
        setManaged(false);
        setFill(CatanResource.newPattern(type));
        setWidth(PREF_WIDTH);
        setHeight(PREF_HEIGHT);
        getStyleClass().add(
            Objects.toString(development, Objects.toString(resource)).toLowerCase().replaceAll("_", "-") + "-card");
        InnerShadow innerShadow = new InnerShadow(20, Color.DODGERBLUE);
        effectProperty().bind(Bindings.when(selected).then(innerShadow).otherwise((InnerShadow) null));
    }
}
