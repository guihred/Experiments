package ml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.ResourceFXUtils;
import utils.ex.SupplierEx;

public class WordSearchApp extends Application {

    private Map<String, String> filtersMap = new HashMap<>();
    private Pattern compile = Pattern.compile("");

    @Override
    public void start(Stage theStage) {
        theStage.setTitle("Word Search Example");
        BorderPane root = new BorderPane();
        Scene theScene = new Scene(root);
        theStage.setScene(theScene);

        ObservableMap<String, Set<String>> wordMap =
                FXCollections.observableMap(SupplierEx.get(WordSearchApp::createMap));

        ListView<String> listView = new ListView<>();

        VBox filters = new VBox();
        TextField filterField = new TextField();

        List<String> allLines =
                SupplierEx.get(() -> getLines(ResourceFXUtils.toPath("pt_PT.dic")).collect(Collectors.toList()));
        FilteredList<String> lines = FXCollections.observableArrayList(allLines).filtered(e -> true);
        filterField.textProperty().addListener((o, old, value) -> {
            compile = SupplierEx.getIgnore(() -> Pattern.compile(value), compile);
            lines.setPredicate(row -> isInCriteria(row) && hasValue(value, row));
        });
        filters.getChildren().add(filterField);
        listView.setItems(lines);
        root.setCenter(filters);
        root.setLeft(listView);
        Button button = SimpleButtonBuilder.newButton("Add", a -> search(wordMap, filters, lines));
        filters.getChildren().add(button);
        Text size = new Text("");
        root.setBottom(size);
        lines.predicateProperty().addListener(e -> size.setText("" + lines.size()));
        search(wordMap, filters, lines);

        theStage.show();

    }

    private boolean hasValue(String value, String row) {
        return StringUtils.isBlank(value) || StringUtils.containsIgnoreCase(row, value)
                || compile.matcher(row.replaceFirst("[/\t].+", "")).find();
    }

    private boolean isInCriteria(String e) {
        Set<Entry<String, String>> entrySet = filtersMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String s2 = entry.getKey() + "=" + entry.getValue();
            if (!e.contains(s2 + ",") && !e.contains(s2 + "]")) {
                return false;
            }
        }
        return true;
    }

    private void search(ObservableMap<String, Set<String>> observableMap, VBox filters, FilteredList<String> lines) {
        Set<String> splitLines =
                observableMap.values().stream().flatMap(Set<String>::stream).collect(Collectors.toSet());
        splitLines.add(null);
        ObservableList<String> values = FXCollections.observableArrayList(splitLines);
        FilteredList<String> filtered = values.sorted().filtered(e -> true);
        List<String> keySet = observableMap.keySet().stream().collect(Collectors.toList());
        keySet.add(0, "");
        ComboBox<String> category = new SimpleComboBoxBuilder<String>().items(keySet).onChange((old, nVal) -> {
            filtersMap.remove(old);
            filtered.setPredicate(e -> {
                Set<String> set = observableMap.get(nVal);
                return e == null || set == null || set.contains(e);
            });
        }).build();

        ComboBox<String> val = new SimpleComboBoxBuilder<String>().items(filtered).onChange((o, s) -> {
            String selectedItem = category.selectionModelProperty().get().getSelectedItem();
            if (s == null) {
                filtersMap.remove(selectedItem);
            } else {
                filtersMap.put(selectedItem, s);
            }
            lines.setPredicate(t -> isInCriteria(t) && hasValue(compile.pattern(), t));
        }).build();

        filters.getChildren().add(filters.getChildren().size() - 1, new HBox(category, val));
    }

    public static Stream<String> getLines(Path txtFile) throws IOException {
        return Files.lines(txtFile, StandardCharsets.UTF_8).sequential().map(String::trim).filter(s -> !s.isEmpty())
                .distinct();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Map<String, Set<String>> createMap() throws IOException {
        return getLines(ResourceFXUtils.toPath("pt_PT.dic")).filter(e -> e.contains("\t"))
                .map(e -> e.replaceAll(".+\t\\[(\\$\\.+\\$)*(.+)\\]", "$2")).flatMap(e -> Stream.of(e.split(",")))
                .collect(Collectors.groupingBy(e -> e.split("=")[0].replaceAll("\\$.+\\$", ""),
                        Collectors.mapping(e -> e.split("=")[1].replaceAll("\\$[A-Z]+", ""), Collectors.toSet())));

    }

}
