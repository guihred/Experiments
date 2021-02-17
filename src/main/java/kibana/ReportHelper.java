package kibana;

import static utils.StringSigaUtils.toDouble;

import extract.PPTService;
import extract.WordService;
import extract.web.JsonExtractor;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker.State;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.data.Question;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;
import utils.fx.RectBuilder;

public final class ReportHelper {
    private static final Logger LOG = HasLogging.log();

    private ReportHelper() {
    }

    public static void addParameters(Map<String, Object> mapaSubstituicao, Map<String, String> params, WebView browser,
            DoubleProperty progress) {
        List<String> keys = mapaSubstituicao.keySet().stream().collect(Collectors.toList());
        CommonsFX.update(progress, 0);
        for (String key : keys) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    List<?> list = (List<?>) v;
                    return list.stream().map(e -> remap(params, e, browser)).filter(Objects::nonNull)
                            .peek(o -> CommonsFX.addProgress(progress, 1. / keys.size() / list.size()))
                            .collect(Collectors.toList());
                }
                CommonsFX.addProgress(progress, 1. / keys.size());
                return replaceString(params, v);
            });
        }
        CommonsFX.update(progress, 1);
    }

    public static void addParametersNotCrop(Map<String, Object> mapaSubstituicao, Map<String, String> params,
            WebView browser, DoubleProperty progress) {
        List<String> keys = mapaSubstituicao.keySet().stream().collect(Collectors.toList());
        CommonsFX.update(progress, 0);
        for (String key : keys) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    List<?> list = (List<?>) v;
                    return list.stream().map(e -> remapUncroppred(params, browser, e)).filter(Objects::nonNull)
                            .peek(o -> CommonsFX.addProgress(progress, 1. / keys.size() / list.size()))
                            .collect(Collectors.toList());
                }
                CommonsFX.addProgress(progress, 1. / keys.size());
                return replaceString(params, v);
            });
        }
        CommonsFX.update(progress, 1);
    }

    public static void finalizeReport(Map<String, Object> mapaSubstituicao, File reportFile) {
        String modelUsed = mapaSubstituicao.get("model").toString();
        String extension = ReportHelper.getExtension(reportFile.getName());
        if ("pptx".equals(extension)) {
            PPTService.getPowerPoint(mapaSubstituicao, modelUsed, reportFile);
        } else {
            WordService.getWord(mapaSubstituicao, modelUsed, reportFile);
        }
        ImageFXUtils.openInDesktop(reportFile);
    }

    public static String getExtension(String replaceString) {
        return replaceString.replaceAll(".+\\.(\\w+)$", "$1");
    }

    public static boolean isLoading(WebEngine engine) {
        return engine.getLoadWorker().getState() == State.RUNNING;
    }

    public static void loadSite(WebEngine engine2, String url) {
        RunnableEx.ignore(() -> {
            if (!Objects.equals(engine2.getLocation(), url)) {
                engine2.load(url);
            }
            LOG.info("LOADED {}", url);
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void mergeImage(Map<String, Object> mapaSubstituicao, List<Object> collect) {
        mapaSubstituicao.merge("gerid", collect, (o, n) -> {
            ((List<?>) o).addAll((List) n);
            return o;
        });
    }

    public static Stream<Image> objectList(Object e) {
        if (!(e instanceof Collection)) {
            return Stream.empty();
        }
        return ((Collection<?>) e).stream().filter(o -> o instanceof Image).map(Image.class::cast);
    }

    @SuppressWarnings({ "unchecked" })
    public static void onImageSelected(Map<String, Object> mapaSubstituicao, File reportFile, ListView<String> build,
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
            SimpleDialogBuilder.closeStage(build);
            LOG.info("APPLYING MAP {}", mapaSubstituicao);
            RunnableEx.runNewThread(() -> finalizeReport(mapaSubstituicao, reportFile));
        }
    }

    public static String replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public static File saveHtmlImage(WebView browser2, File out) {
        LOG.info("SAVING to {}", out.getName());
        return SupplierEx.get(() -> ImageFXUtils.take(browser2, out));
    }

    public static Image textToImage(String s) {
        String collect2 = Stream.of(s.split("\n")).filter(StringUtils::isNotBlank)
                .map(str -> "<p>" + str.replaceAll("(\\d+\\.\\d+\\.\\d+\\.\\d+|\\d{11})", "<font>$1</font>") + "</p>")
                .collect(Collectors.joining("\n"));
        String format =
                String.format("<!DOCTYPE html>\n<html>\n<head>\n<style>\nfont {background-color: yellow;}</style>\n"
                        + "</head><body>%s</body>\n</html>", collect2);
        return PhantomJSUtils.saveHtmlImage(format);
    }

    private static WritableImage crop(Map<String, Object> info, File outFile) {
        String externalForm = ResourceFXUtils.convertToURL(outFile).toExternalForm();
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
        return destImage;
    }

    private static Object getCSV(Map<String, Object> imageObj, Map<String, String> params) {
        String extension = "xlsx";
        File outFile = ResourceFXUtils.getOutFile(
                extension + "/" + replaceString(params, imageObj.getOrDefault("name", "erro")) + "." + extension);
        if (outFile.exists()) {
            return outFile;
        }
        CommonsFX.runInPlatformSync(() -> {
            new FileChooserBuilder().title(replaceString(params, imageObj.getOrDefault("name", "erro")))
                    .onSelect(f -> saveCSV(f, imageObj, outFile)).extensions("Data", "*.csv", "*.xlsx", "*.xls")
                    .openFileAction(null);
            return;
        });
        return outFile;
    }

    private static Image getImage(Map<String, Object> imageObj, WebView browser, Map<String, String> params) {
        File outFile = ResourceFXUtils
                .getOutFile("print/" + replaceString(params, imageObj.getOrDefault("name", "erro")) + ".png");
        if (!JsonExtractor.isNotRecentFile(outFile)) {
            return crop(imageObj, outFile);
        }

        WebEngine engine = browser.getEngine();
        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");

        String finalURL = replaceString(params, kibanaURL);
        CommonsFX.runInPlatform(() -> {
            if (imageObj.containsKey("zoom")) {
                Double zoom = StringSigaUtils.toDouble(imageObj.getOrDefault("zoom", 1));
                browser.zoomProperty().set(zoom);
            }
            loadSite(engine, finalURL);
        });
        RunnableEx.measureTime("Load Site " + replaceString(params, imageObj.get("name")), () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                RunnableEx.sleepSeconds(6);
                CommonsFX.runInPlatform(() -> atomicBoolean.set(isLoading(engine)));
                RunnableEx.sleepSeconds(6);
            }
            image.setValue(saveImage(imageObj, outFile, browser));
        });
        return image.getValue();
    }

    private static Image getUncroppedImage(Map<String, Object> imageObj, WebView browser, Map<String, String> params) {
        File outFile = ResourceFXUtils
                .getOutFile("print/" + replaceString(params, imageObj.getOrDefault("name", "erro")) + ".png");
        if (JsonExtractor.isRecentFile(outFile, 6)) {
            String externalForm = ResourceFXUtils.convertToURL(outFile).toExternalForm();
            return new Image(externalForm);
        }

        WebEngine engine = browser.getEngine();
        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");
        String finalURL = replaceString(params, kibanaURL);
        CommonsFX.runInPlatform(() -> loadSite(engine, finalURL));
        RunnableEx.measureTime("Load Site " + imageObj.get("name"), () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                RunnableEx.sleepSeconds(6);
                CommonsFX.runInPlatform(() -> atomicBoolean.set(isLoading(engine)));
                RunnableEx.sleepSeconds(6);
            }
            CommonsFX.runInPlatformSync(() -> saveHtmlImage(browser, outFile));
            String externalForm = ResourceFXUtils.convertToURL(outFile).toExternalForm();
            image.setValue(new Image(externalForm));
        });
        return image.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Object remap(Map<String, String> params, Object e, WebView browser) {
        if (e instanceof Map) {
            Map<String, Object> e2 = (Map<String, Object>) e;
            if (e2.containsKey("url")) {
                return getImage(e2, browser, params);
            }

            return getCSV(e2, params);
        }
        if (e instanceof Image) {
            return e;
        }
        return replaceString(params, e);
    }

    @SuppressWarnings("unchecked")
    private static Object remapUncroppred(Map<String, String> params, WebView browser, Object e) {
        if (e instanceof Map) {
            Map<String, Object> e2 = (Map<String, Object>) e;
            if (e2.containsKey("url")) {
                return getUncroppedImage(e2, browser, params);
            }
            return getCSV(e2, params);
        }
        if (e instanceof Image) {
            return e;
        }
        return replaceString(params, e);
    }

    private static void saveCSV(File srcFile, Map<String, Object> params, File outFile) {
        DataframeML dataframe = DataframeBuilder.build(srcFile);
        if (params.containsKey("mappings")) {
            Map<String, String> mapping = JsonExtractor.accessMap(params, "mappings");
            List<String> cols2 = dataframe.cols();
            mapping.forEach((k, v) -> {
                if (cols2.contains(k)) {
                    dataframe.map(v, k, m -> m);
                }
            });
        }
        List<String> cols = dataframe.cols();
        String columns = params.getOrDefault("columns", "").toString();
        cols.removeIf(s -> s.matches(columns));
        dataframe.removeCol(cols.toArray(new String[0]));
        if (params.containsKey("sort")) {
            Object object = params.get("sort");
            DataframeUtils.sort(dataframe, Objects.toString(object));
        }
        List<String> o = JsonExtractor.accessList(params, "questions");
        List<Question> a = o.stream().map(t -> Question.parseQuestion(dataframe, t)).collect(Collectors.toList());
        for (Question question : a) {
            dataframe.filter(question.getColName(), question);
        }
        List<String> finalHeaders = Stream.of(columns.split("\\|")).collect(Collectors.toList());
        dataframe.sortHeaders(finalHeaders);
        List<Map<String, Object>> items2 = new ArrayList<>();
        dataframe.forEachRow(items2::add);
        if (items2.isEmpty()) {
            Map<String, Object> e = new LinkedHashMap<>();
            finalHeaders.forEach(c -> e.put(c, ""));
            items2.add(e);
        }
        ExcelService.getExcel(items2, outFile);

    }

    private static WritableImage saveImage(Map<String, Object> info, File outFile, WebView browser2) {
        CommonsFX.runInPlatformSync(() -> saveHtmlImage(browser2, outFile));
        return crop(info, outFile);
    }

}
