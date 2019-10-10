package audio.mp3;

import extract.Music;
import extract.SongUtils;
import java.nio.file.Files;
import javafx.beans.NamedArg;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import simplebuilder.SimpleDialogBuilder;

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
                    () -> Files.deleteIfExists(selectedItem.getArquivo().toPath()))
                .displayDialog();
            return;
        }
        new EditSongController(selectedItem).show();
    }

}