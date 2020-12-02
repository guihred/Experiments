package ethical.hacker;

import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import kibana.KibanaApi;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class ReportApplication extends Application {

    private static final Logger LOG = HasLogging.log();
    @FXML
    private TextField ipField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private WebView browser;
    @FXML
    private Text loc;
    @FXML
    private ComboBox<Path> model;
    @FXML
    private ComboBox<Integer> hours;

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
        SimpleComboBoxBuilder.of(model).converter(p -> p.getFileName().toString()).items(firstFileMatch).select(0);
    }

    public void makeReportConsultas() {
        String finalIP = ipField.getText();
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", finalIP, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
            Map<String, String> params = new LinkedHashMap<>();
            params.put("\\$ip", finalIP);
            params.put("\\$date", DateFormatUtils.currentDate());
            params.put("\\$hour", "-" + hours.getValue() + "h");
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            File reportFile = ResourceFXUtils
                    .getOutFile("docx/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            if (mapaSubstituicao.containsKey("gerid")) {
                LOG.info("GETTING GERID CREDENTIALS ");
                Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(finalIP);
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

    public void setIp(String value) {
        CommonsFX.runInPlatform(() -> ipField.setText(value));
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Report Application", "ReportApplication.fxml", this, primaryStage);
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
