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
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.PhantomJSUtils;
import utils.ResourceFXUtils;

public class SonarApi extends Application {

    // private static final Logger LOG = HasLogging.log();

    private static final Map<String, String> GET_HEADERS = ImmutableMap.<String, String>builder()

            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
            .put("Accept", "application/json").put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
            .put("Accept-Encoding", "gzip, deflate")
            .put("Referer", "http://localhost:9000/project/issues?id=Experiments%3AExperiments&resolved=false")
            .put("DNT", "1").put("Connection", "keep-alive").build();


    @Override
    public void start(Stage primaryStage) throws Exception {
        List<Map<String, Object>> fromURLJson = getFromURLJson(
                "http://localhost:9000/api/issues/search?componentKeys=Experiments%3AExperiments&s=FILE_LINE&resolved=false&ps=100&organization=default-organization&facets=severities%2Ctypes%2Crules&additionalFields=_all",
                ResourceFXUtils.getOutFile("json/sonarRequest.json"));
        TextField filterField = new TextField();
        TableView<Map<String, Object>> build = new SimpleTableViewBuilder<Map<String, Object>>().savable().copiable()
                .items(CommonsFX.newFastFilter(filterField,
                        FXCollections.observableArrayList(fromURLJson).filtered(s -> true)))
                .build();
        VBox.setVgrow(build, Priority.ALWAYS);
        SimpleTableViewBuilder.addColumns(build, fromURLJson.get(0).keySet());
        primaryStage.setTitle("Sonar API");
        primaryStage.setScene(new Scene(new VBox(filterField, build)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "unchecked" })
    protected static List<Map<String, Object>> getFromURLJson(String url, File outFile) throws IOException {
        Map<String, String> hashMap = new HashMap<>(GET_HEADERS);
        PhantomJSUtils.makeGet(url, hashMap, outFile);
        Object object = JsonExtractor.toObject(outFile);
        return (List<Map<String, Object>>) JsonExtractor.access(object, "issues");
    }
}