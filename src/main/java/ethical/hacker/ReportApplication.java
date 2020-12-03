package ethical.hacker;

import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import kibana.KibanaApi;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class ReportApplication extends Application {

    private static final Logger LOG = HasLogging.log();
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private WebView browser;
    @FXML
    private HBox paramsPane;
    @FXML
    private Text loc;
    @FXML
    private ComboBox<Path> model;

    private Map<String, String> params = new LinkedHashMap<>();

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        WebEngine engine = browser.getEngine();
        Worker<Void> loadWorker = engine.getLoadWorker();
        engine.locationProperty().addListener((ob, old, val) -> loc.setText(StringUtils.abbreviate(val, 80)));
        progressIndicator.progressProperty().bind(loadWorker.progressProperty());
        File parentFile = ResourceFXUtils.toFile("kibana/modeloRelatorio.json").getParentFile();
        List<Path> firstFileMatch = FileTreeWalker
                .getFirstFileMatch(parentFile, p -> p.getFileName().toString().startsWith("modeloRelatorio")).stream()
                .distinct().collect(Collectors.toList());
        SimpleComboBoxBuilder.of(model).converter(p -> p.getFileName().toString()).items(firstFileMatch)
                .onSelect(this::onModelChange).select(0);
    }

    public void makeReportConsultas() {
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", params, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
            params.put("\\$date", DateFormatUtils.currentDate());
            params.put("\\$currentHour", DateFormatUtils.currentHour());
            params.put("\\$currentMonth", DateFormatUtils.currentTime("MMMM yyyy"));
            params.put("\\$dateInverted", DateFormatUtils.currentTime("yyyy-MM-dd"));
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            File reportFile = ResourceFXUtils.getOutFile("docx/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            if (mapaSubstituicao.containsKey("gerid")) {
                LOG.info("GETTING GERID CREDENTIALS ");
                Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(params.get("\\$ip"));
                params.put("\\$creds", makeKibanaSearch.keySet().stream().collect(Collectors.joining("\n")));
                List<Object> collect =
                        makeKibanaSearch.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
                ReportHelper.mergeImage(mapaSubstituicao, collect);
            }
            ReportHelper.addParameters(mapaSubstituicao, params, browser);
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            WordService.getWord(mapaSubstituicao, mapaSubstituicao.get("model").toString(), reportFile);
            ImageFXUtils.openInDesktop(reportFile);

        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Report Application", "ReportApplication.fxml", this, primaryStage);
        primaryStage.setMaximized(true);
    }

    private Node getNode(String k, Object v) {
        if (v instanceof List) {
            List<?> v2 = (List<?>) v;
            return new SimpleComboBoxBuilder<>().items(v2.toArray())
                    .onSelect(s -> params.put("\\$" + k, s.toString()))
                    .build();
        }
        TextField textField = new TextField();
        textField.textProperty().addListener((ob, old, val) -> params.put("\\$" + k, val));
        return textField;
    }

    private void onModelChange(Path path) throws IOException {
        paramsPane.getChildren().clear();
        Map<Object, Object> accessMap = JsonExtractor.accessMap(JsonExtractor.toObject(path.toFile()), "params");
        accessMap.forEach((k, v) -> {
            Node node = getNode(k.toString(), v);
            VBox newVBox = SimpleVBoxBuilder.newVBox(StringSigaUtils.changeCase(k + ""), node);
            paramsPane.getChildren().add(newVBox);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
