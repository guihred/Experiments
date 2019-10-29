package extract;

import static java.util.Comparator.comparing;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableView;

public final class FilesComparatorHelper {
	private static final List<String> STYLE_CLASSES = Arrays.asList("", "vermelho", "amarelo");
    private FilesComparatorHelper() {
    }

    public static Music getFromMap(File m, Map<File, Music> fileMap) {
        return fileMap.computeIfAbsent(m, MusicReader::readTags);
    }

    public static String getItemClass(ObservableList<File> items2, File s, ObservableList<String> classes,
        Map<File, Music> fileMap) {
        String fileString = toFileString(s);
		classes.removeAll(STYLE_CLASSES);
        File find = find(items2, fileString);
        if (find == null) {
            return "vermelho";
        }
        if (isEqualSong(s, find, fileMap)) {
            return "";
        }
        return "amarelo";

    }

    public static boolean isEqualSong(File s, File m, Map<File, Music> fileMap) {
        return Objects.equals(getFromMap(s, fileMap), getFromMap(m, fileMap));
    }

    public static boolean notRepeated(ObservableList<File> items2, File s) {
        String fileString = toFileString(s);
        return !items2.stream().anyMatch(m -> toFileString(m).equals(fileString));
    }

    public static String toFileString(File s) {
        return s.getParentFile().getName() + "/" + s.getName();
    }

    @SuppressWarnings("unchecked")
    public static void updateCells(TableView<File> table1) {
        Platform.runLater(() -> {
            Parent root = table1.getScene().getRoot();
            for (Node cell : root.lookupAll(".cell")) {
				cell.getStyleClass().removeAll(STYLE_CLASSES);
            }
            File selectedItem = table1.getSelectionModel().getSelectedItem();
            String fileString = selectedItem == null ? "" : toFileString(selectedItem);
            for (Node cell : root.lookupAll(".table-view")) {
                TableView<File> tables = (TableView<File>) cell;
                File find = find(tables.getItems(), fileString);
                if (find != null) {
                    tables.getSelectionModel().select(find);
                }
                int selectedIndex = tables.getSelectionModel().getSelectedIndex();
                tables.getItems().sort(comparing(FilesComparatorHelper::toFileString));
                tables.scrollTo(0);
                tables.scrollTo(selectedIndex);
            }
        });
    }

    private static File find(ObservableList<File> items2, String fileString) {
        for (int i = 0; i < items2.size(); i++) {
            File file = items2.get(i);
            if (toFileString(file).equals(fileString)) {
                return file;
            }
        }
        return null;
    }
}
