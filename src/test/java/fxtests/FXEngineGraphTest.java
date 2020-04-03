package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.GraphMain;
import graphs.app.JavaFileDependency;
import graphs.app.PackageTopology;
import graphs.app.ProjectTopology;
import graphs.entities.Cell;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.junit.Test;
import utils.ConsoleUtils;
import utils.ImageFXUtils;


@SuppressWarnings("static-method")
public class FXEngineGraphTest extends AbstractTestExecution {

    @Test
    public void testPackageTopology() {
        measureTime("JavaFileDependency.getJavaFileDependencies", () -> {
            List<JavaFileDependency> javaFiles = JavaFileDependency.getJavaFileDependencies(null);
            Map<String, List<JavaFileDependency>> filesByPackage = javaFiles.stream()
                .collect(Collectors.groupingBy(JavaFileDependency::getPackage));
            filesByPackage.forEach((pack, files) -> {
                Map<String, Map<String, Long>> packageDependencyMap = PackageTopology.createFileDependencyMap(files);
                PackageTopology.printDependencyMap(packageDependencyMap);
            });
        });
    }

    @Test
    public void testProjectTopology() {

        measureTime("JavaFileDependency.getJavaFileDependencies", () -> {
            Map<String, Map<String, Long>> packageDependencyMap = ProjectTopology.createProjectDependencyMap();
            PackageTopology.printDependencyMap(packageDependencyMap);
        });
    }

    @Test
	public void verifyAllTopologies() {
		show(GraphMain.class);
        // RandomTopology GridLayout CircleTopology
        List<ComboBox<?>> queryButtons = Stream.of("#selectLayout", "#topologySelect")
            .map(e -> lookup(e).queryComboBox())
            .collect(Collectors.toList());
        List<Node> queryAll = lookup("Go").queryAll().stream().collect(Collectors.toList());
        for (ComboBox<?> e : queryButtons) {
			ObservableList<?> items = e.getItems();
            if (items.size() <= 11) {
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
        ComboBox<?> layout = queryButtons.get(0);
        interact(() -> layout.getSelectionModel().select(1));
        ComboBox<?> topology = queryButtons.get(1);
        interact(() -> topology.getSelectionModel().select(4));
        for (int i = queryAll.size() - 1; i >= 0; --i) {
            Node node = queryAll.get(i);
            clickOn(node);
            ConsoleUtils.waitAllProcesses();
        }

	}

    @Test
	public void verifyGraphMain()  {
		show(GraphMain.class);
        ImageFXUtils.setShowImage(false);
        tryClickButtons();
        tryClickButtons();
    }

    @Test
	public void verifyZoomable() {
		show(GraphMain.class);
		lookup(Cell.class).stream().limit(10).forEach(e -> {
            tryClickOn(e);
            randomDrag(e, 100);
		});
		scroll(2, VerticalDirection.UP);
		scroll(2, VerticalDirection.DOWN);
	}
}
