package ethical.hacker;

import extract.ExcelService;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import org.slf4j.Logger;
import paintexp.PrintConfig;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleButtonBuilder;
import utils.*;

public class PageExtractor extends Application {

    private static final Logger LOG = HasLogging.log();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Page Extractor");
        HBox root = new HBox();
        ExtractUtils.insertProxyConfig();

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
        Map<String, String> columnMap = new HashMap<>();
        root.getChildren().add(new VBox(new Text("Name"), columnName, new Text("Selector"), selector,
                SimpleButtonBuilder.newButton("Add Text Field",
                        e -> addTextField(value, paginatedTableView, selector, columnName, columnMap)),
                SimpleButtonBuilder.newButton("Add _HTML Field",
                        e -> addHTMLField(value, paginatedTableView, selector, columnName, columnMap)),
                buildOpenDirectoryButton,
                new FileChooserBuilder().name("Export Excel").title("Export Excel").extensions("Excel", "*.xlsx")
                        .onSelect(file -> exportExcel(value, paginatedTableView, columnMap, file)).buildSaveButton(),
                SimpleButtonBuilder.newButton("Export PDF", e -> exportPDF(paginatedTableView, e))

        )

        );
        HBox.setHgrow(paginatedTableView, Priority.ALWAYS);
        root.getChildren().add(paginatedTableView);

        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addHTMLField(ObservableList<Document> value, PaginatedTableView paginatedTableView,
            TextField selector, TextField columnName, Map<String, String> columnMap) {
        String text = selector.getText();
        columnMap.put(columnName.getText(), text);
        LOG.info("{} = {}", columnName.getText(), text);
        Map<Integer, Node> map = new HashMap<>();
        paginatedTableView.addColumn(columnName.getText(),
                i -> map.computeIfAbsent(i, j -> getColumnHtml(value, text, j)));
    }

    private static void addTextField(ObservableList<Document> value, PaginatedTableView paginatedTableView,
            TextField selector, TextField columnName, Map<String, String> columnMap) {
        String text = selector.getText();
        columnMap.put(columnName.getText(), text);
        LOG.info("{} = {}", columnName.getText(), text);
        paginatedTableView.addColumn(columnName.getText(), i -> getColumnValue(value, text, i));
    }

    private static void exportExcel(ObservableList<Document> value, PaginatedTableView paginatedTableView,
            Map<String, String> columnMap, File file) {
        List<Integer> items = paginatedTableView.getFilteredItems();
        List<String> columns = paginatedTableView.getColumns();
        Map<String, FunctionEx<Integer, Object>> mapa = new LinkedHashMap<>();
        List<Integer> collect = items;
        for (String column : columns) {
            String string = columnMap.get(column);
            mapa.put(column, i -> getColumnValue(value, string, i));
        }
        ExcelService.getExcel(collect, mapa, file);
    }

    private static void exportPDF(PaginatedTableView paginatedTableView, ActionEvent e0) {
        List<List<Object>> elements = paginatedTableView.getElements();
        List<String> columns = paginatedTableView.getColumns();
        List<Image> collect =
                elements.stream()
                        .flatMap(list -> list.stream()
                                .map(e -> e instanceof ImageView ? ((ImageView) e).getImage()
                                        : ImageFXUtils.toImage(new Text(Objects.toString(e)))))
                        .flatMap(PageExtractor::splitImage)
                        .collect(Collectors.toList());
        PrintConfig printConfig = new PrintConfig(collect);
        printConfig.show();
        printConfig.setLinesColumns(columns.size(), 1);
        printConfig.printToPDF(e0);

    }

    private static Node getColumnHtml(ObservableList<Document> value, String text, Integer i) {
        Document document = value.get(i);
        Elements select = document.select(text);
        String collect =
                select.stream().map(Element::html).filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
        String format = String.format("<!DOCTYPE html><html><body>%s</body></html>", collect);
        File outFile = ResourceFXUtils.getOutFile("png/" + (text + i).replaceAll("[ #,\\.]+", "-") + ".png");
        ExtractUtils.saveHtmlImage(format, outFile);
        return new ImageView(ResourceFXUtils.convertToURL(outFile).toExternalForm());
    }

    private static String getColumnValue(ObservableList<Document> value, String text, Integer i) {
        Elements select = value.get(i).select(text);
        return select.stream().map(Element::text).filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
    }

    private static Stream<? extends Image> splitImage(Image t) {
        if (t.getHeight() / 1131 > 2) {
            int w = (int) t.getWidth();
            int h = (int) t.getHeight();
            WritableImage writableImage = new WritableImage(w, h / 2);
            writableImage.getPixelWriter().setPixels(0, 0, w, h / 2, t.getPixelReader(), 0, 0);
            WritableImage writableImage2 = new WritableImage(w, h / 2);
            writableImage2.getPixelWriter().setPixels(0, 0, w, h / 2, t.getPixelReader(), 0, h / 2);
            return Stream.of(writableImage, writableImage2);
        }
        return Stream.of(t);
    }
}
