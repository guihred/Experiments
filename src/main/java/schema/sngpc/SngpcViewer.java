
package schema.sngpc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class SngpcViewer extends Application {

	private static final Logger LOG = HasLogging.log();

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("SNGPC Viewer");
		primaryStage.setScene(new Scene(createSplitTreeListDemoNode(primaryStage)));
		primaryStage.show();
	}

	private void addColumns(TableView<Map<String, String>> tableView, Collection<String> keySet) {

		keySet.forEach(key -> {
			final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
			column.setCellValueFactory(
					param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
			column.prefWidthProperty().bind(tableView.widthProperty().divide(keySet.size()).add(-5));
			tableView.getColumns().add(column);
		});
	}

	private void addValue(Node item, TreeItem<Map<String, String>> e) {
		try {
			NamedNodeMap attributes = item.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node item2 = attributes.item(i);
				if (e.getValue() == null) {
					e.setValue(newMap(item.getNodeName(), item.getNodeValue()));
				}
				e.getValue().put(item2.getNodeName(), item2.getNodeValue());
			}
			String nodeValue = item.getFirstChild().getNodeValue();
			if (StringUtils.isNotBlank(nodeValue)) {
				e.setValue(newMap(item.getNodeName(), nodeValue));
			}

		} catch (Exception e2) {
			LOG.trace("", e2);
		}
	}

	private Parent createSplitTreeListDemoNode(Window ownerWindow) {
		ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
		TableView<Map<String, String>> sideTable = new SimpleTableViewBuilder<Map<String, String>>().items(list)
				.build();
		SimpleTreeViewBuilder<Map<String, String>> build = new SimpleTreeViewBuilder<Map<String, String>>()
				.root(newMap("Root", null))
				.onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue));

		Map<Node, TreeItem<Map<String, String>>> allItems = new HashMap<>();

		File file = ResourceFXUtils.toFile("FL94_REL758_20181031061530.xml");
		readXMLFile(build, allItems, file);

		return new VBox(CommonsFX.newButton("Import XML", e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Xml", "*.xml"));
			File newFile = fileChooser.showOpenDialog(ownerWindow);
			if (newFile != null) {
				readXMLFile(build, allItems, newFile);
			}

		}), new SplitPane(build.build(), sideTable));
	}

	private Text newText(Node item) {
		Text text = new Text(item.getNodeName());
		Font font = Font.getDefault();
		text.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
		return text;
	}

	private void onSelectTreeItem(ObservableList<Map<String, String>> list, TableView<Map<String, String>> sideTable,
			TreeItem<Map<String, String>> newValue) {
		list.clear();
		sideTable.getColumns().clear();
		if (newValue != null && newValue.isLeaf()) {
			addColumns(sideTable, newValue.getValue().keySet());
			list.add(newValue.getValue());
		} else if (newValue != null
				&& newValue.getChildren().stream()
				.anyMatch(TreeItem<Map<String, String>>::isLeaf)) {
			List<String> keySet = newValue.getChildren().stream()
					.map(TreeItem<Map<String, String>>::getValue)
					.flatMap(m -> m.keySet().stream()).collect(Collectors.toList());
			keySet.addAll(newValue.getValue().keySet());
			addColumns(sideTable, keySet);
			Map<String, String> newItem = new HashMap<>();
			newItem.putAll(newValue.getValue());
			list.add(newItem);
			if (keySet.size() - 1 == newValue.getChildren().size()) {
				newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
				.forEach(newItem::putAll);
			} else {
				newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
				.forEach(list::add);
			}
		}
	}

	private void readXMLFile(SimpleTreeViewBuilder<Map<String, String>> build,
			Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
		try {
			XmlObject parse = XmlObject.Factory.parse(file);
			Node domNode = parse.getDomNode();
			List<Node> currentNodes = new ArrayList<>();
			currentNodes.add(domNode);
			TreeItem<Map<String, String>> value = new TreeItem<>(
					newMap(domNode.getNodeName(), domNode.getNodeValue()));
			value.setGraphic(newText(domNode));
			build.root(value);
			allItems.put(domNode, value);
			while (!currentNodes.isEmpty()) {
				domNode = currentNodes.remove(0);
				NodeList childNodes = domNode.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					if (item.getNodeType() != Node.TEXT_NODE) {
						currentNodes.add(0, item);
						TreeItem<Map<String, String>> e = new TreeItem<>(
								newMap(item.getNodeName(), item.getNodeValue()));
						allItems.get(domNode).getChildren().add(e);
						allItems.put(item, e);
						e.setGraphic(newText(item));
						addValue(item, e);
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeIOException("ERROR READING", e);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static Map<String, String> newMap(String key, String value) {
		Map<String, String> hashMap = new HashMap<String, String>() {
			@Override
			public String toString() {
				return value;
			}
		};
		hashMap.put(key, value);
		return hashMap;
	}
}
