package kibana;

import extract.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.fx.RotateUtils;

public class ReportApplication extends Application {

    private static final Logger LOG = HasLogging.log();
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private WebView browser;
    @FXML
    private HBox paramsPane;
    @FXML
    private Text loc;
    @FXML
    private Slider zoom;
    @FXML
    private ComboBox<Path> model;

    private Map<String, String> params = new LinkedHashMap<>();
    private Map<String, Node> paramsNode = new LinkedHashMap<>();

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        CommonsFX.bindBidirectional(browser.zoomProperty(), zoom.valueProperty());
        WebEngine engine = browser.getEngine();
        Worker<Void> loadWorker = engine.getLoadWorker();
        engine.locationProperty().addListener((ob, old, val) -> loc.setText(StringUtils.abbreviate(val, 100)));
        CommonsFX.bind(loadWorker.progressProperty(), progressIndicator.progressProperty());
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
            addCommonParams();
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            String extension = ReportHelper.getExtension(replaceString);
            File reportFile = ResourceFXUtils.getOutFile(extension + "/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            addGeridInfo(mapaSubstituicao);
            ReportHelper.addParameters(mapaSubstituicao, params, browser, progressBar.progressProperty());
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            ReportHelper.finalizeReport(mapaSubstituicao, reportFile);
        });
    }

    public void makeReportConsultasEditImages() {
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", params, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
            addCommonParams();
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            String extension = ReportHelper.getExtension(replaceString);
            File reportFile = ResourceFXUtils.getOutFile(extension + "/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            addGeridInfo(mapaSubstituicao);
            ReportHelper.addParametersNotCrop(mapaSubstituicao, params, browser, progressBar.progressProperty());
            displayEditDialog(mapaSubstituicao, reportFile);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Report Application", "ReportApplication.fxml", this, primaryStage);
        primaryStage.setMaximized(true);
    }

    private void addCommonParams() {
        params.put("\\$gatheredDate", DateFormatUtils.currentTime("ddMMyyyy"));
        params.put("\\$dateInverted", DateFormatUtils.currentTime("yyyy-MM-dd"));
        params.put("\\$currentHour", DateFormatUtils.currentHour());
        params.put("\\$currentMonth", DateFormatUtils.currentTime("MMMM yyyy"));
        params.put("\\$date", DateFormatUtils.currentDate());

    }

    private void addGeridInfo(Map<String, Object> mapaSubstituicao) {
        if (mapaSubstituicao.containsKey("gerid")) {
            LOG.info("GETTING GERID CREDENTIALS ");
            String index = params.get("\\$index");
            Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(params.get("\\$ip"), index);
            params.put("\\$creds", makeKibanaSearch.keySet().stream().collect(Collectors.joining("\n")));
            List<Object> textAsImage =
                    makeKibanaSearch.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
            ReportHelper.mergeImage(mapaSubstituicao, textAsImage);
        }
        if (JsonExtractor.accessMap(mapaSubstituicao, "params").containsKey("ip")) {
            int days = (int) Math.ceil(Math.max(1., StringSigaUtils.toInteger(params.get("\\$hour")) / 24.));
            KibanaApi.kibanaFullScan(params.get("\\$ip"), days, progressIndicator.progressProperty())
                    .forEach((k, v) -> params.put("\\$" + k, v));
        }

    }

    private void displayEditDialog(Map<String, Object> mapaSubstituicao, File reportFile) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setPreserveRatio(true);
        List<String> imageUrls = mapaSubstituicao.values().stream().flatMap(ReportHelper::objectList)
                .map(o -> (String) ClassReflectionUtils.invoke(o, "impl_getUrl")).collect(Collectors.toList());

        SimpleListViewBuilder<String> urlsListView = new SimpleListViewBuilder<>();
        urlsListView.onSelect((old, val) -> RunnableEx.runIf(val, v -> imageView.setImage(new Image(v))))
                .cellFactory((String st) -> st.replaceAll(".+/", "")).multipleSelection().onKey(KeyCode.DELETE, () -> {
                    SimpleDialogBuilder.closeStage(imageView);
                    for (String s : urlsListView.selected()) {
                        Files.deleteIfExists(Paths.get(new URI(s)));
                    }
                    makeReportConsultasEditImages();
                }).items(imageUrls);
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.TRANSPARENT);
        StackPane stackPane = new StackPane(imageView, rectangle);
        rectangle.setManaged(false);
        imageView.setManaged(false);
        ListView<String> build = urlsListView.build();
        SplitPane pane = new SplitPane(build, stackPane);

        pane.getDividers().get(0).positionProperty().addListener((ob, old, val) -> imageView
                .setFitWidth((1 - val.doubleValue()) * 0.99 * imageView.getScene().getWidth()));

        RotateUtils.moveArea(stackPane, rectangle, imageView,
                img -> ReportHelper.onImageSelected(mapaSubstituicao, reportFile, build, img));
        CommonsFX.runInPlatform(() -> {
            new SimpleDialogBuilder().bindWindow(browser).title("Crop Images").node(pane).displayDialog();
            build.prefHeightProperty().bind(imageView.getScene().heightProperty());
        });
    }

    private Node getNode(String k, Object v) {
        return paramsNode.computeIfAbsent(k, key -> {
            if (v instanceof List) {
                List<?> v2 = (List<?>) v;
                return new SimpleComboBoxBuilder<>().items(v2.toArray())
                        .onSelect(s -> params.put("\\$" + k, s.toString())).select(0).build();
            }
            TextField textField = new TextField();
            textField.textProperty().addListener((ob, old, val) -> params.put("\\$" + k, val));
            return textField;
        });
    }

    private void onModelChange(Path path) throws IOException {
        paramsPane.getChildren().clear();
        Map<Object, Object> accessMap = JsonExtractor.accessMap(JsonExtractor.toObject(path.toFile()), "params");
        accessMap.forEach((k, v) -> {
            Node node = getNode(k.toString(), v);
            paramsPane.getChildren().add(SimpleVBoxBuilder.newVBox(StringSigaUtils.changeCase(k + ""), node));
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
