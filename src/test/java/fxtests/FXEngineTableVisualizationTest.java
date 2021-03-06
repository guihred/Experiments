package fxtests;

import fxpro.ch05.TableVisualizationExample;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import org.junit.Test;

public class FXEngineTableVisualizationTest extends AbstractTestExecution {

    @Test
	public void testaToolsVerify() {
        show(TableVisualizationExample.class);
        Set<Node> queryAll2 = lookup(".tab").queryAll();
        for (Node node : queryAll2) {
            tryClickOn(node);
            Set<Node> queryAs = lookup(".tab-content-area").queryAll().stream().filter(e -> e.isVisible())
                .collect(Collectors.toSet());
            from(queryAs).lookup(Control.class::isInstance).queryAll().stream()
                .filter(Node::isVisible).limit(20)
                .forEach(this::tryClickOn);

        }
        for (Node node : lookup(ToggleButton.class)) {
            tryClickOn(node);
        }
        for (MenuButton menuButton : lookup(MenuButton.class)) {
            ObservableList<MenuItem> items = menuButton.getItems();
            runReversed(items, menu -> interact(menu::fire));
        }
	}

}
