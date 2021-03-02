package kibana;

import extract.web.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
            Map<String, Object> mapaSubstituicao = getReplacementMap(modelFile);
            File reportFile = reportName(mapaSubstituicao);
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
            File reportFile = reportName(mapaSubstituicao);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
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

        final double hoursInADay = 24.;
        int days = (int) Math.max(1., Math.ceil(StringSigaUtils.toInteger(params.get("\\$hour")) / hoursInADay));
        String ipParam = params.get("\\$ip");
        if (JsonExtractor.accessMap(mapaSubstituicao, "params").containsKey("ip")) {
            for (String ip : ipParam.split("[, ]+")) {
                KibanaApi.kibanaFullScan(ip, days, progressIndicator.progressProperty())
                        .forEach((k, v) -> params.merge("\\$" + k, v, ReportApplication::mergeStrings));
            }
        }
        if (mapaSubstituicao.containsKey("gerid")) {
            params.remove("\\$orIps");
            params.remove("\\$otherIps");
            params.remove("\\$creds");
            Set<String> credentialText = new LinkedHashSet<>();
            for (String ipValue : ipParam.split("[, ]+")) {
                String index = params.get("\\$index");
                String authenticationSuccess = " AND \\\"AUTHENTICATION_SUCCESS\\\"";
                Map<String, String> credentialMap =
                        KibanaApi.getGeridCredencial(ipValue + authenticationSuccess, index, days);
                if (credentialMap.isEmpty()) {
                    credentialMap = KibanaApi.getGeridCredencial(ipValue, index, days);
                }
                LOG.info("GETTING GERID CREDENTIALS {} {}", ipValue, credentialMap.keySet());
                credentialText.addAll(credentialMap.values());
                List<String> collect = credentialMap.keySet().stream().collect(Collectors.toList());
                params.merge("\\$orIps", ipValue, (o, n) -> mergeStrings(o, n, " OR "));
                for (String credencial : collect) {
                    Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(
                            "\\\"" + credencial + "\\\"" + authenticationSuccess, index, days);
                    credentialText.addAll(iPsByCredencial.values());
                    String collect2 = iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
                    params.merge("\\$otherIps", collect2, ReportApplication::mergeStrings);
                    iPsByCredencial.keySet()
                            .forEach(i -> params.merge("\\$orIps", i, (o, n) -> mergeStrings(o, n, " OR ")));
                    LOG.info("GETTING GERID IP by CREDENTIALS {} {}", credencial, iPsByCredencial.keySet());
                }

                params.merge("\\$creds", credentialMap.keySet().stream().map(CredentialInvestigator::credentialInfo)
                        .collect(Collectors.joining("\n")),
                        ReportApplication::mergeStrings);
            }
            ReportHelper.mergeImage(mapaSubstituicao,
                    credentialText.stream().distinct().map(ReportHelper::textToImage).collect(Collectors.toList()));
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
        Map<Object, Object> accessMap = JsonExtractor.accessMap(JsonExtractor.toObject(path.toFile()), "params");
        accessMap.forEach((k, v) -> {
            Node node = getNode(k.toString(), v);
            paramsPane.getChildren().add(SimpleVBoxBuilder.newVBox(StringSigaUtils.changeCase(k + ""), node));
        });
    }

    private File reportName(Map<String, Object> mapaSubstituicao) {
        String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"))
                .replaceAll("\\.(inss|gov|br|prevnet|dataprev)|vip-p?", "");

        String extension = ReportHelper.getExtension(replaceString);
        return ResourceFXUtils.getOutFile(extension + "/" + replaceString);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String mergeStrings(String o, String n) {
        return mergeStrings(o, n, "\n");
    }

    private static String mergeStrings(String o, String n, String delimiter) {
        return Stream.of(o, n).flatMap(m -> Stream.of(m.split(delimiter))).distinct()
                .collect(Collectors.joining(delimiter));
    }

    private static void removeImage(Map<String, Object> mapaSubstituicao, List<String> imageUrls, String t) {
        imageUrls.remove(t);
        for (Object e : mapaSubstituicao.values()) {
            if (e instanceof List) {
                JsonExtractor
                        .<Object>accessList(e).stream().filter(o -> o instanceof Image && Objects
                                .equals(t, ClassReflectionUtils.invoke(o, "impl_getUrl")))
                        .findFirst().ifPresent(o -> {
                            int indexOf = JsonExtractor.<Object>accessList(e).indexOf(o);
                            JsonExtractor.<Object>accessList(e).remove(indexOf);
                        });
            }
        }
    }

}
