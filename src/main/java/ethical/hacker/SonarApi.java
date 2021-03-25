package ethical.hacker;

import com.google.common.collect.ImmutableMap;
import extract.web.JsonExtractor;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.SupplierEx;

public class SonarApi extends Application {

    public static final String SONAR_API_ISSUES = ProjectProperties.getField();

    private static final Map<String,
            String> GET_HEADERS = ImmutableMap.<String, String>builder()
                    .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                    .put("Accept", "application/json").put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
                    .put("Accept-Encoding", "gzip, deflate")
                    .put("Referer", "http://localhost:9000/project/issues?id=Experiments%3AExperiments&resolved=false")
                    .put("DNT", "1").put("Connection", "keep-alive").build();
    @FXML
    private ListView<String> rulesList;
    @FXML
    private TableView<Map<String, Object>> sonarTable;
    @FXML
    private TextField filterField;
    @FXML
    private ListView<String> componentsList;
    private ObservableList<Map<String, Object>> issuesList = FXCollections.observableArrayList();
    private ObservableList<String> components = FXCollections.observableArrayList();
    private ObservableList<String> rules = FXCollections.observableArrayList();

    public void initialize() {
        SimpleTableViewBuilder.of(sonarTable).savable().copiable()
                .items(CommonsFX.newFastFilter(filterField, issuesList.filtered(s -> true)))
                .onSortClicked((s, b) -> QuickSortML.sortMapList(issuesList, s, b)).build();
        SimpleListViewBuilder.of(componentsList).items(components)
                .onDoubleClick(s -> filterField.setText(s.replaceAll("\t.+", ""))).build();
        SimpleListViewBuilder.of(rulesList).items(rules)
                .onDoubleClick(s -> filterField.setText(s.replaceAll("\t.+", ""))).build();
    }

    public void onActionOpenDataframe() throws IOException {
        openDataframe(filterField, sonarTable);
    }

    public void onActionUpdate() {
        List<String> keySet = onUpdate(issuesList, components, rules);
        SimpleTableViewBuilder.addColumns(sonarTable, keySet);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CommonsFX.loadFXML("Sonar API", "SonarApi.fxml", this, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static List<String> onUpdate(List<Map<String, Object>> issuesList) {
        return onUpdate(issuesList, new ArrayList<>(), new ArrayList<>());
    }
    public static List<String> onUpdate(List<Map<String, Object>> issuesList, List<String> components,
            List<String> rules) {
        Object sonarRequest = getFromURLJson(getApiUrl(1), ResourceFXUtils.getOutFile("json/sonarRequest.json"));
        List<Map<String, Object>> newJson = JsonExtractor.accessList(sonarRequest, "issues");
        issuesList.clear();
        issuesList.addAll(newJson);
        Integer total = JsonExtractor.access(sonarRequest, Integer.class, "total");
        for (int i = 1; i <= total / 100; i++) {
            Object fromURLJson =
                    getFromURLJson(getApiUrl(i + 1), ResourceFXUtils.getOutFile("json/sonarRequest" + i + ".json"));
            List<Map<String, Object>> newJson2 = JsonExtractor.accessList(fromURLJson, "issues");
            issuesList.addAll(newJson2);
        }
        for (Map<String, Object> map : issuesList) {
            Object compute =
                    map.computeIfPresent("component", (k, v) -> Objects.toString(v, "").replaceAll(".+java/", ""));
            components.add(Objects.toString(compute, ""));
        }
        List<String> resources = components.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparingLong(Entry<String, Long>::getValue).reversed())
                .map(e -> e.getKey() + "\t" + e.getValue()).collect(Collectors.toList());
        components.clear();
        components.addAll(resources);
        List<String> ruless = issuesList.stream()
                .collect(Collectors.groupingBy(s -> Objects.toString(s.get("rule")), Collectors.counting())).entrySet()
                .stream().sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
                .map(e -> e.getKey() + "\t" + e.getValue()).collect(Collectors.toList());
        rules.clear();
        rules.addAll(ruless);

        List<String> keySet = new ArrayList<>(newJson.get(0).keySet());
        keySet.removeAll(Arrays.asList("updateDate", "comments", "fromHotspot", "project", "effort", "creationDate",
                "transitions", "flows", "organization", "textRange", "actions", "debt", "hash", "key"));
        return keySet;

    }

    protected static Object getFromURLJson(String url, File outFile) {
        return SupplierEx.get(() -> {
            if (JsonExtractor.isNotRecentFile(outFile)) {
                Map<String, String> hashMap = new HashMap<>(GET_HEADERS);
                PhantomJSUtils.makeGet(url, hashMap, outFile);
            }
            return JsonExtractor.toFullObject(outFile);
        });
    }

    private static String getApiUrl(int p) {
        return SONAR_API_ISSUES + "&p=" + p;
    }

    private static void openDataframe(TextField filterField, TableView<Map<String, Object>> build) throws IOException {
        TableView<Map<String, Object>> table = build;
        File ev = ResourceFXUtils.getOutFile("csv/" + table.getId() + ".csv");
        CSVUtils.saveToFile(table, ev);
        new SimpleDialogBuilder().bindWindow(filterField).show(DataframeExplorer.class).addStats(ev);
    }
}