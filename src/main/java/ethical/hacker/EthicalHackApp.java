package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public abstract class EthicalHackApp extends Application {
    protected static final int WIDTH = 600;
    @FXML
    protected TextField resultsFilter;
    @FXML
    protected TextField dns;
    @FXML
    protected TableView<Entry<Integer, String>> servicesTable;
    @FXML
    protected TextField address;
    @FXML
    protected TextField networkAddress;
    @FXML
    protected TextField filterField;
    @FXML
    protected TableColumn<Entry<Integer, String>, Object> portColumn;
    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected Text ports;
    @FXML
    protected TableView<Map<String, String>> commonTable;
    protected ObservableList<Integer> portsSelected = observableArrayList();
    protected ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());
    protected ObservableMap<String, Set<String>> count = observableHashMap();
    @FXML
    protected HBox parent;

    protected EthicalHackApp() {
    }

    public static void addColumns(final TableView<Map<String, String>> simpleTableViewBuilder,
        final Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setSortable(true);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(param.getValue().getOrDefault(key, "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }


    public static synchronized void updateItem(ObservableList<Map<String, String>> items,
        ObservableMap<String, Set<String>> count1, String primaryKey, String targetKey,
        Change<? extends String, ? extends List<String>> change) {
        if (!change.wasAdded()) {
            return;
        }
        String key = change.getKey();
        List<String> valueAdded = change.getValueAdded();
        Map<String, String> e2 = items.stream().filter(c -> key.equals(c.get(primaryKey))).findAny()
            .orElseGet(ConcurrentHashMap::new);
        e2.put(primaryKey, key);
        e2.put(targetKey, valueAdded.stream().collect(Collectors.joining("\n")));
        if (!items.contains(e2)) {
            items.add(e2);
        }
        items.add(0, items.remove(0));
        Set<String> orDefault = count1.getOrDefault(primaryKey, new HashSet<>());
        orDefault.add(key);
        count1.put(targetKey, orDefault);
    }

    public static MapChangeListener<String, List<String>> updateItemOnChange(String primaryKey, String targetKey,
        ObservableMap<String, Set<String>> count1, ObservableList<Map<String, String>> items) {
        count1.clear();
        return change -> updateItem(items, count1, primaryKey, targetKey, change);
    }

    protected static void addIfChecked(List<Integer> list, Entry<Integer, String> e, Boolean val) {
        if (!val) {
            list.remove(e.getKey());
        } else if (!list.contains(e.getKey())) {
            list.add(e.getKey());
        }
    }

}
