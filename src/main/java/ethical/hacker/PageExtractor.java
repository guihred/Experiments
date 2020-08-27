package ethical.hacker;

import extract.ExcelService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.PaginatedTableView;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleButtonBuilder;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class PageExtractor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Page Extractor");
        HBox root = new HBox();

        ObservableList<Document> value = FXCollections.observableArrayList();
        PaginatedTableView paginatedTableView = new PaginatedTableView();

        Button buildOpenDirectoryButton = new FileChooserBuilder()

                .name("Load HTMLs").onSelect(file -> {
                    List<Path> pathByExtension = ResourceFXUtils.getPathByExtension(file, ".html");
                    paginatedTableView.setListSize(pathByExtension.size());
                    value.setAll(pathByExtension.stream().map(Path::toFile)
                            .map(FunctionEx.makeFunction(f -> Jsoup.parse(f, StandardCharsets.UTF_8.displayName())))
                            .collect(Collectors.toList()));
                }).title("HTML to LOAD").buildOpenDirectoryButton();
        TextField selector = new TextField();
        TextField columnName = new TextField();
        Map<String, String> hashMap = new HashMap<>();
        root.getChildren().add(new VBox(new Text("Name"), columnName, new Text("Selector"), selector,
                SimpleButtonBuilder.newButton("Add Field", e -> {
                    String text = selector.getText();
                    hashMap.put(columnName.getText(), text);
                    HasLogging.log().info("{} = {}", columnName.getText(), text);
                    paginatedTableView.addColumn(columnName.getText(), i -> getColumnValue(value, text, i));
                }), buildOpenDirectoryButton, new FileChooserBuilder().name("Export Excel").title("Export Excel")
                        .extensions("Excel", "*.xlsx").onSelect(file -> {
                            List<Integer> items = paginatedTableView.getFilteredItems();
                            List<String> columns = paginatedTableView.getColumns();
                            Map<String, FunctionEx<Integer, Object>> mapa = new LinkedHashMap<>();
                            List<Integer> collect = items;
                            for (String column : columns) {
                                String string = hashMap.get(column);
                                mapa.put(column, i -> getColumnValue(value, string, i));
                            }
                            ExcelService.getExcel(collect, mapa, file);
                        }).buildSaveButton()));
        HBox.setHgrow(paginatedTableView, Priority.ALWAYS);
        root.getChildren().add(paginatedTableView);

        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String getColumnValue(ObservableList<Document> value, String text, Integer i) {
        Elements select = value.get(i).select(text);
        return select.stream().map(Element::text).filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
    }
}
