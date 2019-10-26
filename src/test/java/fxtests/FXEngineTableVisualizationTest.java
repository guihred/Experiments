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
import utils.ConsumerEx;
import utils.RunnableEx;
import utils.TreeElement;

public class FXEngineTableVisualizationTest extends AbstractTestExecution {

    @Test
	public void testaToolsVerify() throws Exception {
        show(TableVisualizationExample.class);
        Set<Node> queryAll2 = lookup(".tab").queryAll();
        for (Node node : queryAll2) {
            RunnableEx.ignore(() -> clickOn(node));
            Set<Node> queryAs = lookup(".tab-content-area").queryAll().stream().filter(e -> e.isVisible())
                .collect(Collectors.toSet());
            for (Node node2 : queryAs) {
                getLogger().info("{}", TreeElement.displayStyleClass(node2));
            }
            from(queryAs).lookup(Control.class::isInstance).queryAll().stream()
                .filter(e -> e.isVisible()).limit(20)
                .forEach(ConsumerEx.ignore((n) -> clickOn(n)));

        }
        for (Node node : lookup(ToggleButton.class)) {
            RunnableEx.ignore(() -> clickOn(node));
        }
        for (MenuButton menuButton : lookup(MenuButton.class)) {
            ObservableList<MenuItem> items = menuButton.getItems();
            for (int j = items.size() - 1; j >= 0; j--) {
                MenuItem menu = items.get(j);
                interact(menu::fire);
            }
        }
	}

}
