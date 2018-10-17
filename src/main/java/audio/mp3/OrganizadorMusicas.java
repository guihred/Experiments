package audio.mp3;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleSliderBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class OrganizadorMusicas extends Application {
    private static final Logger LOGGER = HasLogging.log(OrganizadorMusicas.class);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Organizador de Músicas");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 600, 250, Color.WHITE);
        FlowPane gridpane = new FlowPane();
        gridpane.setVgap(10);
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        root.setCenter(gridpane);

        Label listaMusicas = new Label("Lista Músicas");
        GridPane.setHalignment(listaMusicas, HPos.CENTER);
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
        final TableView<Musica> medicamentosEstoqueTable = tabelaMusicas();
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");

        medicamentosEstoqueTable.setItems(getMusicas(musicsDirectory));
        medicamentosEstoqueTable.prefWidthProperty().bind(root.widthProperty().add(-50));
        medicamentosEstoqueTable.prefHeightProperty().bind(root.heightProperty());
        Button buttonEstoque = CommonsFX.newButton("Carregar Musicas", e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                medicamentosEstoqueTable.setItems(getMusicas(selectedFile));
            }
        });
        Button buttonVideos = CommonsFX.newButton("Carregar Vídeos", e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                List<Path> pathByExtension = ResourceFXUtils.getPathByExtension(selectedFile, ".mp4");
                List<Musica> collect = pathByExtension.parallelStream().map(v -> {
                    Musica musica = new Musica();
                    File file = v.toFile();
                    musica.setArquivo(file);
                    musica.setTitulo(file.getName());
                    return musica;
                }).collect(Collectors.toList());
                medicamentosEstoqueTable.setItems(FXCollections.observableArrayList(collect));
            }
        });
        gridpane.getChildren()
                .add(new VBox(listaMusicas, new HBox(buttonEstoque, buttonVideos), medicamentosEstoqueTable));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node[] criarField(String nome, StringProperty propriedade) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(propriedade);
        return new Node[] { new Label(nome), textField };
    }

    private ObservableList<Musica> getMusicas(File file) {
        try {
            return LeitorMusicas.getMusicas(file);
        } catch (Exception e) {
            LOGGER.error("", e);
            return FXCollections.emptyObservableList();
        }
    }

    private void handleMousePressed(final TableView<Musica> musicaTable, MouseEvent event) {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
            Musica selectedItem = musicaTable.getSelectionModel().getSelectedItem();
            if (selectedItem.getTitulo().endsWith(".mp4")) {
                handleVideo(selectedItem);
                return;
            }
            Stage stage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(criarField("Título", selectedItem.tituloProperty()));
            root.getChildren().addAll(criarField("Artista", selectedItem.artistaProperty()));
            root.getChildren().addAll(criarField("Álbum", selectedItem.albumProperty()));
            root.setAlignment(Pos.CENTER);
            Image imageData = ResourceFXUtils.extractEmbeddedImage(selectedItem.getArquivo());
            if (imageData != null) {
                ImageView imageView = new ImageView(imageData);
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
                root.getChildren().addAll(imageView);
            }
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(selectedItem.getArquivo().toURI().toString()));
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
            Button splitButton = CommonsFX.newButton("_Split",
                    e -> {
                        SongUtils.splitAudio(selectedItem.getArquivo(), outFile,
                                mediaPlayer.getTotalDuration().multiply(initialSlider.getValue()),
                                mediaPlayer.getTotalDuration().multiply(finalSlider.getValue()));
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                        LeitorMusicas.saveMetadata(selectedItem, outFile);
                        try {
                            Files.copy(outFile, selectedItem.getArquivo());
                        } catch (IOException e1) {
                            HasLogging.log().error("", e1);
                        }
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
            root.getChildren().add(hbox);

            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnCloseRequest(e -> mediaPlayer.dispose());
            mediaPlayer.play();
            LOGGER.info("{}", selectedItem);
        }
    }

    private Slider addSlider(VBox flow, MediaPlayer mediaPlayer) {
        Slider slider = new SimpleSliderBuilder(0, 1, 0).blocks(1000).build();
        Label label = new Label("00:00");
        slider.valueProperty().addListener(
                (observable, oldValue, newValue) -> label.setText(
                        SongUtils.formatDurationMillis(mediaPlayer.getTotalDuration().multiply(slider.getValue()))));
        flow.getChildren().add(label);
        flow.getChildren().add(slider);
        return slider;
    }

    private void handleVideo(Musica selectedItem) {
        CommonsFX.displayDialog("Convert to Mp3", "Convert", () -> SongUtils.convertToAudio(selectedItem.getArquivo()));
    }

    @SuppressWarnings("unchecked")
    private TableView<Musica> tabelaMusicas() {

        final TableView<Musica> musicaTable = new TableView<>();
        musicaTable.setPrefWidth(600);
        musicaTable.setScaleShape(false);

        musicaTable.setOnMousePressed(event -> handleMousePressed(musicaTable, event));

        TableColumn<Musica, String> tituloMusica = new TableColumn<>("Título");
        tituloMusica.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        tituloMusica.setSortable(true);

        int other = 6;
        tituloMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));
        TableColumn<Musica, String> nomeMusica = new TableColumn<>("Artista");

        nomeMusica.setSortable(true);
        nomeMusica.setCellValueFactory(new PropertyValueFactory<>("artista"));
        nomeMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

        TableColumn<Musica, String> albumMusica = new TableColumn<>("Álbum");
        albumMusica.setSortable(true);
        albumMusica.setCellValueFactory(new PropertyValueFactory<>("album"));
        albumMusica

                .prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));
        TableColumn<Musica, String> anoMusica = new TableColumn<>("Ano");
        anoMusica.setSortable(true);
        anoMusica.setCellValueFactory(new PropertyValueFactory<>("ano"));
        anoMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

        TableColumn<Musica, String> generoMusica = new TableColumn<>("Gênero");
        generoMusica.setSortable(true);
        generoMusica.setCellValueFactory(new PropertyValueFactory<>("genero"));
        generoMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

        musicaTable.getColumns().setAll(tituloMusica, nomeMusica, albumMusica, anoMusica, generoMusica);
        return musicaTable;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
