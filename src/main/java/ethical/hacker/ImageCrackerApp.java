package ethical.hacker;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import utils.CommonsFX;
import utils.ExcelService;
import utils.ex.FunctionEx;
import utils.fx.PaginatedTableView;

public class ImageCrackerApp extends Application {
    private Map<File, String> crackImages;
    private TableColumn<Integer, ImageView> imageCol;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Cracker");
        HBox root = new HBox();
        ObservableList<File> value = FXCollections.observableArrayList();
        PaginatedTableView paginatedTableView = new PaginatedTableView();
        paginatedTableView.addColumn("Name", i -> value.get(i).getName());
        paginatedTableView.addColumn("Read", i -> textField(value, i));
        imageCol = paginatedTableView.addColumn("Image", i -> {
            File file = value.get(i);
            ImageView imageView = new ImageView(file.toURI().toURL().toExternalForm());
            CommonsFX.bind(imageCol.widthProperty(), imageView.fitWidthProperty());
            return imageView;
        });
        FileChooserBuilder chooserBuilder = new FileChooserBuilder()
                .onSelect(file -> {
                    paginatedTableView.setListSize(0);
                    crackImages = ImageCracker.crackImages(file);
                    paginatedTableView.setListSize(crackImages.size());
                    value.addAll(crackImages.keySet());
                }).title("Images to LOAD");
        root.getChildren()
                .add(new VBox(chooserBuilder.name("Load Images Directory").buildOpenDirectoryButton(),
                        chooserBuilder.extensions("Image", "*.png", "*.jpg", "*.gif", "*.jpeg").name("Open image")
                                .buildOpenButton(),
                        new FileChooserBuilder().name("Export Excel")
                .title("Export Excel").extensions("Excel", "*.xlsx").onSelect(file -> {
                    Map<String, FunctionEx<Integer, Object>> mapa = new LinkedHashMap<>();
                    mapa.put("Name", i -> value.get(i).getName());
                    mapa.put("Read", i -> crackImages.get(value.get(i)));
                    List<Integer> items = paginatedTableView.getFilteredItems();
                    ExcelService.getExcel(items, mapa, file);
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