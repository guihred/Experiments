package audio.mp3;

import com.google.common.io.Files;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleSliderBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.SongUtils;

public final class MusicHandler implements EventHandler<MouseEvent>, HasLogging {
    /**
     * 
     */
    private final TableView<Music> musicaTable;

    public MusicHandler(TableView<Music> musicaTable) {
        this.musicaTable = musicaTable;
    }

    @Override
    public void handle(MouseEvent e) {
        if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            handleMousePressed(musicaTable);
        }
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
                currentSlider
                        .setValue(mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis());
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
        Button splitButton = CommonsFX.newButton("_Split", e -> {
            if (initialSlider.getValue() != 0 || finalSlider.getValue() != 1) {
                splitAndSave(selectedItem, mediaPlayer, initialSlider, finalSlider, outFile, progressIndicator, stage);
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
        root.getChildren().add(progressIndicator);
        root.getChildren().add(hbox);

        stage.setScene(new Scene(root));
        stage.show();
        stage.setOnCloseRequest(e -> mediaPlayer.dispose());
        mediaPlayer.play();
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
                        getLogger().error("", e1);
                    }
                    stage.close();
                });
            }
        });
    }

}