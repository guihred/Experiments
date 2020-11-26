package ethical.hacker;

import static utils.StringSigaUtils.toDouble;

import extract.PhantomJSUtils;
import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import kibana.KibanaApi;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import paintexp.tool.RectBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class ReportApplication extends Application {
    private static final String IP_REPLACEMENT = "191.96" + ".73.211";
    private static final Logger LOG = HasLogging.log();
    @FXML
    private TextField ipField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private WebView browser;
    @FXML
    private ComboBox<Path> model;
    private WebEngine engine;

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        engine = browser.getEngine();
        Worker<Void> loadWorker = engine.getLoadWorker();
        progressIndicator.progressProperty().bind(loadWorker.progressProperty());
        File parentFile = ResourceFXUtils.toFile("kibana/modeloRelatorio.json").getParentFile();
        List<Path> firstFileMatch = FileTreeWalker
                .getFirstFileMatch(parentFile, p -> p.getFileName().toString().startsWith("modeloRelatorio")).stream()
                .distinct().collect(Collectors.toList());
        SimpleComboBoxBuilder.of(model).converter(p -> p.getFileName().toString()).items(firstFileMatch).select(0);
    }

    public boolean isLoading() {
        return engine.getLoadWorker().getState() == State.RUNNING;
    }

    public void loadSite(String url) {
        RunnableEx.ignore(() -> {
            engine.load(url);
            LOG.info("LOADED {}", url);
        });
    }

    public void makeReportConsultas() {
        String finalIP = ipField.getText();
        File modelFile = model.getSelectionModel().getSelectedItem().toFile();
        LOG.info("MAKING REPORT {} {}", finalIP, modelFile.getName());
        RunnableEx.runNewThread(() -> {
            Map<String, Object> mapaSubstituicao = JsonExtractor.accessMap(JsonExtractor.toObject(modelFile));
            File reportFile = ResourceFXUtils.getOutFile("docx/" + mapaSubstituicao.get("name") + finalIP + ".docx");
            LOG.info("OUTPUT REPORT {} ", reportFile.getName());
            Map<String, String> params = new LinkedHashMap<>();
            params.put("\\$ip", finalIP);
            params.put("\\$date", DateFormatUtils.currentDate());
            boolean containsGerid = mapaSubstituicao.containsKey("gerid");
            if (containsGerid) {
                LOG.info("GETTING GERID CREDENTIALS ");
                Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(finalIP);
                params.put("\\$creds", makeKibanaSearch.keySet().stream().collect(Collectors.joining("\n")));
                List<Object> collect =
                        makeKibanaSearch.values().stream().map(this::textToImage).collect(Collectors.toList());
                mergeImage(mapaSubstituicao, collect);
            }
            addParameters(finalIP, mapaSubstituicao, params);
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            WordService.getWord(mapaSubstituicao,
                    containsGerid ? "ModeloGeralReporteConsultas.docx" : "ModeloGeralReporte.docx", reportFile);
            ImageFXUtils.openInDesktop(reportFile);

        });
    }

    public File saveHtmlImage() {
        return SupplierEx.get(() -> {
            Bounds bounds = browser.getBoundsInLocal();
            return ImageFXUtils.take(browser, bounds.getWidth(), bounds.getHeight());
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

    private void addParameters(String finalIP, Map<String, Object> mapaSubstituicao, Map<String, String> params) {
        for (String key : mapaSubstituicao.keySet().stream().collect(Collectors.toList())) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    return ((List<?>) v).stream().map(e -> remap(finalIP, params, e)).filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                return replaceString(params, v);
            });
        }
    }

    private Image getImage(String finalIP, Map<String, Object> imageObj) {
        File outFile = ResourceFXUtils.getOutFile("print/" + imageObj.getOrDefault("name", "erro") + finalIP + ".png");
        if (outFile.exists()) {
            return new Image(ResourceFXUtils.convertToURL(outFile).toExternalForm());
        }

        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");
        String replaceAll = kibanaURL.replaceAll(IP_REPLACEMENT, finalIP);
        CommonsFX.runInPlatform(() -> loadSite(replaceAll));
        RunnableEx.measureTime("Load Site " + imageObj.get("name"), () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                RunnableEx.sleepSeconds(6);
                CommonsFX.runInPlatform(() -> atomicBoolean.set(isLoading()));
                RunnableEx.sleepSeconds(6);
            }
            image.setValue(saveImage(imageObj, outFile));
        });
        return image.getValue();
    }

    @SuppressWarnings("unchecked")
    private Object remap(String finalIP, Map<String, String> params, Object e) {
        if (e instanceof Map) {
            return getImage(finalIP, (Map<String, Object>) e);
        }
        if (e instanceof Image) {
            return e;
        }
        return replaceString(params, e);
    }

    private WritableImage saveImage(Map<String, Object> info, File outFile) throws IOException {
        File saveHtmlImage = CommonsFX.runInPlatformSync(this::saveHtmlImage);
        String externalForm = ResourceFXUtils.convertToURL(saveHtmlImage).toExternalForm();
        Image value = new Image(externalForm);
        double width = value.getWidth();
        double height = value.getHeight();
        double width2 = width * toDouble(info.get("width"));
        double height2 = height * toDouble(info.get("height"));
        Rectangle a = new Rectangle(width2, height2);
        a.setLayoutX(width * toDouble(info.get("x")));
        a.setLayoutY(height * toDouble(info.get("y")));
        WritableImage destImage = new WritableImage((int) a.getWidth(), (int) a.getHeight());
        RectBuilder.copyImagePart(value, destImage, a);
        ImageFXUtils.saveImage(destImage, outFile);
        return destImage;
    }

    private Image textToImage(String s) {
        String collect2 = Stream.of(s.split("\n")).filter(StringUtils::isNotBlank)
                .map(str -> "<p>" + str.replaceAll("(\\d+\\.\\d+\\.\\d+\\.\\d+)", "<font>$1</font>") + "</p>")
                .collect(Collectors.joining("\n"));
        String format =
                String.format("<!DOCTYPE html>\n<html>\n<head>\n<style>\nfont {background-color: yellow;}</style>\n"
                        + "</head><body>%s</body>\n</html>", collect2);
        return PhantomJSUtils.saveHtmlImage(format);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void mergeImage(Map<String, Object> mapaSubstituicao, List<Object> collect) {
        mapaSubstituicao.merge("gerid", collect, (o, n) -> {
            ((List<?>) o).addAll((List) n);
            return o;
        });
    }

    private static Object replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

}
