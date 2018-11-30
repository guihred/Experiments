package simplebuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;

public class SimpleToggleGroupBuilder implements SimpleBuilder<ToggleGroup> {
    private ToggleGroup toggleGroup = new ToggleGroup();

    public SimpleToggleGroupBuilder addRadioToggle(String text) {
        new RadioButton(text).setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(Node node) {
        Toggle e = new ToggleButton(null, node);
        e.setUserData(node);

        e.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(Node node, Object ob) {
        ToggleButton toggleButton = new ToggleButton(null, node);
        toggleButton.setTooltip(new Tooltip(Objects.toString(ob, "")));
        Toggle e = toggleButton;
        e.setUserData(ob);

        e.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(Node node, String id) {
        ToggleButton e = new ToggleButton(null, node);
        e.setId(id);
        e.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(String text) {
        ToggleButton node = new ToggleButton(text);
        node.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(String text, Node node) {
        ToggleButton node2 = new ToggleButton(text, node);
        node2.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggle(String text, Node node, String id) {
        ToggleButton e = new ToggleButton(text, node);
        e.setId(id);
        e.setToggleGroup(toggleGroup);
        return this;
    }

    public  SimpleToggleGroupBuilder addToggle(Toggle toggle) {
        toggle.setToggleGroup(toggleGroup);
        return this;
    }

    public SimpleToggleGroupBuilder addToggleTooltip(Node node, String text) {
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

    public SimpleToggleGroupBuilder select(Object index) {
        toggleGroup.selectToggle(
                toggleGroup.getToggles().stream().filter(t -> t.getUserData().equals(index)).findFirst().orElse(null));
        return this;
    }
    public Toggle selectedItem() {
        return toggleGroup.getSelectedToggle();
    }

}
