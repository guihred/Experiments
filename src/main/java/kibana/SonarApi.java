package kibana;

import com.google.common.collect.ImmutableMap;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CSVUtils;
import utils.CommonsFX;
import utils.PhantomJSUtils;
import utils.ResourceFXUtils;

public class SonarApi extends Application {

    private static final String SONAR_API_ISSUES =
            "http://localhost:9000/api/issues/search?componentKeys=Experiments%3AExperiments" + "&s=FILE_LINE"
                    + "&resolved=false" + "&ps=100" + "&organization=default-organization"
                    + "&facets=severities%2Ctypes%2Crules" + "&additionalFields=_all";

    private static final Map<String,
            String> GET_HEADERS = ImmutableMap.<String, String>builder()
                    .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                    .put("Accept", "application/json").put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
                    .put("Accept-Encoding", "gzip, deflate")
                    .put("Referer", "http://localhost:9000/project/issues?id=Experiments%3AExperiments&resolved=false")
                    .put("DNT", "1").put("Connection", "keep-alive").build();

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextField filterField = new TextField();
        ObservableList<Map<String, Object>> issuesList = FXCollections.observableArrayList();
        TableView<Map<String, Object>> build = new SimpleTableViewBuilder<Map<String, Object>>().savable().copiable()
                .items(CommonsFX.newFastFilter(filterField, issuesList.filtered(s -> true))).build();
        HBox.setHgrow(build, Priority.ALWAYS);
        primaryStage.setTitle("Sonar API");
        primaryStage.setScene(new Scene(
                new HBox(new VBox(new Text("Filter"), filterField, SimpleButtonBuilder.newButton("Update", () -> {
                    Object sonarRequest =
                            getFromURLJson(getApiUrl(1), ResourceFXUtils.getOutFile("json/sonarRequest.json"));
                    List<Map<String, Object>> newJson =
                            JsonExtractor.accessList(sonarRequest, "issues");
                    issuesList.addAll(newJson);
                    Integer valueOf = Integer.valueOf(JsonExtractor.access(sonarRequest, String.class, "total"));
                    for (int i = 1; i <= valueOf / 100; i++) {
                        Object fromURLJson = getFromURLJson(getApiUrl(i + 1),
                                ResourceFXUtils.getOutFile("json/sonarRequest" + i + ".json"));
                        List<Map<String, Object>> newJson2 = JsonExtractor.accessList(fromURLJson, "issues");
                        issuesList.addAll(newJson2);
                    }

                    SimpleTableViewBuilder.addColumns(build, newJson.get(0).keySet());
                }), SimpleButtonBuilder.newButton("Open Dataframe", () -> {
                    TableView<Map<String, Object>> table = build;
                    File ev = ResourceFXUtils.getOutFile("csv/" + table.getId() + ".csv");
                    CSVUtils.saveToFile(table, ev);
                    new SimpleDialogBuilder().bindWindow(filterField).show(DataframeExplorer.class).addStats(ev);
                })), build)));
        primaryStage.show();
    }

    private String getApiUrl(int p) {
        return SONAR_API_ISSUES + "&p=" + p;
    }

    public static void main(String[] args) {
        launch(args);
    }

    protected static Object getFromURLJson(String url, File outFile) throws IOException {
        Map<String, String> hashMap = new HashMap<>(GET_HEADERS);
        PhantomJSUtils.makeGet(url, hashMap, outFile);
        return JsonExtractor.toObject(outFile);
    }
}