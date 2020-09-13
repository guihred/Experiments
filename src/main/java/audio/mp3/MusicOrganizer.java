package audio.mp3;

import static simplebuilder.SimpleVBoxBuilder.newVBox;

import extract.Music;
import extract.MusicReader;
import extract.QuickSortML;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.*;
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
        imageColumn.setCellValueFactory(
                m -> new SimpleObjectProperty<>(view(SupplierEx.nonNull(m.getValue().getImage(), DEFAULT_VIEW))));
        SimpleTableViewBuilder.of(musicaTable).sortable(true).multipleSelection().equalColumns().copiable()
                .onMousePressed(musicHandler).onKeyReleased(musicHandler::handle).onSortClicked(this::sortBy);
        configurarFiltroRapido(filterText, musicaTable, FXCollections.observableArrayList());
        consertarMusicas.disableProperty().bind(Bindings.createBooleanBinding(
                () -> musicaTable.getItems().stream().anyMatch(Music::isNotMP3), musicaTable.getItems()));
    }

    public void onActionCarregarMusicas(ActionEvent e) {
        new FileChooserBuilder().name("Carregar _Musicas").title("Carregar Pasta de Músicas").onSelect(selectedFile -> {
            musicas = MusicReader.getMusicas(selectedFile, progress.progressProperty());
            configurarFiltroRapido(filterText, musicaTable, musicas);
        }).openDirectoryAction(e);
    }

    public void onActionCarregarVideos(ActionEvent e) {
        new FileChooserBuilder().name("Carregar _Vídeos").title("Carregar Pasta de Músicas").onSelect(selectedFile -> {
            ObservableList<Music> videos = FXCollections.observableArrayList();
            FileTreeWalker.getPathByExtensionAsync(selectedFile, v -> videos.add(new Music(v.toFile())), ".mp4", ".wma",
                    ".webm", ".wav");
            configurarFiltroRapido(filterText, musicaTable, videos);
        }).openDirectoryAction(e);
    }

    public void onActionConsertarMusicas() {
        fixSongs(musicaTable);
    }

    @Override
    public void start(Stage primaryStage) {
        final int HEIGHT = 250;
        final int WIDTH = 600;
        CommonsFX.loadFXML("Organizador de Músicas", "MusicOrganizer.fxml", this, primaryStage, WIDTH, HEIGHT);
    }

    private void sortBy(Entry<String, SortType> columnName) {
        ObservableList<Music> items2 = SupplierEx.nonNull(musicas, musicaTable.getItems());
        Comparator<Music> comparing = Comparator.comparing(
                e -> comparing(e, StringSigaUtils.removerDiacritico(StringSigaUtils.changeCase(columnName.getKey()))));
        SortType value = columnName.getValue();
        QuickSortML.sort(items2, value == SortType.ASCENDING ? comparing : comparing.reversed());
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

    private static void fixSongs(TableView<Music> musicasTable) {
        ObservableList<Music> items = musicasTable.getItems();
        Optional<Music> findFirst = items.stream().filter(m -> StringUtils.isBlank(m.getArtista())
                || StringUtils.isBlank(m.getAlbum()) || m.getTitulo().contains("-")).findFirst();
        if (!findFirst.isPresent()) {
            return;
        }
        Music music = findFirst.get();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        List<String> fields = ClassReflectionUtils.getFields(Music.class);
        for (String name : fields) {
            Object fieldValue = ClassReflectionUtils.getFieldValue(music, name);
            if (fieldValue instanceof StringProperty) {
                StringProperty a = (StringProperty) fieldValue;
                TextField textField = new TextField();
                textField.textProperty().bindBidirectional(a);
                vBox.getChildren().add(newVBox(StringSigaUtils.changeCase(name), textField));
            }
        }
        if (StringUtils.isBlank(music.getAlbum())) {
            music.setAlbum(music.getPasta());
        }
        Image imageData = music.getImage();
        if (imageData != null) {
            vBox.getChildren().addAll(view(imageData));
        }
        vBox.getChildren().add(SimpleButtonBuilder.newButton("_Fix", f -> {
            MusicReader.saveMetadata(music);
            StageHelper.closeStage(vBox);
        }));
        new SimpleDialogBuilder().text("Fix Fields").node(vBox).bindWindow(musicasTable).displayDialog();

    }

    private static ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        final int prefWidth = 50;
        imageView.setFitWidth(prefWidth);
        imageView.setPreserveRatio(true);
        return imageView;
    }

}
