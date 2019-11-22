package audio.mp3;

import extract.Music;
import extract.SongUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.NamedArg;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import simplebuilder.SimpleDialogBuilder;
import utils.CrawlerTask;
import utils.ResourceFXUtils;

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

    public static void handleMousePressed(Music selectedItem) {
        if (!selectedItem.getArquivo().exists()) {
            return;
        }

        if (selectedItem.isNotMP3()) {
            new SimpleDialogBuilder().text(String.format("Convert%n%s", selectedItem.getArquivo().getName()))
                .button("_Convert to Mp3", () -> SongUtils.convertToAudio(selectedItem.getArquivo()),
                    () -> {
                        Path path = selectedItem.getArquivo().toPath();
                        File outFile = ResourceFXUtils.getOutFile(path.toFile().getName());
                        CrawlerTask.copy(path, outFile);
                        Files.deleteIfExists(path);
                    })
                .displayDialog();
            return;
        }
        new EditSongController(selectedItem).show();
    }

}