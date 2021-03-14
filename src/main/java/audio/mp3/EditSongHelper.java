package audio.mp3;

import static utils.CommonsFX.createField;
import static utils.ex.RunnableEx.run;

import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import extract.web.ImageLoader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

final class EditSongHelper {
    private static final long MAX_SIZE = 1_000_000L;
    private static final Logger LOG = HasLogging.log();

    private EditSongHelper() {
    }

    public static void splitAndSave(Music selectedItem, Slider initialSlider, Slider finalSlider, File outFile,
            ProgressIndicator progressIndicator, ObjectProperty<MediaPlayer> mediaPlayer) {
        MediaPlayer mediaPlayer2 = mediaPlayer.get();
        if (!isAbleToChange(mediaPlayer2)) {
            LOG.error("Cannot Split And Save Audio {}", selectedItem);
            return;
        }
        DoubleProperty progress = SongUtils.splitAudio(selectedItem.getArquivo(), outFile,
                mediaPlayer.get().getTotalDuration().multiply(initialSlider.getValue()),
                mediaPlayer.get().getTotalDuration().multiply(finalSlider.getValue()));
        progressIndicator.progressProperty().bind(progress);
        progressIndicator.setVisible(true);
        CommonsFX.runInPlatform(() -> SongUtils.stopAndDispose(mediaPlayer.get()));
        progress.addListener((v, o, n) -> {
            if (n.intValue() == 1) {
                RunnableEx.sleepSeconds(2);
                CommonsFX.runInPlatform(() -> {
                    run(() -> {
                        MusicReader.saveMetadata(selectedItem, outFile);
                        File arquivo = selectedItem.getArquivo();
                        ExtractUtils.copy(outFile, arquivo);
                    });
                    StageHelper.closeStage(progressIndicator);
                });
            }
        });
    }

    public static void splitAudio(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
            ObjectProperty<Duration> startTime) {
        MediaPlayer mediaPlayer2 = mediaPlayer.get();
        if (!isAbleToChange(mediaPlayer2)) {
            LOG.error("CAN'T Split Audio {}", file);
            return;
        }
        LOG.info("Splitting status {}", mediaPlayer2.getStatus());
        Duration currentTime = mediaPlayer2.getTotalDuration().multiply(currentSlider.getValue());
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
        new SimpleDialogBuilder().text("Split Multiple").node(root).bindWindow(currentSlider).displayDialog();
    }

    public static void splitInFiles(ObjectProperty<MediaPlayer> mediaPlayer, File file, Slider currentSlider,
            Duration currentTime, Music music, ProgressIndicator progressIndicator,
            ObjectProperty<Duration> startTime) {
        SongUtils.stopAndDispose(mediaPlayer.get());
        String format = FunctionEx.mapIf(music.getArtista(), a -> String.format("%s-%s.mp3", music.getTitulo(), a),
                music.getTitulo().replaceAll("\\..+", ".mp3"));

        File newFile = ResourceFXUtils.getOutFile("mp3/" + format);
        DoubleProperty splitAudio = SongUtils.splitAudio(file, newFile, startTime.get(), currentTime);
        progressIndicator.progressProperty().bind(splitAudio);
        progressIndicator.setVisible(true);
        splitAudio.addListener((ob, old, n) -> {
            progressIndicator.setVisible(true);
            if (n.intValue() != 1) {
                return;
            }
            CommonsFX.runInPlatform(() -> {
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
        if (!valueChanging && isAbleToChange(mediaPlayer2)) {
            double pos = currentSlider.getValue();
            Duration seekTo = mediaPlayer2.getTotalDuration().multiply(pos);
            SongUtils.seekAndUpdatePosition(seekTo, currentSlider, mediaPlayer2);
        }
    }

    static void findImage(Music selectedItem, Stage stage, ObjectProperty<MediaPlayer> mediaPlayer) {
        String value = MusicReader.getDescription(selectedItem);
        ObservableList<Node> children = FXCollections.observableArrayList();

        final int prefWidth = 300;
        SimpleListViewBuilder<Node> listBuilder = new SimpleListViewBuilder<>();
        ListView<Node> builder = listBuilder.items(children).prefWidth(prefWidth).build();
        listBuilder.onDoubleClick(n -> {
            StageHelper.closeStage(builder);
            stage.close();
            ImageView view = (ImageView) n;
            Image image = view.getImage();
            selectedItem.setImage(image);
            SongUtils.stopAndDispose(mediaPlayer.get());
            MusicReader.saveMetadata(selectedItem);
        });
        File parentFile = selectedItem.getArquivo().getParentFile();
        VBox.setVgrow(builder, Priority.ALWAYS);
        new SimpleDialogBuilder().title("Searching Image").text(value).node(builder).bindWindow(stage).displayDialog();
        ImageLoader.loadImages(children, selectedItem.getAlbum(), selectedItem.getArtista(), selectedItem.getPasta(),
                selectedItem.getTitulo());
        RunnableEx.runNewThread(() -> {
            RunnableEx.sleepSeconds(5);
            List<Path> pathByExtension = FileTreeWalker.getPathByExtension(parentFile, ".jpg", ".png");
            return
            pathByExtension.stream().filter(e -> {
                BasicFileAttributes computeAttributes = ResourceFXUtils.computeAttributes(e.toFile());
                return computeAttributes.size() < MAX_SIZE;// LESS than a MB
            }).limit(10)
                    .collect(Collectors.toList());
        }, pathByExtension -> {
            if (!pathByExtension.isEmpty()) {
                CommonsFX.runInPlatform(() -> {
                    LOG.info("ADDING FOLDER IMAGES");
                    List<ImageView> collect = pathByExtension.stream().map(
                            e -> ImageLoader.convertToImage(ResourceFXUtils.convertToURL(e.toFile()).toExternalForm()))
                            .collect(Collectors.toList());
                    children.addAll(0, collect);
                });
            }
        });
    }

    private static boolean isAbleToChange(MediaPlayer mediaPlayer2) {
        return mediaPlayer2 != null && mediaPlayer2.getStatus() != Status.UNKNOWN;
    }
}
