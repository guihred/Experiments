package kibana;

import extract.web.JsonExtractor;
import extract.web.JsoupUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
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
import ml.data.Mapping;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.fx.AutocompleteField;
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
    private Text zoomText;
    @FXML
    private Slider zoom;
    @FXML
    private ComboBox<Path> model;

    private Map<String, String> params = new LinkedHashMap<>();
    private Map<String, Node> paramsNode = new LinkedHashMap<>();

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        CommonsFX.bindBidirectional(browser.zoomProperty(), zoom.valueProperty());
        zoomText.textProperty().bind(Bindings.createStringBinding(
                () -> String.format(Locale.ENGLISH, "%s (%.2f)", "Zoom", browser.getZoom()), browser.zoomProperty()));

        WebEngine engine = browser.getEngine();
        engine.setUserAgent(JsoupUtils.USER_AGENT + "\nAuthorization: Basic " + ExtractUtils.getEncodedAuthorization());
        Worker<Void> loadWorker = engine.getLoadWorker();
        engine.locationProperty().addListener((ob, old, val) -> loc.setText(StringUtils.abbreviate(val, 100)));
        CommonsFX.bind(loadWorker.progressProperty(), progressIndicator.progressProperty());
        File parentFile = ResourceFXUtils.toFile("kibana/modeloRelatorio.json").getParentFile();
        List<Path> firstFileMatch = FileTreeWalker
                .getFirstFileMatch(parentFile, p -> p.getFileName().toString().startsWith("modeloRelatorio")).stream()
                .distinct().collect(Collectors.toList());
        SimpleComboBoxBuilder.of(model).converter(p -> p.getFileName().toString()).items(firstFileMatch)
                .onSelect(this::onModelChange).select(0);
        RunnableEx.runNewThread(Mapping::getMethods);
    }

    public void makeReportConsultas() {
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", params, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = getReplacementMap(modelFile);
            File reportFile = ReportHelper.reportName(mapaSubstituicao, params);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            ReportHelper.addParameters(mapaSubstituicao, params, browser, progressBar.progressProperty());
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            ReportHelper.finalizeReport(mapaSubstituicao, reportFile);
        });
    }

    public void makeReportConsultasEditImages() {
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", params, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = getReplacementMap(modelFile);
            File reportFile = ReportHelper.reportName(mapaSubstituicao, params);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            ReportHelper.addParametersNotCrop(mapaSubstituicao, params, browser, progressBar.progressProperty());
            displayEditDialog(mapaSubstituicao, reportFile);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Report Application", "ReportApplication.fxml", this, primaryStage);
        primaryStage.setMaximized(true);
        CommonsFX.onCloseWindow(primaryStage, ReportHelper::quit);
    }

    private void addCommonParams() {
        params.put("\\$gatheredDate", DateFormatUtils.currentTime("ddMMyyyy"));
        params.put("\\$dateInverted", DateFormatUtils.currentTime("yyyy-MM-dd"));
        params.put("\\$currentHour", DateFormatUtils.currentHour());
        params.put("\\$currentMonth", DateFormatUtils.currentTime("MMMM yyyy"));
        params.put("\\$date", DateFormatUtils.currentDate());

    }

    private void addGeridInfo(Map<String, Object> mapaSubstituicao) {

        final double hoursInADay = 24.;
        int days = (int) Math.max(1., Math.ceil(StringSigaUtils.toInteger(params.get("\\$hour")) / hoursInADay));
        String ipParam = params.get("\\$ip");
        if (JsonExtractor.accessMap(mapaSubstituicao, "params").containsKey("ip")) {
            params.putAll(ReportHelper.adjustParams(ipParam, days, progressIndicator.progressProperty()));

        }
        if (mapaSubstituicao.containsKey("gerid")) {
            String index = params.get("\\$index");
            params.putAll(ReportHelper.adjustParams(mapaSubstituicao, days, ipParam, index,
                    progressIndicator.progressProperty()));
        }

    }

    private void displayEditDialog(Map<String, Object> mapaSubstituicao, File reportFile) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setPreserveRatio(true);
        List<String> imageUrls = mapaSubstituicao.values().stream().flatMap(ReportHelper::objectList)
                .map(o -> (String) ClassReflectionUtils.invoke(o, "impl_getUrl")).distinct()
                .collect(Collectors.toList());

        SimpleListViewBuilder<String> urlsListView = new SimpleListViewBuilder<>();
        urlsListView.onSelect((old, val) -> RunnableEx.runIf(val, v -> imageView.setImage(new Image(v))))
                .cellFactory((String st) -> st.replaceAll(".+/", "")).multipleSelection().onKey(KeyCode.DELETE, () -> {
                    SimpleDialogBuilder.closeStage(imageView);
                    for (String s : urlsListView.selected()) {
                        Files.deleteIfExists(Paths.get(new URI(s)));
                    }
                    makeReportConsultasEditImages();
                }).onKey(KeyCode.SUBTRACT, t -> {
                    removeImage(mapaSubstituicao, imageUrls, t);
                    if (imageUrls.isEmpty()) {
                        SimpleDialogBuilder.closeStage(imageView);
                        LOG.info("APPLYING MAP {}", mapaSubstituicao);
                        RunnableEx.runNewThread(() -> ReportHelper.finalizeReport(mapaSubstituicao, reportFile));
                    }
                }).items(imageUrls);
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.TRANSPARENT);
        StackPane stackPane = new StackPane(imageView, rectangle);
        rectangle.setManaged(false);
        imageView.setManaged(false);
        ListView<String> build = urlsListView.build();
        SplitPane pane = new SplitPane(build, stackPane);

        final double fitRatio = 0.99;
        pane.getDividers().get(0).positionProperty().addListener((ob, old, val) -> imageView
                .setFitWidth((1 - val.doubleValue()) * fitRatio * imageView.getScene().getWidth()));

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
                if (v2.contains("")) {
                    AutocompleteField textField = new AutocompleteField();
                    textField.setEntries(v2.stream().map(Objects::toString).collect(Collectors.toList()));
                    textField.textProperty().addListener((ob, old, val) -> params.put("\\$" + k, val));
                    textField.setText(v2.stream().findFirst().map(Objects::toString).orElse(""));
                    return textField;
                }

                return new SimpleComboBoxBuilder<>().items(v2.toArray())
                        .onSelect(s -> params.put("\\$" + k, s.toString())).select(0).build();
            }
            if (v instanceof Boolean) {
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().addListener((ob, old, val) -> params.put("\\$" + k, val + ""));
                checkBox.setSelected((Boolean) v);
                return checkBox;
            }
            TextField textField = new TextField();
            textField.textProperty().addListener((ob, old, val) -> params.put("\\$" + k, val));
            return textField;
        });
    }

    private Map<String, Object> getReplacementMap(File modelFile) throws IOException {
        Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
        addCommonParams();
        addGeridInfo(mapaSubstituicao);
        return mapaSubstituicao;
    }

    private void onModelChange(Path path) throws IOException {
        paramsPane.getChildren().clear();
        Map<Object, Object> accessMap = JsonExtractor.accessMap(JsonExtractor.toFullObject(path.toFile()), "params");
        accessMap.forEach((k, v) -> {
            Node node = getNode(k.toString(), v);
            paramsPane.getChildren().add(SimpleVBoxBuilder.newVBox(StringSigaUtils.changeCase(k + ""), node));
        });
    }


    public static void main(String[] args) {
        launch(args);
    }

    private static void removeImage(Map<String, Object> mapaSubstituicao, List<String> imageUrls, String t) {
        imageUrls.remove(t);
        for (Object e : mapaSubstituicao.values()) {
            if (e instanceof List) {
                JsonExtractor.<Object>accessList(e).stream().filter(
                        o -> o instanceof Image && Objects.equals(t, ClassReflectionUtils.invoke(o, "impl_getUrl")))
                        .findFirst().ifPresent(o -> {
                            int indexOf = JsonExtractor.<Object>accessList(e).indexOf(o);
                            JsonExtractor.<Object>accessList(e).remove(indexOf);
                        });
            }
        }
    }

}
