package election;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Text;
import ml.graph.PieGraph;
import utils.ClassReflectionUtils;

public final class CandidatoHelper {
    private static final int RELEVANT_FIELD_THRESHOLD = 410;
    private static CandidatoDAO candidatoDAO = new CandidatoDAO();

    CandidatoHelper() {
    }

    public static void addIfChecked(String parent, Map<String, Set<String>> fieldMap, String value, Boolean val) {
        Set<String> set = fieldMap.remove(parent);
        if (set == null) {
            set = FXCollections.observableSet();
        }
        if (!val) {
            set.remove(value);
        } else {
            set.add(value);
        }
        fieldMap.put(parent, set);
    }

    public static void bindTextToMap(Text text18, ObservableMap<String, Set<String>> fieldMap) {
        text18.textProperty().bind(Bindings.createStringBinding(() -> fieldMap.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty()).map(Objects::toString).collect(Collectors.joining(",")), fieldMap));
    }


    public static List<String> distinct(String field) {
        return candidatoDAO.distinct(field);
    }

    public static List<String> getRelevantFields() {
        return ClassReflectionUtils.getFields(Candidato.class).stream()
				.filter(e -> !e.equals("cidade")).filter(e -> candidatoDAO.distinctNumber(e) < RELEVANT_FIELD_THRESHOLD)
				.collect(Collectors.toList());
    }
    public static void onChangeElement(ObservableMap<String, Set<String>> fieldMap, Map<String, CheckBox> portChecks,
        TreeItem<String> newValue) {
        if (newValue != null && newValue.isLeaf()) {
            String value = newValue.getValue();
            String parent = newValue.getParent().getValue();
            if (!portChecks.containsKey(value)) {
                portChecks.put(value, new CheckBox());
                portChecks.get(value).selectedProperty()
                    .addListener((ob, o, val) -> addIfChecked(parent, fieldMap, value, val));
            }
            newValue.setGraphic(portChecks.get(value));
        }
    }

    public static void updateTable(IntegerProperty first, int maxResult, String column, PieGraph pieGraph,
        ObservableList<Candidato> observableArrayList, Map<String, Set<String>> fieldMap) {
        List<Candidato> list = candidatoDAO.list(first.get(), maxResult == 0 ? 10 : maxResult, fieldMap);
        observableArrayList.setAll(list);
        Map<String, Long> histogram = candidatoDAO.histogram(column == null ? getRelevantFields().get(0) : column,
            fieldMap);
        pieGraph.setHistogram(histogram);
    }
}
