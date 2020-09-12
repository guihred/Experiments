package audio.mp3;

import static simplebuilder.SimpleVBoxBuilder.newVBox;

import extract.Music;
import extract.MusicReader;
import extract.QuickSortML;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.*;
import utils.ClassReflectionUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.SupplierEx;

public class MusicOrganizer extends Application {

    private static final int PREF_SIZE = 40;
    private static final int HEIGHT = 250;
    private static final int WIDTH = 600;
    private static final Image DEFAULT_VIEW = defaultView();

    private ProgressIndicator progress = new ProgressIndicator(0);
    private ObservableList<Music> musicas = FXCollections.observableArrayList();

    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        TableView<Music> musicasTable = tabelaMusicas();
        musicasTable.prefWidthProperty().bind(root.widthProperty().subtract(10));
        musicasTable.prefHeightProperty().bind(root.heightProperty().subtract(30));
        TextField filterField = new TextField();
        Button buttonMusic = loadMusic(musicasTable, filterField);
        Button fixMusic = fixMusic(musicasTable);
        ObservableList<Music> musics = FXCollections.observableArrayList();
        configurarFiltroRapido(filterField, musicasTable, musics);
        Button buttonVideos = loadVideos(musicasTable, filterField);
        progress.setMinSize(PREF_SIZE, PREF_SIZE);
        root.getChildren().add(new VBox(new Label("Lista Músicas"),
                new HBox(buttonMusic, buttonVideos, fixMusic, filterField, progress), musicasTable));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        // CommonsFX.addCSS(scene, "filesComparator.css");
        primaryStage.setTitle("Organizador de Músicas");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Button loadMusic(TableView<Music> musicasTable, TextField filterField) {
        return new FileChooserBuilder().name("Carregar _Musicas").title("Carregar Pasta de Músicas")
                .onSelect(selectedFile -> {
                    musicas = MusicReader.getMusicas(selectedFile, progress.progressProperty());
                    configurarFiltroRapido(filterField, musicasTable, musicas);
                }).buildOpenDirectoryButton();
    }

    private TableView<Music> tabelaMusicas() {
        SimpleTableViewBuilder<Music> simpleTableViewBuilder = new SimpleTableViewBuilder<>();
        TableView<Music> musicaTable = simpleTableViewBuilder.prefWidth(WIDTH).scaleShape(false)
                .addColumn("Image", music -> view(SupplierEx.nonNull(music.getImage(), DEFAULT_VIEW)))
                .addColumn("Título", "titulo").addColumn("Artista", "artista").addColumn("Álbum", "album")
                .addColumn("Pasta", "pasta").addColumn("Gênero", "genero").addColumn("LastModified", "lastModified")
                .sortable(true).multipleSelection().equalColumns().build();
        simpleTableViewBuilder.onSortClicked(columnName -> {
            ObservableList<Music> items2 = SupplierEx.nonNull(musicas, musicaTable.getItems());
            Comparator<Music> comparing = Comparator.comparing(e -> comparing(e,
                    StringSigaUtils.removerDiacritico(StringSigaUtils.changeCase(columnName.getKey()))));
            SortType value = columnName.getValue();
            QuickSortML.sort(items2, value == SortType.ASCENDING ? comparing : comparing.reversed());
        });
        MusicHandler value = new MusicHandler(musicaTable);
        musicaTable.setOnMousePressed(value);
        musicaTable.setOnKeyReleased(value::handle);
        return musicaTable;
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

    private static Button fixMusic(TableView<Music> musicasTable) {
        Button newButton = SimpleButtonBuilder.newButton("_Consertar Musicas", e -> fixSongs(musicasTable));
        newButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> musicasTable.getItems().stream().anyMatch(Music::isNotMP3), musicasTable.getItems()));
        return newButton;
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

    private static Button loadVideos(final TableView<Music> musicasTable, TextField filterField) {
        return new FileChooserBuilder().name("Carregar _Vídeos").title("Carregar Pasta de Músicas")
                .onSelect(selectedFile -> {
                    ObservableList<Music> videos = FXCollections.observableArrayList();
                    FileTreeWalker.getPathByExtensionAsync(selectedFile, v -> videos.add(new Music(v.toFile())),
                            ".mp4", ".wma", ".webm", ".wav");
                    configurarFiltroRapido(filterField, musicasTable, videos);
                }).buildOpenDirectoryButton();
    }

    private static ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        final int prefWidth = 50;
        imageView.setFitWidth(prefWidth);
        imageView.setPreserveRatio(true);
        return imageView;
    }

}
