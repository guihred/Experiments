package ethical.hacker;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import utils.HasLogging;

public final class EthicalHackApp {

    private static final Logger LOG = HasLogging.log();

    private EthicalHackApp() {
    }

    public static void addColumns(final TableView<Map<String, String>> simpleTableViewBuilder,
        final Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    public static CheckBox getCheckBox(List<Integer> checkedPorts, Map<Integer, CheckBox> checkbox,
            Entry<Integer, String> e) {
        return checkbox.computeIfAbsent(e.getKey(), i -> newCheck(checkedPorts, e));
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
        LOG.trace("{} of {} = {}", targetKey, key, valueAdded);

    }

    public static MapChangeListener<String, List<String>> updateItemOnChange(String primaryKey, String targetKey,
        ObservableMap<String, Set<String>> count1, ObservableList<Map<String, String>> items) {
        count1.clear();
        return change -> updateItem(items, count1, primaryKey, targetKey, change);
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
