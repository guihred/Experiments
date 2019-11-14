package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.GraphMain;
import graphs.app.JavaFileDependency;
import graphs.app.PackageTopology;
import graphs.entities.Cell;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.junit.Test;
import utils.ConsoleUtils;
import utils.ImageFXUtils;


public class FXEngineGraphTest extends AbstractTestExecution {

	@Test
    public void testPackageTopology() {
        measureTime("JavaFileDependency.getJavaFileDependencies", () -> {
            List<JavaFileDependency> javaFiles = JavaFileDependency.getJavaFileDependencies(null);
            Map<String, List<JavaFileDependency>> filesByPackage = javaFiles.stream()
                .collect(Collectors.groupingBy(JavaFileDependency::getPackage));
            filesByPackage.forEach((pack, files) -> {
                getLogger().trace(pack);
                Map<String, Map<String, Long>> packageDependencyMap = PackageTopology.createFileDependencyMap(files);
                PackageTopology.printDependencyMap(packageDependencyMap);
            });
        });
    }

	@Test
	public void verifyAllTopologies() {
		show(GraphMain.class);
        List<ComboBox<?>> queryButtons = Stream.of("#selectLayout", "#topologySelect")
            .map(e -> lookup(e).queryComboBox())
            .collect(Collectors.toList());
        Collections.shuffle(queryButtons);
		Set<Node> queryAll = lookup("Go").queryAll();
        for (ComboBox<?> e : queryButtons) {
			ObservableList<?> items = e.getItems();
            if (items.size() <= 10) {
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
