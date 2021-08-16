package ethical.hacker;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import utils.CommonsFX;
import utils.ExcelService;
import utils.ex.FunctionEx;
import utils.fx.PaginatedTableView;

public class ImageCrackerApp extends Application {
    private Map<File, String> crackImages;
    private TableColumn<Integer, ImageView> imageCol;
    private ObservableList<File> imageFiles = FXCollections.observableArrayList();
    @FXML
    private TextField textField;
    @FXML
    private PaginatedTableView paginatedTableView;

    private FileChooserBuilder chooserBuilder;

    public void initialize() {
        paginatedTableView.addColumn("Name", i -> imageFiles.get(i).getName());
        paginatedTableView.addColumn("Read", this::textField);
        imageCol = paginatedTableView.addColumn("Image", i -> {
            File file = imageFiles.get(i);
            ImageView imageView = new ImageView(file.toURI().toURL().toExternalForm());
            CommonsFX.bind(imageCol.widthProperty(), imageView.fitWidthProperty());
            return imageView;
        });
        paginatedTableView.setColumnsWidth(5, 5, 5);
        chooserBuilder = new FileChooserBuilder().onSelect(file -> {
            paginatedTableView.setListSize(0);
            crackImages = ImageCracker.crackImages(file);
            paginatedTableView.setListSize(crackImages.size());
            imageFiles.addAll(crackImages.keySet());
        }).title("Images to LOAD");
    }

    public void onActionExportExcel(ActionEvent e) {
        new FileChooserBuilder().name("Export Excel").title("Export Excel").extensions("Excel", "*.xlsx")
                .onSelect(file -> {
                    Map<String, FunctionEx<Integer, Object>> mapa = new LinkedHashMap<>();
                    mapa.put("Name", i -> imageFiles.get(i).getName());
                    mapa.put("Read", i -> crackImages.get(imageFiles.get(i)));
                    List<Integer> items = paginatedTableView.getFilteredItems();
                    ExcelService.getExcel(items, mapa, file);
                }).saveFileAction(e);
    }

    public void onActionLoadImagesDirectory(ActionEvent e) {
        chooserBuilder.name("Load Images Directory").openDirectoryAction(e);
    }

    public void onActionOpenimage(ActionEvent e) {
        chooserBuilder.name("Load Images Directory").openFileAction(e);
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Image Cracker", "ImageCrackerApp.fxml", this, primaryStage);
    }

    private TextArea textField(Integer i) {
        TextArea textArea = new TextArea(crackImages.get(imageFiles.get(i)));
        textArea.textProperty().addListener((ob, o, n) -> crackImages.put(imageFiles.get(i), n));
        return textArea;
    }

    public static void main(String[] args) {
        launch(args);
    }
}