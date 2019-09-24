package audio.mp3;

import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import java.io.File;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private boolean close = true;

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
            .addListener(e -> EditSongHelper.updateCurrentSlider(mediaPlayer.get(), currentSlider));
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
        EditSongHelper.findImage(selectedItem, stage, mediaPlayer);
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
            EditSongHelper.splitAndSave(selectedItem, initialSlider, finalSlider, outFile, progressIndicator12,
                mediaPlayer);
            return;
        }
        mediaPlayer.get().stop();
        mediaPlayer.get().dispose();
        MusicReader.saveMetadata(selectedItem);
        if (close) {
            StageHelper.closeStage(e.getTarget());
        }
    }

    public void onActionSplitMultiple() {
        EditSongHelper.splitAudio(mediaPlayer, selectedItem.getArquivo(), currentSlider, startTime);
    }

    public void setClose(boolean close) {
        this.close = close;
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
        EditSongHelper.updateMediaPlayer(mediaPlayer.get(), currentSlider, newValue);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void bind(TextField textField, StringProperty propriedade) {
        textField.textProperty().bindBidirectional(propriedade);
    }

}
