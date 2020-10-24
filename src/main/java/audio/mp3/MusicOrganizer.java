package audio.mp3;

import extract.Music;
import extract.MusicReader;
import extract.QuickSortML;
import java.util.Comparator;
import java.util.Objects;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.SupplierEx;

public class MusicOrganizer extends Application {

    private static final Image DEFAULT_VIEW = defaultView();
    @FXML
    private Button consertarMusicas;
    @FXML
    private TextField filterText;
    @FXML
    private TableView<Music> musicaTable;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private TableColumn<Music, ImageView> imageColumn;
    private ObservableList<Music> musicas = FXCollections.observableArrayList();

    public double getProgress() {
        return progress.getProgress();
    }

    public void initialize() {
        MusicHandler musicHandler = new MusicHandler(musicaTable);
        imageColumn.setCellValueFactory(m -> new SimpleObjectProperty<>(
                MusicHandler.view(SupplierEx.nonNull(m.getValue().getImage(), DEFAULT_VIEW))));
        SimpleTableViewBuilder.of(musicaTable).sortable(true).multipleSelection().equalColumns().copiable()
                .onMousePressed(musicHandler).onKeyReleased(musicHandler::handle).onSortClicked(this::sortBy);
        configurarFiltroRapido(filterText, musicaTable, FXCollections.observableArrayList());
        consertarMusicas.disableProperty().bind(Bindings.createBooleanBinding(
                () -> musicaTable.getItems().stream().anyMatch(Music::isNotMP3), musicaTable.getItems()));
    }

    public void onActionCarregarMusicas(ActionEvent e) {
        new FileChooserBuilder().name("Carregar _Musicas").title("Carregar Pasta de Músicas")
                .initialDir(ResourceFXUtils.getUserFolder("Music")).onSelect(selectedFile -> {
                    musicas = MusicReader.getMusicas(selectedFile, progress.progressProperty());
                    configurarFiltroRapido(filterText, musicaTable, musicas);
                }).openDirectoryAction(e);
    }

    public void onActionCarregarVideos(ActionEvent e) {
        new FileChooserBuilder().name("Carregar _Vídeos").title("Carregar Pasta de Músicas")
                .initialDir(ResourceFXUtils.getUserFolder("Music")).onSelect(selectedFile -> {
                    musicas.clear();
                    FileTreeWalker.getPathByExtensionAsync(selectedFile, v -> musicas.add(new Music(v.toFile())),
                            ".mp4", ".wma", ".webm", ".wav");
                    configurarFiltroRapido(filterText, musicaTable, musicas);
                }).openDirectoryAction(e);
    }

    public void onActionConsertarMusicas() {
        MusicHandler.fixSongs(musicaTable);
    }

    @Override
    public void start(Stage primaryStage) {
        final int HEIGHT = 250;
        final int WIDTH = 600;
        CommonsFX.loadFXML("Organizador de Músicas", "MusicOrganizer.fxml", this, primaryStage, WIDTH, HEIGHT);
    }

    private void sortBy(String columnName, Boolean ascending) {
        ObservableList<Music> items2 = SupplierEx.nonNull(musicas, musicaTable.getItems());
        Comparator<Music> comparing = Comparator.comparing(
                e -> comparing(e, StringSigaUtils.removerDiacritico(StringSigaUtils.changeCase(columnName))));
        QuickSortML.sort(items2, ascending ? comparing : comparing.reversed());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String comparing(Music e, String columnName) {
        Object fieldValue = ClassReflectionUtils.getFieldValue(e, columnName);
        return StringSigaUtils.removerDiacritico(Objects.toString(fieldValue).toLowerCase().trim());
    }

    private static void configurarFiltroRapido(TextField filterField, final TableView<Music> musicasEstoqueTable,
            ObservableList<Music> musicas) {
        FilteredList<Music> filteredData = new FilteredList<>(musicas, p -> true);
        musicasEstoqueTable.setItems(filteredData);
        filterField.textProperty()
                .addListener((o, old, newV) -> filteredData.setPredicate(musica -> musica.getArquivo().exists()
                        && (StringUtils.isEmpty(newV) || StringUtils.containsIgnoreCase(musica.toString(), newV))));
    }

    private static Image defaultView() {
        return new Image(ResourceFXUtils.toExternalForm("fb.jpg"));
    }

}
