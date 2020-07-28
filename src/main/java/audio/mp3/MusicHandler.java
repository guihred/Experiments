package audio.mp3;

import extract.Music;
import extract.SongUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import simplebuilder.SimpleDialogBuilder;
import utils.ConsumerEx;
import utils.ExtractUtils;
import utils.ResourceFXUtils;

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

    public static void handleMousePressed(List<Music> songs) {
        if (songs.isEmpty() || songs.stream().anyMatch(e -> !e.getArquivo().exists())) {
            return;
        }
        Music music = songs.get(0);
        if (!music.isNotMP3()) {
            new EditSongController(music).show();
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
        }, () -> songs.forEach(ConsumerEx.makeConsumer(s -> onConvertionEnded(s.getArquivo()))));
        dialog.displayDialog();
    }

    public static void handleMousePressed(Music songs) {
        handleMousePressed(Arrays.asList(songs));
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