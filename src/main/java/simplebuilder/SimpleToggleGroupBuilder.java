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
        Toggle e = new ToggleButton(null, node);
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
