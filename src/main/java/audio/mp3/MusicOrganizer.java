package audio.mp3;

import static simplebuilder.SimpleVBoxBuilder.newVBox;
import static utils.StringSigaUtils.nonNull;

import extract.Music;
import extract.MusicReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.ClassReflectionUtils;
import utils.ResourceFXUtils;
import utils.StageHelper;

public class MusicOrganizer extends Application {

    private static final int HEIGHT = 250;
    private static final int WIDTH = 600;
    private static final Image DEFAULT_VIEW = defaultView();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        TableView<Music> musicasTable = tabelaMusicas();
        musicasTable.prefWidthProperty().bind(root.widthProperty().subtract(10));
        TextField filterField = new TextField();
        Button buttonMusic = loadMusic(musicasTable, filterField);
        Button fixMusic = fixMusic(musicasTable);
        ObservableList<Music> musics = FXCollections.observableArrayList();
        configurarFiltroRapido(filterField, musicasTable, musics);
        Button buttonVideos = loadVideos(musicasTable, filterField);
        root.getChildren().add(new VBox(new Label("Lista Músicas"),
            new HBox(buttonMusic, buttonVideos, fixMusic, filterField), musicasTable));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Organizador de Músicas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void convertToImage(Music music, TableCell<Music, Object> cell) {
        cell.setGraphic(view(nonNull(music.getImage(), DEFAULT_VIEW)));
    }

    private TableView<Music> tabelaMusicas() {
        TableView<Music> musicaTable = new SimpleTableViewBuilder<Music>().prefWidth(WIDTH).scaleShape(false)
            .addColumn("Image", this::convertToImage).addColumn("Título", "titulo").addColumn("Artista", "artista")
            .addColumn("Álbum", "album").addColumn("Pasta", "pasta").addColumn("Gênero", "genero")
            .addColumn("Ano", "ano").sortable(true).equalColumns().build();
        musicaTable.setOnMousePressed(new MusicHandler(musicaTable));
        return musicaTable;
    }

    public static void main(String[] args) {
        launch(args);
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
                vBox.getChildren().add(newVBox(name, textField));
            }
        }
        if (StringUtils.isBlank(music.getAlbum())) {
            music.setAlbum(music.getPasta());
        }
        Image imageData = music.getImage();
        if (imageData != null) {
            ImageView imageView = new ImageView(imageData);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            vBox.getChildren().addAll(imageView);
        }
        vBox.getChildren().add(SimpleButtonBuilder.newButton("_Fix", f -> {
            MusicReader.saveMetadata(music);
            StageHelper.closeStage(vBox);
        }));
        new SimpleDialogBuilder().text("Fix Fields").button(vBox).bindWindow(musicasTable).displayDialog();

    }

    private static Button loadMusic(TableView<Music> musicasTable, TextField filterField) {
        return StageHelper.selectDirectory("Carregar _Musicas", "Carregar Pasta de Músicas", selectedFile -> {
            ObservableList<Music> musicas = MusicReader.getMusicas(selectedFile);
            musicasTable.setItems(musicas);
            configurarFiltroRapido(filterField, musicasTable, musicas);
        });
    }

    private static Button loadVideos(final TableView<Music> musicasTable, TextField filterField) {
        return StageHelper.selectDirectory("Carregar _Vídeos", "Carregar Pasta de Músicas", selectedFile -> {
            List<Music> videos = ResourceFXUtils.getPathByExtension(selectedFile, ".mp4", ".wma").parallelStream()
                .map(v -> new Music(v.toFile())).collect(Collectors.toList());
            configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList(videos));
        });
    }

    private static ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(true);
        return imageView;
    }

}
