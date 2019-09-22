package extract;

import static extract.SongUtils.updateCurrentSlider;
import static extract.SongUtils.updateMediaPlayer;
import static utils.CommonsFX.createField;
import static utils.RunnableEx.run;

import audio.mp3.EditSongController;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.ResourceFXUtils;
import utils.StageHelper;

public final class MusicHandler implements EventHandler<MouseEvent> {
    private final TableView<Music> musicaTable;

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

    public static void findImage(Music selectedItem, Stage stage, ObjectProperty<MediaPlayer> mediaPlayer) {
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
                mediaPlayer.get().stop();
                mediaPlayer.get().dispose();
                MusicReader.saveMetadata(selectedItem);
            }
            dialog.close();
            stage.close();
        });
        ImageLoader.loadImages(children, selectedItem.getAlbum(), selectedItem.getArtista(), selectedItem.getPasta(),
            selectedItem.getTitulo());
    }

    public static void handleMousePressed(Music selectedItem) {
        if (!selectedItem.getArquivo().exists()) {
            return;
        }

        if (selectedItem.isNotMP3()) {
            StageHelper.displayDialog(String.format("Convert%n%s", selectedItem.getArquivo().getName()),
                "_Convert to Mp3", () -> SongUtils.convertToAudio(selectedItem.getArquivo()),
                () -> Files.deleteIfExists(selectedItem.getArquivo().toPath()));
            return;
        }
        showEditSong(selectedItem);
    }

    public static void showEditSong(Music selectedItem) {
        new EditSongController(selectedItem).show();
    }

    public static void splitAndSave(Music selectedItem, Slider initialSlider, Slider finalSlider, File outFile,
        ProgressIndicator progressIndicator, Stage stage, ObjectProperty<MediaPlayer> mediaPlayer) {
        DoubleProperty progress = SongUtils.splitAudio(selectedItem.getArquivo(), outFile,
            mediaPlayer.get().getTotalDuration().multiply(initialSlider.getValue()),
            mediaPlayer.get().getTotalDuration().multiply(finalSlider.getValue()));
        progressIndicator.progressProperty().bind(progress);
        progressIndicator.setVisible(true);
        progress.addListener((v, o, n) -> {
            if (n.intValue() == 1) {
                Platform.runLater(() -> {
                    mediaPlayer.get().stop();
                    mediaPlayer.get().dispose();
                    MusicReader.saveMetadata(selectedItem, outFile);
                    run(() -> Files.copy(outFile.toPath(), new FileOutputStream(selectedItem.getArquivo())));
                    stage.close();
                });
            }
        });
    }

    public static void splitAudio(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
        ObjectProperty<Duration> startTime) {
        Duration currentTime = mediaPlayer.get().getTotalDuration().multiply(currentSlider.getValue());
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
        Button splitButton = SimpleButtonBuilder.newButton("_Split", a -> splitInFiles(mediaPlayer, file, currentSlider,
            currentTime, music, progressIndicator, stage, startTime));
        root.getChildren().addAll(splitButton);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void splitInFiles(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
        Duration currentTime, Music music, ProgressIndicator progressIndicator, Stage stage,
        ObjectProperty<Duration> startTime) {
        mediaPlayer.get().dispose();
        String format = music.getArtista().isEmpty()
            ? String.format("%s.mp3", music.getTitulo().replaceAll("\\..+", ""))
            : String.format("%s-%s.mp3", music.getTitulo().replaceAll("\\..+", ""), music.getArtista());

        File newFile = ResourceFXUtils.getOutFile(format);
        DoubleProperty splitAudio = SongUtils.splitAudio(file, newFile, startTime.get(), currentTime);
        progressIndicator.progressProperty().bind(splitAudio);
        progressIndicator.setVisible(true);
        splitAudio.addListener((ob, old, n) -> {
            progressIndicator.setVisible(true);
            if (n.intValue() == 1) {
                Platform.runLater(() -> {
                    mediaPlayer.set(new MediaPlayer(new Media(file.toURI().toString())));
                    mediaPlayer.get().totalDurationProperty().addListener(b -> {
                        SongUtils.seekAndUpdatePosition(currentTime, currentSlider, mediaPlayer.get());
                        currentSlider.valueChangingProperty().addListener(
                            (o, oldValue, newValue) -> updateMediaPlayer(mediaPlayer.get(), currentSlider, newValue));
                        mediaPlayer.get().currentTimeProperty()
                            .addListener(c -> updateCurrentSlider(mediaPlayer.get(), currentSlider));
                        mediaPlayer.get().play();
                    });
                    stage.close();
                });
            }
        });
        startTime.set(currentTime);
    }

}