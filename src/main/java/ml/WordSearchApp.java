package ml;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleComboBoxBuilder;

public class WordSearchApp extends Application {

    private Map<String, String> filtersMap = new HashMap<>();

    @Override
    public void start(Stage theStage) throws Exception {
        theStage.setTitle("Timeline Example");
        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
        theStage.setScene(theScene);

        ObservableMap<String, Set<String>> observableMap = FXCollections.observableMap(createMap());

        ListView<String> listView = new ListView<>();

        VBox filters = new VBox();
        List<String> collect2 = getLines(ResourceFXUtils.toURI("pt_PT.dic")).collect(Collectors.toList());
        FilteredList<String> lines = FXCollections.observableArrayList(collect2).filtered(e -> true);
        listView.setItems(lines);

        root.getChildren().add(listView);
        root.getChildren().add(filters);
        Button button = new Button("Add");
        root.getChildren().add(button);
        button.setOnAction(a -> {
            Set<String> collect = observableMap.values().stream().flatMap(Set<String>::stream)
                    .collect(Collectors.toSet());
            collect.add(null);
            ObservableList<String> values = FXCollections.observableArrayList(collect);
            FilteredList<String> filtered = values.sorted().filtered(e -> true);
            ComboBox<String> category = new SimpleComboBoxBuilder<String>()
                    .items(observableMap.keySet())
                    .onChange((old, nVal) -> {
                        filtersMap.remove(old);
                        filtered.setPredicate(e -> {
                            Set<String> set = observableMap.get(nVal);
                            return e == null || set != null && set.contains(e);
                        });
                    }).nullOption("Categoria").build();


            ComboBox<String> val = new SimpleComboBoxBuilder<String>().items(filtered).onSelect(s -> {
                String selectedItem = category.selectionModelProperty().get().getSelectedItem();
                if (s == null) {
                    filtersMap.remove(selectedItem);
                    return;
                }
                filtersMap.put(selectedItem, s);

                lines.setPredicate(e -> {
                    Set<Entry<String, String>> entrySet = filtersMap.entrySet();
                    for (Entry<String, String> entry : entrySet) {
                        String s2 = entry.getKey() + "=" + entry.getValue();
                        if (!e.contains(s2 + ",") && !e.contains(s2 + "]")) {
                            return false;
                        }
                    }
                    return true;
                });
            }).build();

            filters.getChildren().add(new HBox(category, val));
        });

        theStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static Stream<String> getLines(URI txtFile) throws IOException {
        return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
                .filter(s -> !s.isEmpty()).distinct();
    }

    private Map<String, Set<String>> createMap() throws IOException {

        return getLines(ResourceFXUtils.toURI("pt_PT.dic"))
                .filter(e -> e.contains("\t"))
                .map(e -> e.replaceAll(".+\t\\[(\\$\\.+\\$)*(.+)\\]", "$2")).flatMap(e -> Stream.of(e.split(",")))
                .collect(Collectors.groupingBy(e -> e.split("=")[0].replaceAll("\\$.+\\$", ""),
                        Collectors.mapping(e -> e.split("=")[1].replaceAll("\\$[A-Z]+", ""), Collectors.toSet())));

    }

}
