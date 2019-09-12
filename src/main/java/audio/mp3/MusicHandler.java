package audio.mp3;

import static utils.CommonsFX.createField;
import static utils.RunnableEx.run;
import static utils.SongUtils.updateCurrentSlider;
import static utils.SongUtils.updateMediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
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
import simplebuilder.SimpleListViewBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.SongUtils;
import utils.StageHelper;

public final class MusicHandler implements EventHandler<MouseEvent> {
    private static final int IMAGE_MAX_WIDTH = 300;
    /**
     * 
     */
    private final TableView<Music> musicaTable;
    private Duration startTime = Duration.ZERO;
    private MediaPlayer mediaPlayer;

    public MusicHandler(@NamedArg("musicaTable") TableView<Music> musicaTable) {
        this.musicaTable = musicaTable;
    }

    public TableView<Music> getMusicaTable() {
        return musicaTable;
    }

    @Override
    public void handle(MouseEvent e) {
        if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            handleMousePressed(getMusicaTable().getSelectionModel().getSelectedItem());
        }
    }

    public void handleMousePressed(Music selectedItem) {
        if (!selectedItem.getArquivo().exists()) {
            return;
        }

        if (selectedItem.isNotMP3()) {
            StageHelper.displayDialog(String.format("Convert%n%s", selectedItem.getArquivo().getName()),
                "_Convert to Mp3", () -> SongUtils.convertToAudio(selectedItem.getArquivo()),
                () -> Files.deleteIfExists(selectedItem.getArquivo().toPath()));
            return;
        }
        Stage stage = new Stage();
        VBox root = new VBox();
        root.getChildren().addAll(createField("Título", selectedItem.tituloProperty()));
        root.getChildren().addAll(createField("Artista", selectedItem.artistaProperty()));
        root.getChildren().addAll(createField("Álbum", selectedItem.albumProperty()));
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

        Slider currentSlider = SongUtils.addSlider(root, mediaPlayer);
        currentSlider.valueChangingProperty()
            .addListener((observable, oldValue, newValue) -> updateMediaPlayer(mediaPlayer, currentSlider, newValue));
        mediaPlayer.currentTimeProperty().addListener(e -> updateCurrentSlider(mediaPlayer, currentSlider));
        Slider initialSlider = SongUtils.addSlider(root, mediaPlayer);
        initialSlider.setValue(0);
        Slider finalSlider = SongUtils.addSlider(root, mediaPlayer);
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

    private void findImage(Music selectedItem, Stage stage) {
        String value = MusicReader.getDescription(selectedItem);
        ObservableList<Node> children = FXCollections.observableArrayList();
        children.add(new Text(value));

        final int prefWidth = 300;
        SimpleListViewBuilder<Node> tableBuilder = new SimpleListViewBuilder<>();
        ListView<Node> builder = tableBuilder.items(children).prefWidth(prefWidth).build();

        Stage dialog = StageHelper.displayDialog(value, builder);
        tableBuilder.onDoubleClick(n -> {
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
                    run(() -> Files.copy(outFile.toPath(), new FileOutputStream(selectedItem.getArquivo())));
                    stage.close();
                });
            }
        });
    }

    private void splitAudio(File file, Slider currentSlider) {
        Duration currentTime = mediaPlayer.getTotalDuration().multiply(currentSlider.getValue());
        Music music = new Music(file);
        VBox root = new VBox();
        root.getChildren().addAll(createField("Título", music.tituloProperty()));
        root.getChildren().addAll(createField("Artista", music.artistaProperty()));
        root.getChildren().addAll(createField("Álbum", music.albumProperty()));
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
                            .addListener(
                                (o, oldValue, newValue) -> updateMediaPlayer(mediaPlayer, currentSlider, newValue));
                        mediaPlayer.currentTimeProperty()
                            .addListener(c -> updateCurrentSlider(mediaPlayer, currentSlider));
                        mediaPlayer.play();
                    });
                    stage.close();
                });
            }
        });
        startTime = currentTime;
    }


}