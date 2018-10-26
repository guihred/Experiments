package audio.mp3;

import com.google.common.io.Files;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SongUtils;

public class MusicOrganizer extends Application {
	private static final Logger LOGGER = HasLogging.log();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Organizador de Músicas");

        VBox root = new VBox();

        Label listaMusicas = new Label("Lista Músicas");
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
		final TableView<Music> musicasTable = tabelaMusicas();
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");
        chooser.setInitialDirectory(musicsDirectory.getParentFile());

		musicasTable.setItems(MusicReader.getMusicas(musicsDirectory));
        musicasTable.prefWidthProperty().bind(root.widthProperty().add(-10));
        musicasTable.prefHeightProperty().bind(root.heightProperty().add(-50));
		TextField filterField = new TextField();
        Button buttonMusic = loadMusic(primaryStage, chooser, musicasTable, filterField);
        configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList());
        Button buttonVideos = loadVideos(primaryStage, chooser, musicasTable, filterField);
        root.getChildren()
                .add(new VBox(listaMusicas, new HBox(buttonMusic, buttonVideos, filterField), musicasTable));
        Scene scene = new Scene(root, 600, 250, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button loadVideos(Stage primaryStage, DirectoryChooser chooser, final TableView<Music> musicasTable,
            TextField filterField) {
        return CommonsFX.newButton("Carregar Vídeos", e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                List<Music> videos = ResourceFXUtils
                        .getPathByExtension(selectedFile, ".mp4")
                        .parallelStream()
                        .map(v -> {
                    Music musica = new Music();
                    File file = v.toFile();
                    musica.setArquivo(file);
                    musica.setTitulo(file.getName());
                    return musica;
                }).collect(Collectors.toList());
                configurarFiltroRapido(filterField, musicasTable, FXCollections.observableArrayList(videos));
            }
        });
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

	private void configurarFiltroRapido(TextField filterField, final TableView<Music> musicasEstoqueTable,
			ObservableList<Music> musicas) {
		FilteredList<Music> filteredData = new FilteredList<>(musicas, p -> true);
		musicasEstoqueTable.setItems(filteredData);
		filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(musica -> {
			if (newValue == null || newValue.isEmpty()) {
				return true;
			}
			return musica.toString().toLowerCase().contains(newValue.toLowerCase());
		}));
	}

    private Node[] criarField(String nome, StringProperty propriedade) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(propriedade);
        return new Node[] { new Label(nome), textField };
    }

    private void handleMousePressed(final TableView<Music> songsTable) {
            Music selectedItem = songsTable.getSelectionModel().getSelectedItem();
            if (selectedItem.getTitulo().endsWith(".mp4")) {
                CommonsFX.displayDialog("Convert", "_Convert to Mp3",
                        () -> SongUtils.convertToAudio(selectedItem.getArquivo()));
                return;
            }
            Stage stage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(criarField("Título", selectedItem.tituloProperty()));
            root.getChildren().addAll(criarField("Artista", selectedItem.artistaProperty()));
            root.getChildren().addAll(criarField("Álbum", selectedItem.albumProperty()));
            root.setAlignment(Pos.CENTER);
            Image imageData = SongUtils.extractEmbeddedImage(selectedItem.getArquivo());
            if (imageData != null) {
                ImageView imageView = new ImageView(imageData);
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
                root.getChildren().addAll(imageView);
            }
            Media media = new Media(selectedItem.getArquivo().toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            
            Slider currentSlider = addSlider(root, mediaPlayer);
            currentSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue && !newValue) {
                    double pos = currentSlider.getValue();
                    final Duration seekTo = mediaPlayer.getTotalDuration().multiply(pos);
                    SongUtils.seekAndUpdatePosition(seekTo, currentSlider, mediaPlayer);
                }
            });
            mediaPlayer.currentTimeProperty().addListener(e -> {
                mediaPlayer.getCurrentTime();
                if (!currentSlider.isValueChanging()) {
                    currentSlider.setValue(
                            mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis());
                }
            });
            Slider initialSlider = addSlider(root, mediaPlayer);
            initialSlider.setValue(0);
            Slider finalSlider = addSlider(root, mediaPlayer);
            finalSlider.setValue(0.999);
            mediaPlayer.totalDurationProperty().addListener(e -> finalSlider.setValue(1));
            File outFile = new File("out", selectedItem.getArquivo().getName());
			ProgressIndicator progressIndicator = new ProgressIndicator(0);
			progressIndicator.setVisible(false);
            Button splitButton = CommonsFX.newButton("_Split",
                    e -> {
                        if (initialSlider.getValue() != 0 || finalSlider.getValue() != 1) {
							splitAndSave(selectedItem, mediaPlayer, initialSlider, finalSlider, outFile,
									progressIndicator, stage);
							return;
                        }
						mediaPlayer.stop();
						mediaPlayer.dispose();
						MusicReader.saveMetadata(selectedItem);
                        stage.close();
                    });

            Button stopButton = CommonsFX.newButton("_Play/Pause", e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            });
            HBox hbox = new HBox(stopButton, splitButton);
            hbox.setAlignment(Pos.CENTER);
			root.getChildren().add(progressIndicator);
            root.getChildren().add(hbox);

            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnCloseRequest(e -> mediaPlayer.dispose());
            mediaPlayer.play();
            LOGGER.info("{}", selectedItem);
    }

    private void splitAndSave(Music selectedItem, MediaPlayer mediaPlayer, Slider initialSlider, Slider finalSlider,
			File outFile, ProgressIndicator progressIndicator, Stage stage) {
		DoubleProperty progress = SongUtils.splitAudio(selectedItem.getArquivo(), outFile,
                mediaPlayer.getTotalDuration().multiply(initialSlider.getValue()),
                mediaPlayer.getTotalDuration().multiply(finalSlider.getValue()));
		progressIndicator.progressProperty().bind(progress);
		progressIndicator.setVisible(true);
		progress.addListener((v, o, n) -> {
			if (n.intValue() == 1) {
				Platform.runLater(() -> {
					mediaPlayer.stop();
					mediaPlayer.dispose();
					MusicReader.saveMetadata(selectedItem, outFile);
					try {
						Files.copy(outFile, selectedItem.getArquivo());
                    } catch (Exception e1) {
						LOGGER.error("", e1);
					}
					stage.close();
				});
			}
		});
    }

    private Slider addSlider(VBox flow, MediaPlayer mediaPlayer) {
        Slider slider = new SimpleSliderBuilder(0, 1, 0).blocks(1000).build();
        Label label = new Label("00:00");
        
        label.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> mediaPlayer.getTotalDuration() == null ? "00:00"
                                : SongUtils.formatDurationMillis(
                                        mediaPlayer.getTotalDuration().multiply(slider.getValue())),
                        slider.valueProperty(), mediaPlayer.totalDurationProperty()));
        
        flow.getChildren().add(label);
        flow.getChildren().add(slider);
        return slider;
    }

    private TableView<Music> tabelaMusicas() {
        TableView<Music> musicaTable = new SimpleTableViewBuilder<Music>()
                .prefWidth(600)
                .scaleShape(false)
                .addColumn("Título", "titulo")
                .addColumn("Artista", "artista")
                .addColumn("Álbum", "album")
                .addColumn("Ano", "ano")
                .addColumn("Gênero", "genero")
                .equalColumns()
                .build();
        musicaTable.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
                handleMousePressed(musicaTable);
            }
        });
        return musicaTable;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
