package election;

import static simplebuilder.SimpleTableViewBuilder.equalColumns;
import static simplebuilder.SimpleTableViewBuilder.setFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import ml.graph.PieGraph;
import org.slf4j.Logger;
import utils.DateFormatUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.fx.ImageTableCell;

public final class CandidatoHelper {
    private static final int RELEVANT_FIELD_THRESHOLD = 410;
    private static CandidatoDAO candidatoDAO = new CandidatoDAO();

    private static final Logger LOG = HasLogging.log();

    private CandidatoHelper() {
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
        text18.textProperty()
                .bind(Bindings.createStringBinding(() -> fieldMap.entrySet().stream()
                        .filter(e -> !e.getValue().isEmpty()).map(Objects::toString).collect(Collectors.joining(",")),
                        fieldMap));
    }

    public static void configTable(TableColumn<Candidato, String> fotoUrl, TableColumn<Candidato, Cidade> cidade,
            TableColumn<Candidato, Boolean> eleito, TableColumn<Candidato, LocalDate> nascimento,
            TableView<Candidato> tableView2) {
        fotoUrl.setCellFactory(ImageTableCell::new);
        cidade.setCellFactory(setFormat(Cidade::getCity));
        eleito.setCellFactory(setFormat(StringSigaUtils::simNao));
        nascimento.setCellFactory(setFormat(DateFormatUtils::formatDate));
        equalColumns(tableView2);
    }

    public static List<String> distinct(String field) {
        return candidatoDAO.distinct(field);
    }

    public static List<String> getRelevantFields() {
        return candidatoDAO.distinctFields().entrySet().stream().filter(i -> i.getValue() < RELEVANT_FIELD_THRESHOLD)
                .map(Entry<String, Long>::getKey).collect(Collectors.toList());
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

    public static void updateTable(Pagination pagination, int maxResult, String column, PieGraph pieGraph,
            ObservableList<Candidato> observableArrayList, Map<String, Set<String>> fieldMap) {
        IntegerProperty first = pagination.currentPageIndexProperty();
        int max = maxResult == 0 ? 10 : maxResult;
        LOG.info("SCANNING CANDIDATES PAGE {} MAX {} COLUMN {} fields {}", pagination.getCurrentPageIndex(), maxResult,
                column, fieldMap);
        List<Candidato> list = candidatoDAO.list(first.get() * max, max, fieldMap);
        observableArrayList.setAll(list);
        Map<String, Long> histogram = candidatoDAO.histogram(column, fieldMap);
        pieGraph.setHistogram(histogram);
        pagination.setPageCount((int) histogram.values().stream().mapToLong(s -> s).sum() / max);
    }
}
