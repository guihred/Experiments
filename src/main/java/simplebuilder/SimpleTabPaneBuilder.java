package simplebuilder;

import java.util.function.BiConsumer;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SimpleTabPaneBuilder extends SimpleRegionBuilder<TabPane, SimpleTabPaneBuilder> {

    private TabPane tabPane;

    public SimpleTabPaneBuilder() {
        super(new TabPane());
        tabPane = node;
	}

    public SimpleTabPaneBuilder addTab(String text, Node node1) {
        tabPane.getTabs().add(new Tab(text, node1));
        return this;
    }

    public SimpleTabPaneBuilder addTab(String text, Node node1, BiConsumer<Tab, Event> handler) {
        Tab e = new Tab(text, node1);
        e.setOnSelectionChanged(evt -> handler.accept(e, evt));
        tabPane.getTabs().add(e);
        return this;
    }

    public SimpleTabPaneBuilder allClosable(boolean closable) {
        tabPane.getTabs().forEach(e -> e.setClosable(closable));
        return this;
    }

}