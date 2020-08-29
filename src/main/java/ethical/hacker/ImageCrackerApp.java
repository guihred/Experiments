package ethical.hacker;

import extract.ExcelService;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.PaginatedTableView;
import simplebuilder.FileChooserBuilder;
import utils.FunctionEx;

public class ImageCrackerApp extends Application {
    private Map<File, String> crackImages;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Image Cracker");
        HBox root = new HBox();

        ObservableList<File> value = FXCollections.observableArrayList();
        PaginatedTableView paginatedTableView = new PaginatedTableView();
        paginatedTableView.addColumn("Name", i -> value.get(i).getName());
        paginatedTableView.addColumn("Read", i -> textField(value, i));
        paginatedTableView.addColumn("Image", i -> new ImageView(value.get(i).toURI().toURL().toExternalForm()));
        Button buildOpenDirectoryButton = new FileChooserBuilder()
                .name("Load Images").onSelect(file -> {
                    crackImages = ImageCracker.crackImages(file);
                    paginatedTableView.setListSize(crackImages.size());
                    value.setAll(crackImages.keySet());
                }).title("Images to LOAD").buildOpenDirectoryButton();
        root.getChildren().add(new VBox(buildOpenDirectoryButton, new FileChooserBuilder().name("Export Excel")
                .title("Export Excel").extensions("Excel", "*.xlsx").onSelect(file -> {
                    List<Integer> items = paginatedTableView.getFilteredItems();
                    Map<String, FunctionEx<Integer, Object>> mapa = new LinkedHashMap<>();
                    List<Integer> collect = items;
                    mapa.put("Name", i -> value.get(i).getName());
                    mapa.put("Read", i -> crackImages.get(value.get(i)));
                    ExcelService.getExcel(collect, mapa, file);
                }).buildSaveButton()));
        HBox.setHgrow(paginatedTableView, Priority.ALWAYS);
        root.getChildren().add(paginatedTableView);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private TextArea textField(ObservableList<File> value, Integer i) {
        TextArea textArea = new TextArea(crackImages.get(value.get(i)));
        textArea.textProperty().addListener((ob, o, n) -> crackImages.put(value.get(i), n));
        return textArea;
    }

    public static void main(String[] args) {
        launch(args);
    }
}