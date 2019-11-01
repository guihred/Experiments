
package extract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.ConsumerEx;
import utils.CrawlerTask;

public class FileAttrApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		CrawlerTask.insertProxyConfig();
		primaryStage.setTitle("File Attributes Application");
		primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static Parent createSplitTreeListDemoNode() {
		File rootFile = new File("").getAbsoluteFile();
		ObservableList<File> files = FXCollections.observableArrayList(rootFile);
		SimpleTreeViewBuilder<File> root = new SimpleTreeViewBuilder<File>().root(rootFile)
				.cellFactory(FileAttrApp::setText).onSelect(ConsumerEx.makeConsumer(t -> onSelectFile(files, t)));
		TableView<File> tableView = new SimpleTableViewBuilder<File>().items(files)
				.addColumns("creationTime", "lastAccessTime", "lastModifiedTime", "isDirectory", "isOther",
						"isRegularFile", "isSymbolicLink", "size")
				.equalColumns().minWidth(200).build();

		return new VBox(new SplitPane(root.build(), tableView));
	}

	private static void onSelectFile(ObservableList<File> files, TreeItem<File> t) throws IOException {
		if (t != null) {
			File value = t.getValue();
			files.set(0, value);
			BasicFileAttributes attr = Files.readAttributes(value.toPath(), BasicFileAttributes.class);
			System.out.println("creationTime: " + attr.creationTime());
			System.out.println("lastAccessTime: " + attr.lastAccessTime());
			System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
			System.out.println("isDirectory: " + attr.isDirectory());
			System.out.println("isOther: " + attr.isOther());
			System.out.println("isRegularFile: " + attr.isRegularFile());
			System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
			System.out.println("size: " + attr.size());
			if (value.isDirectory() && t.getChildren().isEmpty()) {
				File[] listFiles = value.listFiles();
				for (File file2 : listFiles) {
					t.getChildren().add(new TreeItem<>(file2));
				}

			}
		}
	}

	private static void setText(File file, TreeCell<File> cell) {
		cell.setText(file == null ? "" : file.getName());
	}

}
