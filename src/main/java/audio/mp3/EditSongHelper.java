package audio.mp3;

import static utils.CommonsFX.createField;
import static utils.RunnableEx.run;

import extract.ImageLoader;
import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public final class EditSongHelper {
    private EditSongHelper() {
    }

    public static void splitAndSave(Music selectedItem, Slider initialSlider, Slider finalSlider, File outFile,
        ProgressIndicator progressIndicator, ObjectProperty<MediaPlayer> mediaPlayer) {
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
                    run(() -> {
                        try (FileOutputStream out = new FileOutputStream(selectedItem.getArquivo())) {
                            Files.copy(outFile.toPath(), out);
                        }
                    });
                    StageHelper.closeStage(progressIndicator);
                });
            }
        });
    }

    public static void findImage(Music selectedItem, Stage stage, ObjectProperty<MediaPlayer> mediaPlayer) {
        String value = MusicReader.getDescription(selectedItem);
        ObservableList<Node> children = FXCollections.observableArrayList();
        children.add(new Text(value));

        final int prefWidth = 300;
        SimpleListViewBuilder<Node> listBuilder = new SimpleListViewBuilder<>();
        ListView<Node> builder = listBuilder.items(children).prefWidth(prefWidth).build();
        listBuilder.onDoubleClick(n -> {
            if (n instanceof ImageView) {
                ImageView view = (ImageView) n;
                Image image = view.getImage();
                selectedItem.setImage(image);
                mediaPlayer.get().stop();
                mediaPlayer.get().dispose();
                MusicReader.saveMetadata(selectedItem);
            }
            StageHelper.closeStage(builder);
            stage.close();
        });
        StageHelper.displayDialog(value, builder);
        ImageLoader.loadImages(children, selectedItem.getAlbum(), selectedItem.getArtista(), selectedItem.getPasta(),
            selectedItem.getTitulo());
    }

    public static void splitAudio(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
        ObjectProperty<Duration> startTime) {
        Duration currentTime = mediaPlayer.get().getTotalDuration().multiply(currentSlider.getValue());
        Music music = new Music(file);
        VBox root = new VBox();
        root.getChildren().addAll(createField("Título", music.tituloProperty()));
        root.getChildren().addAll(createField("Artista", music.artistaProperty()));
        root.getChildren().addAll(createField("Álbum", music.albumProperty()));
        ProgressIndicator progressIndicator = new ProgressIndicator(-1);
        progressIndicator.setVisible(false);
        root.getChildren().addAll(progressIndicator);

        Button splitButton = SimpleButtonBuilder.newButton("_Split", a -> EditSongHelper.splitInFiles(mediaPlayer, file,
            currentSlider, currentTime, music, progressIndicator, startTime));
        root.getChildren().addAll(splitButton);
        StageHelper.displayDialog("Split Multiple", root);
    }

    public static void splitInFiles(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
        Duration currentTime, Music music, ProgressIndicator progressIndicator, ObjectProperty<Duration> startTime) {
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
                    StageHelper.closeStage(progressIndicator);
                });
            }
        });
        startTime.set(currentTime);
    }

    public static void updateCurrentSlider(MediaPlayer mediaPlayer2, Slider currentSlider) {
        if (!currentSlider.isValueChanging()) {
            Duration currentTime = mediaPlayer2.getCurrentTime();
            Duration totalDuration = mediaPlayer2.getTotalDuration();
            double value = currentTime.toMillis() / totalDuration.toMillis();
            currentSlider.setValue(value);
        }
    }

    public static void updateMediaPlayer(MediaPlayer mediaPlayer2, Slider currentSlider, boolean valueChanging) {
        if (!valueChanging) {
            double pos = currentSlider.getValue();
            final Duration seekTo = mediaPlayer2.getTotalDuration().multiply(pos);
            SongUtils.seekAndUpdatePosition(seekTo, currentSlider, mediaPlayer2);
        }
    }
}
