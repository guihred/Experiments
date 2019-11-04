
package extract;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleTreeViewBuilder;
import utils.ConsumerEx;
import utils.CrawlerTask;
import utils.StageHelper;
import utils.SupplierEx;

public class FileAttrApp extends Application {
    private String[] sizes = { "B", "KB", "MB", "GB", "TB" };
    private Map<File, BasicFileAttributes> attrMap = new LinkedHashMap<>();
    private Map<File, Long> sizeMap = new LinkedHashMap<>();
    private ObservableList<Data> pieData = FXCollections.observableArrayList();

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
        return new VBox(StageHelper.selectDirectory("Select Directory", "Select Directory", f -> {
            root.build().getRoot().getChildren().clear();
            root.root(f);
        }), splitPane, pieChart

        );
    }

    private BasicFileAttributes getAttributes(File value) {
        return attrMap.computeIfAbsent(value, this::computeAttributes);
    }

    private String getFileSize(long sizeInBytes) {
        int a0 = (int) Math.floor(Math.log10(sizeInBytes) / Math.log10(1024));
        return a0 < 0 ? "" + sizeInBytes
            : String.format(Locale.ENGLISH, "%.2f %s", sizeInBytes / Math.pow(1024, a0), sizes[a0]);
    }

    private long getSize(File file) {
        return sizeMap.computeIfAbsent(file, this::computeSize);
    }

    private void onSelectFile(TreeItem<File> t) {
        if (t != null) {
            File value = t.getValue();
            if (value.isDirectory()) {
                File[] listFiles = value.listFiles();
                if (listFiles != null) {
                    pieData.clear();
                    Arrays.parallelSort(listFiles, Comparator.comparing(e -> !e.isDirectory()));
                    for (File file2 : listFiles) {
                        pieData.add(new PieChart.Data(file2.getName(), getSize(file2)));
                    }
                    pieData.sort(Comparator.comparing(Data::getPieValue));
                    if (t.getChildren().isEmpty()) {
                        for (File file2 : listFiles) {
                            t.getChildren().add(new TreeItem<>(file2));
                        }
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
