package audio.mp3;

import static simplebuilder.SimpleVBoxBuilder.newVBox;

import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.NamedArg;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.StageHelper;
import utils.ClassReflectionUtils;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;

public final class MusicHandler implements EventHandler<MouseEvent> {
    private final TableView<Music> musicaTable;

    public MusicHandler(@NamedArg("musicaTable") TableView<Music> musicaTable) {
        this.musicaTable = musicaTable;
    }

    public TableView<Music> getMusicaTable() {
        return musicaTable;
    }

    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            handleMousePressed(getMusicaTable().getSelectionModel().getSelectedItems());
        }
    }
    @Override
    public void handle(MouseEvent e) {
        if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            handleMousePressed(getMusicaTable().getSelectionModel().getSelectedItems());
        }
    }

    public static void fixSongs(TableView<Music> musicasTable) {
        ObservableList<Music> items = musicasTable.getItems();
        Optional<Music> findFirst = items.stream().filter(m -> StringUtils.isBlank(m.getArtista())
                || StringUtils.isBlank(m.getAlbum()) || m.getTitulo().contains("-")).findFirst();
        if (!findFirst.isPresent()) {
            return;
        }
        Music music = findFirst.get();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        List<String> fields = ClassReflectionUtils.getFields(Music.class);
        for (String name : fields) {
            Object fieldValue = ClassReflectionUtils.getFieldValue(music, name);
            if (fieldValue instanceof StringProperty) {
                StringProperty a = (StringProperty) fieldValue;
                TextField textField = new TextField();
                textField.textProperty().bindBidirectional(a);
                vBox.getChildren().add(newVBox(StringSigaUtils.changeCase(name), textField));
            }
        }
        if (StringUtils.isBlank(music.getAlbum())) {
            music.setAlbum(music.getPasta());
        }
        Image imageData = music.getImage();
        if (imageData != null) {
            vBox.getChildren().addAll(MusicHandler.view(imageData));
        }
        vBox.getChildren().add(SimpleButtonBuilder.newButton("_Fix", f -> {
            MusicReader.saveMetadata(music);
            StageHelper.closeStage(vBox);
        }));
        new SimpleDialogBuilder().text("Fix Fields").node(vBox).bindWindow(musicasTable).displayDialog();
    
    }

    public static void handleMousePressed(List<Music> songs) {
        if (songs.isEmpty() || songs.stream().anyMatch(e -> !e.getArquivo().exists())) {
            return;
        }
        Music music = songs.get(0);
        if (!music.isNotMP3()) {
            new SimpleDialogBuilder().show(EditSongController.class,music);
            
            return;
        }
        SimpleDialogBuilder dialog = new SimpleDialogBuilder();
        dialog.text("Convert");
        for (Music m : songs) {
            dialog.text(String.format("%n%s", m.getArquivo().getName()));
        }
        dialog.button("_Convert to Mp3", () -> {
            DoubleBinding finalResult = new SimpleDoubleProperty(0).add(0);
            for (Music m : songs) {
                DoubleProperty convertToAudio = SongUtils.convertToAudio(m.getArquivo());
                finalResult = finalResult.add(convertToAudio);
            }
            return finalResult.divide(songs.size());
        }, () -> ConsumerEx.foreach(songs,s -> onConvertionEnded(s.getArquivo())));
        dialog.displayDialog();
    }

    public static void handleMousePressed(Music songs) {
        handleMousePressed(Arrays.asList(songs));
    }

    public static ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        final int prefWidth = 50;
        imageView.setFitWidth(prefWidth);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private static void onConvertionEnded(File arquivo) throws IOException {
        File file = new File(arquivo.getParentFile(),
                arquivo.getName().replaceAll("\\..+", ".mp3"));
        if (!file.exists()) {
            return;
        }
        Path path = arquivo.toPath();
        File outFile = ResourceFXUtils.getOutFile(path.toFile().getName());
        ExtractUtils.copy(path, outFile);
        Files.deleteIfExists(path);
    }

}