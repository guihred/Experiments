
package extract;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class FileAttrApp extends Application {
    private Map<File, BasicFileAttributes> attrMap = new LinkedHashMap<>();
    private Map<File, Long> sizeMap = new LinkedHashMap<>();
    private ObservableList<Data> pieData = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        ExtractUtils.insertProxyConfig();
        primaryStage.setTitle("File Attributes Application");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.show();
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
        size += SupplierEx.getIgnore(() -> getAttributes(file).size(), 0L);
        return size;
    }

    private Parent createSplitTreeListDemoNode() {
        File rootFile = new File("").getAbsoluteFile();
        SimpleTreeViewBuilder<File> root = new SimpleTreeViewBuilder<File>().root(rootFile).cellFactory(this::setText)
            .onSelect(ConsumerEx.makeConsumer(this::onSelectFile));

        PieChart pieChart = new PieChart();
        SplitPane splitPane = new SplitPane(root.build(), pieChart);
        splitPane.setDividerPositions(1. / 5);
        pieChart.setData(pieData);
        pieChart.setTitle("Directory Files");
        pieChart.setLegendVisible(false);
        pieChart.setClockwise(false);
        pieChart.setStartAngle(90);
        pieChart.setLabelsVisible(true);
        return new VBox(new FileChooserBuilder().name("Select Directory").title("Select Directory").onSelect(f -> {
            root.build().getRoot().getChildren().clear();
            root.root(f);
        }).buildOpenDirectoryButton(), splitPane, pieChart);
    }

    private BasicFileAttributes getAttributes(File value) {
        return attrMap.computeIfAbsent(value, ResourceFXUtils::computeAttributes);
    }

    private long getSize(File file) {
        return sizeMap.computeIfAbsent(file, this::computeSize);
    }

    private void onSelectFile(TreeItem<File> t) {
        if (t == null || !t.getValue().isDirectory()) {
            return;
        }
        File[] listFiles = t.getValue().listFiles();
        if (listFiles == null) {
            return;
        }
        pieData.clear();
        Arrays.parallelSort(listFiles,
                Comparator.comparing((File e) -> !e.isDirectory()).thenComparing(t1 -> -getSize(t1)));
        for (File file2 : listFiles) {
            pieData.add(new PieChart.Data(file2.getName(), getSize(file2)));
        }
        pieData.sort(Comparator.comparing(Data::getPieValue));
        if (!t.getChildren().isEmpty()) {
            return;
        }
        for (File file2 : listFiles) {
            t.getChildren().add(new TreeItem<>(file2));
        }
    }

    private void setText(File file, TreeCell<File> cell) {
        cell.setText(file == null ? "" : file.getName() + " " + StringSigaUtils.getFileSize(getSize(file)));
        cell.setGraphic(file == null ? null : getGraphic(file));

    }

    public static void main(String[] args) {
        launch(args);
    }


    private static ImageView getGraphic(File file) {
        ImageView value = new ImageView(Extension.getExtension(file).getFile());
        value.setFitHeight(10);
        value.setFitWidth(10);
        return value;
    }

}
