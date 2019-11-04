
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
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private String[] sizes = { "B", "KB", "MB", "GB", "TB" };
    private Map<File, BasicFileAttributes> attrMap = new LinkedHashMap<>();
    private Map<File, Long> sizeMap = new LinkedHashMap<>();

    @Override
    public void start(Stage primaryStage) {
        CrawlerTask.insertProxyConfig();
        primaryStage.setTitle("File Attributes Application");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.show();
    }

    private BasicFileAttributes computeAttributes(File v) {
        return SupplierEx.get(() -> Files.readAttributes(v.toPath(), BasicFileAttributes.class));
    }

    private long computeSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File file2 : listFiles) {
                    size += getSize(file2);
                }
            }
        }
        return getAttributes(file).size() + size;
    }

    private Parent createSplitTreeListDemoNode() {
        File rootFile = new File("").getAbsoluteFile();
        ObservableList<BasicFileAttributes> files = FXCollections.observableArrayList(getAttributes(rootFile));
        SimpleTreeViewBuilder<File> root = new SimpleTreeViewBuilder<File>().root(rootFile).cellFactory(this::setText)
            .onSelect(ConsumerEx.makeConsumer(t -> onSelectFile(files, t)));
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
        SplitPane splitPane = new SplitPane(root.build(), tableView);
        splitPane.setDividerPositions(1. / 5);
        return new VBox(StageHelper.selectDirectory("Select Directory", "Select Directory", f -> {
            root.build().getRoot().getChildren().clear();
            root.root(f);
            files.set(0, getAttributes(f));
        }), splitPane);
    }

    private BasicFileAttributes getAttributes(File value) {
        return attrMap.computeIfAbsent(value, this::computeAttributes);
    }

    private String getDate(FileTime date) {
        return df.format(date.toMillis());
    }

    private String getFileSize(long sizeInBytes) {
        int a0 = (int) Math.floor(Math.log10(sizeInBytes) / Math.log10(1024));
        return a0 < 0 ? "" + sizeInBytes
            : String.format(Locale.ENGLISH, "%.2f %s", sizeInBytes / Math.pow(1024, a0), sizes[a0]);
    }

    private long getSize(File file) {
        return sizeMap.computeIfAbsent(file, this::computeSize) ;
    }

    private void onSelectFile(ObservableList<BasicFileAttributes> files, TreeItem<File> t) {
        if (t != null) {
            File value = t.getValue();
            files.set(0, getAttributes(value));
            if (value.isDirectory() && t.getChildren().isEmpty()) {
                File[] listFiles = value.listFiles();
                if (listFiles != null) {
                    for (File file2 : listFiles) {
                        t.getChildren().add(new TreeItem<>(file2));
                    }
                }
            }
        }
    }

    private void setText(File file, TreeCell<File> cell) {
        cell.setText(file == null ? "" : file.getName() + " " + getFileSize(getSize(file)));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
