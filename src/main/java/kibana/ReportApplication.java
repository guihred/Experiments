package kibana;

import extract.JsonExtractor;
import extract.PPTService;
import extract.WordService;
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
import javafx.scene.layout.VBox;
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
            params.put("\\$gatheredDate", DateFormatUtils.currentTime("ddMMyyyy"));
            params.put("\\$dateInverted", DateFormatUtils.currentTime("yyyy-MM-dd"));
            params.put("\\$currentHour", DateFormatUtils.currentHour());
            params.put("\\$currentMonth", DateFormatUtils.currentTime("MMMM yyyy"));
            params.put("\\$date", DateFormatUtils.currentDate());
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            String extension = getExtension(replaceString);
            File reportFile = ResourceFXUtils.getOutFile(extension + "/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            addGeridInfo(mapaSubstituicao);
            ReportHelper.addParameters(mapaSubstituicao, params, browser, progressBar.progressProperty());
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            finalizeReport(mapaSubstituicao, reportFile);

        });
    }

    public void makeReportConsultasEditImages() {
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", params, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
            params.put("\\$gatheredDate", DateFormatUtils.currentTime("ddMMyyyy"));
            params.put("\\$dateInverted", DateFormatUtils.currentTime("yyyy-MM-dd"));
            params.put("\\$currentHour", DateFormatUtils.currentHour());
            params.put("\\$currentMonth", DateFormatUtils.currentTime("MMMM yyyy"));
            params.put("\\$date", DateFormatUtils.currentDate());
            String replaceString = ReportHelper.replaceString(params, mapaSubstituicao.get("name"));
            String extension = getExtension(replaceString);
            File reportFile = ResourceFXUtils.getOutFile(extension + "/" + replaceString);
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            addGeridInfo(mapaSubstituicao);
            ReportHelper.addParametersNotCrop(mapaSubstituicao, params, browser, progressBar.progressProperty());
            ImageView imageView = new ImageView();
            imageView.setFitWidth(500);
            imageView.setPreserveRatio(true);
            List<String> collect = mapaSubstituicao.values().stream().flatMap(ReportApplication::objectList)
                    .map(o -> (String) ClassReflectionUtils.invoke(o, "impl_getUrl")).collect(Collectors.toList());

            ListView<String> build = new SimpleListViewBuilder<String>()
                    .onSelect((old, val) -> RunnableEx.runIf(val, v -> imageView.setImage(new Image(val))))
                    .cellFactory((String st) -> st.replaceAll(".+/", ""))
                    .onKey(KeyCode.DELETE, s -> {
                        Path path = Paths.get(new URI(s));
                        Files.deleteIfExists(path);
                        SimpleDialogBuilder.closeStage(imageView);
                        makeReportConsultasEditImages();
                    })
                    .items(collect).build();
            Rectangle rectangle = new Rectangle();
            rectangle.setStroke(Color.TRANSPARENT);
            rectangle.setFill(Color.TRANSPARENT);
            StackPane stackPane = new StackPane(imageView, rectangle);
            rectangle.setManaged(false);
            imageView.setManaged(false);
            SplitPane pane = new SplitPane(build, stackPane);

            pane.getDividers().get(0).positionProperty().addListener(
                    (ob, old, val) -> imageView
                            .setFitWidth((1 - val.doubleValue()) * 0.9 * imageView.getScene().getWidth()));

            RotateUtils.moveArea(stackPane, rectangle, imageView,
                    img -> onImageSelected(mapaSubstituicao, reportFile, build, img));
            CommonsFX.runInPlatform(() -> {
                new SimpleDialogBuilder().bindWindow(browser).title("Crop Images").node(pane).displayDialog();
                build.prefHeightProperty().bind(imageView.getScene().heightProperty());
            });
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Report Application", "ReportApplication.fxml", this, primaryStage);
        primaryStage.setMaximized(true);
    }

    private void addGeridInfo(Map<String, Object> mapaSubstituicao) {
        if (mapaSubstituicao.containsKey("gerid")) {
            LOG.info("GETTING GERID CREDENTIALS ");
            Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(params.get("\\$ip"));
            params.put("\\$creds", makeKibanaSearch.keySet().stream().collect(Collectors.joining("\n")));
            List<Object> collect =
                    makeKibanaSearch.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
            ReportHelper.mergeImage(mapaSubstituicao, collect);
        }
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
            VBox newVBox = SimpleVBoxBuilder.newVBox(StringSigaUtils.changeCase(k + ""), node);
            paramsPane.getChildren().add(newVBox);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void finalizeReport(Map<String, Object> mapaSubstituicao, File reportFile) {
        String modelUsed = mapaSubstituicao.get("model").toString();
        String extension = getExtension(reportFile.getName());
        if ("pptx".equals(extension)) {
            PPTService.getPowerPoint(mapaSubstituicao, modelUsed, reportFile);
        } else {
            WordService.getWord(mapaSubstituicao, modelUsed, reportFile);
        }
        ImageFXUtils.openInDesktop(reportFile);
    }

    private static String getExtension(String replaceString) {
        return replaceString.replaceAll(".+\\.(\\w+)$", "$1");
    }

    private static Stream<Image> objectList(Object e) {
        if (!(e instanceof Collection)) {
            return Stream.empty();
        }
        return ((Collection<?>) e).stream().filter(o -> o instanceof Image).map(Image.class::cast);
    }

    @SuppressWarnings({ "unchecked" })
    private static void onImageSelected(Map<String, Object> mapaSubstituicao, File reportFile, ListView<String> build,
            Image img) {
        String selectedItem = build.getSelectionModel().getSelectedItem();
        Collection<Object> values = mapaSubstituicao.values();
        for (Object e : values) {
            if (e instanceof List) {
                List<Object> collection = (List<Object>) e;
                collection.stream()
                        .filter(o -> o instanceof Image
                                && Objects.equals(selectedItem, ClassReflectionUtils.invoke(o, "impl_getUrl")))
                        .findFirst().ifPresent(o -> {
                            int indexOf = collection.indexOf(o);
                            collection.set(indexOf, img);
                            build.getItems().remove(selectedItem);
                        });
            }
        }
        if (build.getItems().isEmpty()) {
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            finalizeReport(mapaSubstituicao, reportFile);
            SimpleDialogBuilder.closeStage(build);
        }
    }

}
