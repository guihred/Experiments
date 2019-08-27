package ethical.hacker;

import static schema.sngpc.FXMLCreatorHelper.loadFXML;
import static utils.ResourceFXUtils.toFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class EthicalHackApp extends Application {


    @Override
    public void start(final Stage primaryStage) throws Exception {
        loadFXML(toFile("EthicalHackApp.fxml"), "Ethical Hack App", primaryStage, 500, 500);
    }

    public static void main(final String[] args) {
        launch(args);
    }

    static void addColumns(final TableView<Map<String, String>> simpleTableViewBuilder,
        final Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    static CheckBox getCheckBox(List<Integer> checkedPorts, Map<Integer, CheckBox> checkbox,
        Entry<Integer, String> e) {
        return checkbox.computeIfAbsent(e.getKey(), i -> newCheck(checkedPorts, e));
    }

    private static void addIfChecked(List<Integer> list, Entry<Integer, String> e, Boolean val) {
        if (!val) {
            list.remove(e.getKey());
        } else if (!list.contains(e.getKey())) {
            list.add(e.getKey());
        }
    }

    private static CheckBox newCheck(List<Integer> arrayList, Entry<Integer, String> e) {
        CheckBox check = new CheckBox();
        check.selectedProperty().addListener((ob, o, val) -> addIfChecked(arrayList, e, val));
        return check;
    }

}
