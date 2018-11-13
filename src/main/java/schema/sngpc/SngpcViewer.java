
package schema.sngpc;

import java.io.File;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.apache.xmlbeans.XmlObject;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import simplebuilder.SimpleTreeViewBuilder;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class SngpcViewer extends Application {

	private static final Logger LOG = HasLogging.log();

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("SNGPC Viewer");
		primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
		primaryStage.show();
	}

	private Parent createSplitTreeListDemoNode() {
		ObservableList<String> list = FXCollections.observableArrayList();

		SimpleTreeViewBuilder<String> build = new SimpleTreeViewBuilder<String>().root("Root").onSelect(newValue -> {
			if (newValue != null && newValue.isLeaf()) {
				list.clear();
				for (int i = 1; i <= 10000; i++) {
					list.add(newValue.getValue() + " " + i);
				}
			}
		});
		try {
			File file = ResourceFXUtils.toFile("FL94_REL758_20181031061530.xml");
			XmlObject parse = XmlObject.Factory.parse(file);
			Node domNode = parse.getDomNode();
			NodeList childNodes = domNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);

				LOG.error("node Name={}", item.getNodeName());
				LOG.error("node Value={}", item.getNodeValue());
				LOG.error("node type={}", item.getNodeType());
				NodeList childNodes2 = item.getChildNodes();
				for (int j = 0; j < childNodes2.getLength(); j++) {
					Node item2 = childNodes2.item(j);
					LOG.error("node Name={}", item2.getNodeName());
					LOG.error("node Value={}", item2.getNodeValue());
					LOG.error("node type={}", item2.getNodeType());
				}

			}

		} catch (Exception e) {
			LOG.error("", e);
			throw new RuntimeIOException("ERROR READING", e);
		}

		return new SplitPane(build.build(), new ListView<>(list));
	}

	public static void main(String[] args) {
		launch(args);
	}
}
