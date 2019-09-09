package election;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeView;
import ml.graph.PieGraph;
import simplebuilder.SimpleTreeViewBuilder;
import utils.ClassReflectionUtils;

public final class CandidatoHelper {
    private static final int RELEVANT_FIELD_THRESHOLD = 410;
    private static CandidatoDAO candidatoDAO = new CandidatoDAO();

    CandidatoHelper() {
    }

    public static void addIfChecked(String parent, Map<String, Set<String>> fieldMap, String value, Boolean val) {
        Set<String> set = fieldMap.remove(parent);
        if (!val) {
            set.remove(value);
        } else {
            set.add(value);
        }
        fieldMap.put(parent, set);
    }

    public static List<String> getRelevantFields() {
        return ClassReflectionUtils.getFields(Candidato.class).stream()
            .filter(e -> candidatoDAO.distinctNumber(e) < RELEVANT_FIELD_THRESHOLD).collect(Collectors.toList());
    }

    public static String simNao(Boolean a) {
        return a ? "Sim" : "Não";
    }

    public static TreeView<String> treeView(ObservableMap<String, Set<String>> fieldMap, IntegerProperty first,
        IntegerProperty maxResult, StringProperty column, PieGraph pieGraph, ObservableList<Object> candidates) {
        Map<String, CheckBox> portChecks = new HashMap<>();
        SimpleTreeViewBuilder<String> treeView = new SimpleTreeViewBuilder<String>().root("Root").editable(false)
            .showRoot(false).onSelect(newValue -> {
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
            });
        for (String field : getRelevantFields()) {
            List<String> distinct = candidatoDAO.distinct(field);
            fieldMap.put(field, FXCollections.observableSet());
            treeView.addItem(field, distinct);

        }

        fieldMap.addListener((MapChangeListener<String, Set<String>>) e -> updateTable(first, maxResult.get(),
            column.get(), pieGraph, candidates, fieldMap));

        return treeView.build();
    }

    public static void updateTable(IntegerProperty first, int maxResult, String column, PieGraph pieGraph,
        ObservableList<Object> observableArrayList, Map<String, Set<String>> fieldMap) {
        List<Candidato> list = candidatoDAO.list(first.get(), maxResult, fieldMap);
        observableArrayList.setAll(list);
        Map<String, Long> histogram = candidatoDAO.histogram(column, fieldMap);
        pieGraph.setHistogram(histogram);
    }
}
