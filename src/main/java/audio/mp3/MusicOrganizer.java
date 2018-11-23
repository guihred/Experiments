package audio.mp3;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class MusicOrganizer extends Application implements HasLogging {

    private static final int HEIGHT = 250;
    private static final int WIDTH = 600;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Organizador de Músicas");
        VBox root = new VBox();
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
		final TableView<Music> musicasTable = tabelaMusicas();
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");
        chooser.setInitialDirectory(musicsDirectory.getParentFile());

		musicasTable.setItems(MusicReader.getMusicas(musicsDirectory));
        musicasTable.prefWidthProperty().bind(root.widthProperty().subtract(10));
		TextField filterField = new TextField();
        Button buttonMusic = loadMusic(primaryStage, chooser, musicasTable, filterField);
        configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList());
        Button buttonVideos = loadVideos(primaryStage, chooser, musicasTable, filterField);
        root.getChildren()
                .add(new VBox(new Label("Lista Músicas"), new HBox(buttonMusic, buttonVideos, filterField), musicasTable));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void configurarFiltroRapido(TextField filterField, final TableView<Music> musicasEstoqueTable,
			ObservableList<Music> musicas) {
		FilteredList<Music> filteredData = new FilteredList<>(musicas, p -> true);
		musicasEstoqueTable.setItems(filteredData);
        filterField.textProperty()
                .addListener((o, old, newV) -> filteredData.setPredicate(musica -> StringUtils.isEmpty(newV)
                        || StringUtils.containsIgnoreCase(musica.toString(), newV)));
	}


    private Button loadMusic(Stage primaryStage, DirectoryChooser chooser,
            final TableView<Music> musicasTable,
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
                List<Music> videos = ResourceFXUtils
                        .getPathByExtension(selectedFile, ".mp4")
                        .parallelStream()
                        .map(v -> new Music(v.toFile()))
                        .collect(Collectors.toList());
                configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList(videos));
            }
        });
    }

    private TableView<Music> tabelaMusicas() {
        TableView<Music> musicaTable = new SimpleTableViewBuilder<Music>()
                .prefWidth(WIDTH)
                .scaleShape(false)
                .addColumn("Título", "titulo")
                .addColumn("Artista", "artista")
                .addColumn("Álbum", "album")
                .addColumn("Ano", "ano")
                .addColumn("Gênero", "genero")
                .addColumn("Pasta", "pasta")
                .equalColumns()
                .build();
        musicaTable.setOnMousePressed(new MusicHandler(musicaTable));
        return musicaTable;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
