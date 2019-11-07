package fxtests;

import audio.mp3.FilesComparator;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.TableView;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import utils.ResourceFXUtils;

public class FXFileComparatorTest extends AbstractTestExecution {
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void verifyFileComparator() {
        FilesComparator application = show(FilesComparator.class);
        File[] listFiles = ResourceFXUtils.getUserFolder("Music").listFiles(File::isDirectory);
        int i = 0;
        Set<TableView> lookup = lookup(TableView.class);
        for (TableView<File> query : lookup) {
            application.addSongsToTable(query, listFiles[i++ % listFiles.length]);
            while (application.getProgress() < 1) {
                // DOES NOTHING
            }
        }
        while (application.getProgress() < 1) {
            // DOES NOTHING
        }
        List<Button> filter = lookup(Button.class).stream()
            .filter(e -> !e.getText().startsWith("File") && !e.getText().equals("X")).collect(Collectors.toList());
        filter.forEach(this::tryClickOn);
        for (TableView<?> tableView : lookup) {
            clickOn(from(tableView).lookup(Cell.class::isInstance).queryLabeled());
            for (int j = 0; j < 10; j++) {
                scroll(5, VerticalDirection.DOWN);
                Node queryAll = from(tableView).lookup(".vermelho").query();
                if (queryAll != null) {
                    tryClickOn(queryAll);
                    break;
                }
            }

        }
        filter.forEach(this::tryClickOn);
        WaitForAsyncUtils.waitForFxEvents();
    }
}
