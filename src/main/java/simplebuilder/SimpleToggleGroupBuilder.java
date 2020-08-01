package simplebuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;

public class SimpleToggleGroupBuilder implements SimpleBuilder<ToggleGroup> {
    private ToggleGroup toggleGroup = new ToggleGroup();

    public SimpleToggleGroupBuilder addRadioToggle(final String text) {
        new RadioButton(text).setToggleGroup(toggleGroup);
        return this;
    }


	public SimpleToggleGroupBuilder addToggle(final Node node, final Object userData) {
        ToggleButton toggleButton = new ToggleButton(null, node);
        String tooltip = Objects.toString(userData, "");
        toggleButton.setTooltip(new Tooltip(tooltip));
        toggleButton.setUserData(userData);
        toggleButton.getStyleClass().add(tooltip.toLowerCase().replaceAll(" ", "-"));
        toggleButton.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(final Node node, final String id) {
        ToggleButton e = new ToggleButton(null, node);
        e.setId(id);
        e.setToggleGroup(toggleGroup);
        return this;
    }




    public SimpleToggleGroupBuilder addToggle(final String node, final Object userData) {
	    ToggleButton toggleButton = new ToggleButton(node);
	    toggleButton.setTooltip(new Tooltip(Objects.toString(userData, "")));
	    Toggle e = toggleButton;
	    e.setUserData(userData);
	    e.setToggleGroup(toggleGroup);
	    return this;
	}


    public SimpleToggleGroupBuilder addToggleTooltip(final Node node, final String text) {
        ToggleButton toggleButton = new ToggleButton(null, node);
        toggleButton.setTooltip(new Tooltip(text));
        Toggle e = toggleButton;
        e.setUserData(node);
        e.setToggleGroup(toggleGroup);
        return this;
    }

    @Override
    public ToggleGroup build() {
        return toggleGroup;
    }

    public Node[] getToggles() {
    	return toggleGroup.getToggles().stream().map(Node.class::cast).toArray(Node[]::new);
    }

	public <T>List<T> getTogglesAs(final Class<T> cl) {
        return toggleGroup.getToggles().stream().map(cl::cast).collect(Collectors.toList());
    }

    public SimpleToggleGroupBuilder onChange(final ChangeListener<? super Toggle> listener) {
        toggleGroup.selectedToggleProperty().addListener(listener);
        return this;
    }

    public SimpleToggleGroupBuilder onChange(final InvalidationListener listener) {
        toggleGroup.selectedToggleProperty().addListener(listener);
        return this;
    }

    public SimpleToggleGroupBuilder select(final int index) {
        toggleGroup.selectToggle(toggleGroup.getToggles().get(index));
        return this;
    }

    public SimpleToggleGroupBuilder select(final Object index) {
        toggleGroup.selectToggle(
                toggleGroup.getToggles().stream().filter(t -> Objects.equals(t.getUserData(), index)).findFirst()
                        .orElse(null));
        return this;
    }
    public Toggle selectedItem() {
        return toggleGroup.getSelectedToggle();
    }

}
