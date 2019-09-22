package audio.mp3;

import static extract.SongUtils.updateCurrentSlider;
import static extract.SongUtils.updateMediaPlayer;
import static utils.CommonsFX.createField;
import static utils.RunnableEx.run;

import extract.ImageLoader;
import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.StageHelper;

public class EditSongController extends Application {
    @FXML
    private TextField albumField;
    @FXML
    private Label label10;
    @FXML
    private Slider finalSlider;
    @FXML
    private TextField tituloField;
    @FXML
    private Label label8;
    @FXML
    private Slider initialSlider;
    @FXML
    private TextField artistaField;

    @FXML
    private Label label6;
    @FXML
    private Slider currentSlider;

    @FXML
    private ImageView imageView;
    @FXML
    private ProgressIndicator progressIndicator12;
    private final Music selectedItem;
    private final ObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<>();
    private final ObjectProperty<Duration> startTime = new SimpleObjectProperty<>(Duration.ZERO);

    public EditSongController() {
        selectedItem = MusicReader.readTags(ResourceFXUtils.toFile("TeenTitans.mp3"));
    }

    public EditSongController(Music selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void initialize() {
        Media media = new Media(selectedItem.getArquivo().toURI().toString());
        mediaPlayer.set(new MediaPlayer(media));

        SongUtils.bindSlider(mediaPlayer.get(), currentSlider, label6);
        SongUtils.bindSlider(mediaPlayer.get(), initialSlider, label8);
        SongUtils.bindSlider(mediaPlayer.get(), finalSlider, label10);
        bind(albumField, selectedItem.albumProperty());
        bind(tituloField, selectedItem.tituloProperty());
        bind(artistaField, selectedItem.artistaProperty());

        mediaPlayer.get().currentTimeProperty()
            .addListener(e -> SongUtils.updateCurrentSlider(mediaPlayer.get(), currentSlider));
        mediaPlayer.get().totalDurationProperty().addListener(e -> finalSlider.setValue(1));
        finalSlider.setValue(1 - 1. / 1000);
        Image imageData = MusicReader.extractEmbeddedImage(selectedItem.getArquivo());
        if (imageData != null) {
            imageView.setImage(imageData);
        }
        mediaPlayer.get().play();
    }

    public void onActionFindImage(ActionEvent e) {
        Node target = (Node) e.getTarget();
        Stage stage = (Stage) target.getScene().getWindow();
        EditSongController.findImage(selectedItem, stage, mediaPlayer);
    }

    public void onActionPlayPause() {
        if (mediaPlayer.get().getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.get().pause();
        } else {
            mediaPlayer.get().play();
        }
    }

    public void onActionSplit(ActionEvent e) {
        File outFile = ResourceFXUtils.getOutFile(selectedItem.getArquivo().getName());
        if (initialSlider.getValue() != 0 || finalSlider.getValue() != 1) {
            EditSongController.splitAndSave(selectedItem, initialSlider, finalSlider, outFile, progressIndicator12, mediaPlayer);
            return;
        }
        mediaPlayer.get().stop();
        mediaPlayer.get().dispose();
        MusicReader.saveMetadata(selectedItem);

        StageHelper.closeStage(e.getTarget());
    }

    public void onActionSplitMultiple() {
        EditSongController.splitAudio(mediaPlayer, selectedItem.getArquivo(), currentSlider, startTime);
    }

    public void show() {
        start(new Stage());
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Edit Song", "EditSong.fxml", this, primaryStage);
        primaryStage.setOnCloseRequest(e -> mediaPlayer.get().dispose());
    }

    @SuppressWarnings("unused")
    public void update(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
        SongUtils.updateMediaPlayer(mediaPlayer.get(), currentSlider, newValue);
    }

    public static void findImage(Music selectedItem, Stage stage, ObjectProperty<MediaPlayer> mediaPlayer) {
        String value = MusicReader.getDescription(selectedItem);
        ObservableList<Node> children = FXCollections.observableArrayList();
        children.add(new Text(value));
    
        final int prefWidth = 300;
        SimpleListViewBuilder<Node> tableBuilder = new SimpleListViewBuilder<>();
        ListView<Node> builder = tableBuilder.items(children).prefWidth(prefWidth).build();
        tableBuilder.onDoubleClick(n -> {
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

    public static void main(String[] args) {
        launch(args);
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
    
        Button splitButton = SimpleButtonBuilder.newButton("_Split", a -> EditSongController.splitInFiles(mediaPlayer, file, currentSlider,
            currentTime, music, progressIndicator, startTime));
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

    private static void bind(TextField textField, StringProperty propriedade) {
        textField.textProperty().bindBidirectional(propriedade);
    }

}
