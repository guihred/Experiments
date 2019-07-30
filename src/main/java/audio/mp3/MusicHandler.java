package audio.mp3;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SongUtils;

public final class MusicHandler implements EventHandler<MouseEvent>, HasLogging {
    private static final int IMAGE_MAX_WIDTH = 300;
    /**
     * 
     */
    private final TableView<Music> musicaTable;
    private Duration startTime = Duration.ZERO;
    private MediaPlayer mediaPlayer;

    public MusicHandler(TableView<Music> musicaTable) {
        this.musicaTable = musicaTable;
    }

    @Override
    public void handle(MouseEvent e) {
        if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            handleMousePressed(musicaTable.getSelectionModel().getSelectedItem());
        }
    }

    public void handleMousePressed(Music selectedItem) {
        if (selectedItem.getTitulo().matches(".+\\.(mp4|wma)")) {
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
        Image imageData = MusicReader.extractEmbeddedImage(selectedItem.getArquivo());
        if (imageData != null) {
            ImageView imageView = new ImageView(imageData);
            imageView.setFitWidth(IMAGE_MAX_WIDTH);
            imageView.setPreserveRatio(true);
            root.getChildren().addAll(imageView);
        }
        Media media = new Media(selectedItem.getArquivo().toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        Slider currentSlider = addSlider(root);
        currentSlider.valueChangingProperty()
            .addListener((observable, oldValue, newValue) -> updateMediaPlayer(currentSlider, newValue));
        mediaPlayer.currentTimeProperty().addListener(e -> updateCurrentSlider(currentSlider));
        Slider initialSlider = addSlider(root);
        initialSlider.setValue(0);
        Slider finalSlider = addSlider(root);
        finalSlider.setValue(1 - 1. / 1000);
        mediaPlayer.totalDurationProperty().addListener(e -> finalSlider.setValue(1));
        File outFile = ResourceFXUtils.getOutFile(selectedItem.getArquivo().getName());
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
        progressIndicator.setVisible(false);
        Button splitButton = CommonsFX.newButton("_Split", e -> {
            if (initialSlider.getValue() != 0 || finalSlider.getValue() != 1) {
                splitAndSave(selectedItem, initialSlider, finalSlider, outFile, progressIndicator, stage);
                return;
            }
            mediaPlayer.stop();
            mediaPlayer.dispose();
            MusicReader.saveMetadata(selectedItem);
            stage.close();
        });
        Button splitMultipleButton = CommonsFX.newButton("Split _Multiple",
            e -> splitAudio(selectedItem.getArquivo(), currentSlider));
        Button findImage = CommonsFX.newButton("_Find Image", e -> findImage(selectedItem, stage));

        Button stopButton = CommonsFX.newButton("_Play/Pause", e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        });
        HBox hbox = new HBox(stopButton, splitButton, splitMultipleButton, findImage);
        root.getChildren().add(progressIndicator);
        root.getChildren().add(hbox);

        stage.setScene(new Scene(root));
        stage.show();
        stage.setOnCloseRequest(e -> mediaPlayer.dispose());
        mediaPlayer.play();
    }

    private Slider addSlider(VBox flow) {
        Slider slider = new SimpleSliderBuilder(0, 1, 0).blocks(100_000).build();
        Label label = new Label("00:00");

        label.textProperty()
            .bind(Bindings.createStringBinding(
                () -> mediaPlayer.getTotalDuration() == null ? "00:00"
                    : SongUtils.formatFullDuration(mediaPlayer.getTotalDuration().multiply(slider.getValue())),
                slider.valueProperty(), mediaPlayer.totalDurationProperty()));

        flow.getChildren().add(label);
        flow.getChildren().add(slider);
        return slider;
    }

    private Node[] criarField(String nome, StringProperty propriedade) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(propriedade);
        return new Node[] { new Label(nome), textField };
    }

    private void findImage(Music selectedItem, Stage stage) {
        String value = MusicReader.getDescription(selectedItem);
        ObservableList<Node> children = FXCollections.observableArrayList();
        children.add(new Text(value));

        SimpleTableViewBuilder<Node> tableBuilder = new SimpleTableViewBuilder<>();
        final int prefWidth = 300;
        TableView<Node> tableView = tableBuilder.addColumn("Image", (p, cell) -> {
            cell.setAlignment(Pos.CENTER);
            cell.setGraphic(p);
        }).items(children).prefWidth(prefWidth).equalColumns()

            .build();
        Stage dialog = CommonsFX.displayDialog(value, tableView);
        tableBuilder.onDoubleClick((Node n) -> {
            if (n instanceof ImageView) {
                ImageView view = (ImageView) n;
                Image image = view.getImage();
                selectedItem.setImage(image);
                mediaPlayer.stop();
                mediaPlayer.dispose();
                MusicReader.saveMetadata(selectedItem);
            }
            dialog.close();
            stage.close();
        });
        ImageLoader.loadImages(children, selectedItem.getAlbum(), selectedItem.getArtista(), selectedItem.getPasta(),
            selectedItem.getTitulo());
    }

    private void splitAndSave(Music selectedItem, Slider initialSlider, Slider finalSlider, File outFile,
        ProgressIndicator progressIndicator, Stage stage) {
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
                        Files.copy(outFile.toPath(), new FileOutputStream(selectedItem.getArquivo()));
                    } catch (Exception e1) {
                        getLogger().error("", e1);
                    }
                    stage.close();
                });
            }
        });
    }

    private void splitAudio(File file, Slider currentSlider) {
        Duration currentTime = mediaPlayer.getTotalDuration().multiply(currentSlider.getValue());
        Music music = new Music(file);
        VBox root = new VBox();
        root.getChildren().addAll(criarField("Título", music.tituloProperty()));
        root.getChildren().addAll(criarField("Artista", music.artistaProperty()));
        root.getChildren().addAll(criarField("Álbum", music.albumProperty()));
        root.setAlignment(Pos.CENTER);
        ProgressIndicator progressIndicator = new ProgressIndicator(-1);
        progressIndicator.setVisible(false);
        root.getChildren().addAll(progressIndicator);

        Stage stage = new Stage();
        Button splitButton = CommonsFX.newButton("_Split",
            a -> splitInFiles(file, currentSlider, currentTime, music, progressIndicator, stage));
        root.getChildren().addAll(splitButton);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void splitInFiles(File file, Slider currentSlider, Duration currentTime, Music music,
        ProgressIndicator progressIndicator, Stage stage) {
        mediaPlayer.dispose();
        String format = music.getArtista().isEmpty()
            ? String.format("%s.mp3", music.getTitulo().replaceAll("\\..+", ""))
            : String.format("%s-%s.mp3", music.getTitulo().replaceAll("\\..+", ""), music.getArtista());

        File newFile = ResourceFXUtils.getOutFile(format);
        DoubleProperty splitAudio = SongUtils.splitAudio(file, newFile, startTime, currentTime);
        progressIndicator.progressProperty().bind(splitAudio);
        progressIndicator.setVisible(true);
        splitAudio.addListener((ob, old, n) -> {
            progressIndicator.setVisible(true);
            if (n.intValue() == 1) {
                Platform.runLater(() -> {
                    mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
                    mediaPlayer.totalDurationProperty().addListener(b -> {
                        SongUtils.seekAndUpdatePosition(currentTime, currentSlider, mediaPlayer);
                        currentSlider.valueChangingProperty()
                            .addListener((o, oldValue, newValue) -> updateMediaPlayer(currentSlider, newValue));
                        mediaPlayer.currentTimeProperty().addListener(c -> updateCurrentSlider(currentSlider));
                        mediaPlayer.play();
                    });
                    stage.close();
                });
            }
        });
        startTime = currentTime;
    }

    private void updateCurrentSlider(Slider currentSlider) {
        mediaPlayer.getCurrentTime();
        if (!currentSlider.isValueChanging()) {
            currentSlider.setValue(mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis());
        }
    }

    private void updateMediaPlayer(Slider currentSlider, boolean valueChanging) {
        if (!valueChanging) {
            double pos = currentSlider.getValue();
            final Duration seekTo = mediaPlayer.getTotalDuration().multiply(pos);
            SongUtils.seekAndUpdatePosition(seekTo, currentSlider, mediaPlayer);
        }
    }

}