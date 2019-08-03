package audio.mp3;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleTableViewBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class MusicOrganizer extends Application implements HasLogging {

    private static final int HEIGHT = 250;
    private static final int WIDTH = 600;
    private static final Image DEFAULT_VIEW = defaultView();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Organizador de Músicas");
        VBox root = new VBox();
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
        final TableView<Music> musicasTable = tabelaMusicas();
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");
        chooser.setInitialDirectory(musicsDirectory.getParentFile());

        musicasTable.prefWidthProperty().bind(root.widthProperty().subtract(10));
        TextField filterField = new TextField();
        Button buttonMusic = loadMusic(primaryStage, chooser, musicasTable, filterField);
        Button fixMusic = fixMusic(primaryStage, musicasTable, filterField);
        ObservableList<Music> musics = FXCollections.observableArrayList();
        configurarFiltroRapido(filterField, musicasTable, musics);
        Button buttonVideos = loadVideos(primaryStage, chooser, musicasTable, filterField);
        root.getChildren().add(new VBox(new Label("Lista Músicas"),
            new HBox(buttonMusic, buttonVideos, fixMusic, filterField), musicasTable));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarFiltroRapido(TextField filterField, final TableView<Music> musicasEstoqueTable,
        ObservableList<Music> musicas) {
        FilteredList<Music> filteredData = new FilteredList<>(musicas, p -> true);
        musicasEstoqueTable.setItems(filteredData);
        filterField.textProperty().addListener((o, old, newV) -> filteredData.setPredicate(
            musica -> StringUtils.isEmpty(newV) || StringUtils.containsIgnoreCase(musica.toString(), newV)));
    }

    private void convertToImage(Music music, TableCell<Music, Object> cell) {
        cell.setGraphic(view(music.getImage() != null ? music.getImage() : DEFAULT_VIEW));
    }

    private Button fixMusic(Stage primaryStage, TableView<Music> musicasTable, TextField filterField) {

        return CommonsFX.newButton("Consertar Musicas", e -> {
            ObservableList<Music> items = musicasTable.getItems();
            items.stream().filter(m -> StringUtils.isBlank(m.getArtista())).findFirst().ifPresent(m -> {
                VBox vBox = new VBox(10);
                List<String> fields = ClassReflectionUtils.getFields(Music.class);
                for (String name : fields) {
                    Object fieldValue = ClassReflectionUtils.getFieldValue(m, name);
                    if (fieldValue instanceof StringProperty) {
                        StringProperty a = (StringProperty) fieldValue;
                        Text text = new Text(name);
                        TextField textField = new TextField();
                        textField.textProperty().bindBidirectional(a);
                        vBox.getChildren().add(new VBox(text, textField));
                    }
                }

                Image imageData = m.getImage();
                if (imageData != null) {
                    ImageView imageView = new ImageView(imageData);
                    imageView.setFitWidth(50);
                    imageView.setPreserveRatio(true);
                    vBox.getChildren().addAll(imageView);
                }
                Stage stage = new Stage();
                vBox.getChildren().add(CommonsFX.newButton("Fix", f -> {
                    MusicReader.saveMetadata(m);
                    stage.close();
                }));

                stage.setScene(new Scene(vBox));
                stage.show();

            });

        });
    }

    private Button loadMusic(Stage primaryStage, DirectoryChooser chooser, final TableView<Music> musicasTable,
        TextField filterField) {
        return CommonsFX.newButton("Carregar Musicas", e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                ObservableList<Music> musicas = MusicReader.getMusicas(selectedFile);
                musicasTable.setItems(musicas);
                configurarFiltroRapido(filterField, musicasTable, musicas);
            }
        });
    }

    private Button loadVideos(Stage primaryStage, DirectoryChooser chooser, final TableView<Music> musicasTable,
        TextField filterField) {
        return CommonsFX.newButton("Carregar Vídeos", e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                List<Music> videos = ResourceFXUtils.getPathByExtension(selectedFile, ".mp4", ".wma").parallelStream()
                    .map(v -> new Music(v.toFile())).collect(Collectors.toList());
                configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList(videos));
            }
        });
    }

    private TableView<Music> tabelaMusicas() {
        TableView<Music> musicaTable = new SimpleTableViewBuilder<Music>().prefWidth(WIDTH).scaleShape(false)
            .addColumn("Image", this::convertToImage).addColumn("Título", "titulo").addColumn("Artista", "artista")
            .addColumn("Álbum", "album").addColumn("Pasta", "pasta").addColumn("Gênero", "genero")
            .addColumn("Ano", "ano").sortable(true).equalColumns().build();
        musicaTable.setOnMousePressed(new MusicHandler(musicaTable));
        return musicaTable;
    }

    private ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Image defaultView() {
        return new Image(ResourceFXUtils.toExternalForm("fb.jpg"));
    }

}
