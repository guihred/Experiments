package fxtests;

import graphs.app.GraphModelLauncher;
import graphs.entities.Cell;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import utils.ConsoleUtils;
import utils.ConsumerEx;
import utils.ImageFXUtils;
import utils.RunnableEx;


public class FXEngineGraphTest extends AbstractTestExecution {

	@Override
	public void start(final Stage stage) throws Exception {
		super.start(stage);
		show(GraphModelLauncher.class);
	}
	@Test
	public void verify() throws Exception {
        ImageFXUtils.setShowImage(false);
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
        closeCurrentWindow();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void verifyAllTopologies() throws Exception {
		Set<ComboBox> queryButtons = lookup(".combo-box").queryAllAs(ComboBox.class).stream()
				.filter(ComboBox::isVisible)
				.collect(Collectors.toSet());
		Set<Node> queryAll = lookup("Go").queryAll();
		for (ComboBox e : queryButtons) {
			ObservableList<?> items = e.getItems();
			if (items.size() < 10) {
				for (int i = 0; i < items.size() ; i++) {
					int j = i;
					interact(() -> e.getSelectionModel().select(j));
					for (Node node : queryAll) {
						clickOn(node);
						ConsoleUtils.waitAllProcesses();
					}
				}
			}
		}
	}

	@Test
	public void verifyZoomable() throws Exception {
		Set<Node> queryButtons = lookup(Cell.class::isInstance).queryAll();
		queryButtons.forEach(e -> {
            RunnableEx.ignore(() -> clickOn(e));
            drag(e, MouseButton.PRIMARY);
			moveBy(100, 100);
			drop();
		});
		scroll(2, VerticalDirection.UP);
		scroll(2, VerticalDirection.DOWN);
	}

}
