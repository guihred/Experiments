package simplebuilder;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class SimpleToggleGroupBuilder implements SimpleBuilder<ToggleGroup> {
    private ToggleGroup toggleGroup = new ToggleGroup();

    public SimpleToggleGroupBuilder addRadioToggle(String text) {
        return addToggle(new RadioButton(text));
    }

    public SimpleToggleGroupBuilder addToggle(Node node, String id) {
        ToggleButton e = new ToggleButton(null, node);
        e.setId(id);
        return addToggle(e);
    }

    public SimpleToggleGroupBuilder addToggle(String text) {
        return addToggle(new ToggleButton(text));
    }

    public SimpleToggleGroupBuilder addToggle(String text, Node node) {
        return addToggle(new ToggleButton(text, node));
    }

    public SimpleToggleGroupBuilder addToggle(String text, Node node, String id) {
        ToggleButton e = new ToggleButton(text, node);
        e.setId(id);
        return addToggle(e);
    }

    public SimpleToggleGroupBuilder addToggle(Toggle toggle) {
        toggle.setToggleGroup(toggleGroup);
        return this;
    }

    @Override
    public ToggleGroup build() {
        return toggleGroup;
    }

    public <T>List<T> getTogglesAs(Class<T> cl) {
        return toggleGroup.getToggles().stream().map(cl::cast).collect(Collectors.toList());
    }

    public SimpleToggleGroupBuilder onChange(ChangeListener<? super Toggle> listener) {
        toggleGroup.selectedToggleProperty().addListener(listener);
        return this;
    }

    public SimpleToggleGroupBuilder select(int index) {
        toggleGroup.selectToggle(toggleGroup.getToggles().get(index));
        return this;
    }

}
