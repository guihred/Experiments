
package extract;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
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
import utils.*;

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
        ObservableList<BasicFileAttributes> files = FXCollections.observableArrayList(getAttributes(rootFile));
        SimpleTreeViewBuilder<File> root = new SimpleTreeViewBuilder<File>().root(rootFile)
            .cellFactory(FileAttrApp::setText).onSelect(ConsumerEx.makeConsumer(t -> onSelectFile(files, t)));
        SimpleTableViewBuilder<BasicFileAttributes> builder = new SimpleTableViewBuilder<BasicFileAttributes>()
            .items(files);
        List<Method> allMethodsRecursive = ClassReflectionUtils.getAllMethodsRecursive(BasicFileAttributes.class);
        allMethodsRecursive.sort(Comparator.comparing(Method::getName));

        Map<Class<?>, FunctionEx<Object, ?>> formatterMap = new HashMap<>();
        formatterMap.put(long.class, i -> getFileSize((Long) i));
        formatterMap.put(FileTime.class, i -> getDate((FileTime) i));

        for (Method method : allMethodsRecursive) {
            FunctionEx<Object, ?> functionEx = formatterMap.getOrDefault(method.getReturnType(), i -> i);
            builder.addColumn(method.getName(), (ob, cell) -> cell
                .setText(Objects.toString(SupplierEx.get(() -> functionEx.apply(method.invoke(ob))))));
        }
        TableView<BasicFileAttributes> tableView = builder.equalColumns().minWidth(200).build();
        return new VBox(new SplitPane(root.build(), tableView));
    }

    private static BasicFileAttributes getAttributes(File value) {
        return SupplierEx.get(() -> Files.readAttributes(value.toPath(), BasicFileAttributes.class));
    }

    private static String getDate(FileTime date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(date.toMillis());
    }

    private static String getFileSize(long sizeInBytes) {
        int a0 = (int) Math.floor(Math.log10(sizeInBytes) / Math.log10(1024));
        String[] sizes = { "B", "KB", "MB", "GB", "TB" };
        return a0 < 0 ? "" + sizeInBytes : String.format("%.1f %s", sizeInBytes / Math.pow(1024, a0), sizes[a0]);
    }

    private static void onSelectFile(ObservableList<BasicFileAttributes> files, TreeItem<File> t) {
        if (t != null) {
            File value = t.getValue();
            BasicFileAttributes attr = getAttributes(value);
            files.set(0, attr);
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
