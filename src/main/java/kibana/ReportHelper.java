package kibana;

import static utils.StringSigaUtils.toDouble;

import extract.PPTService;
import extract.WordService;
import extract.web.JsonExtractor;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.lang.reflect.Method;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ml.data.DataframeBuilder;
import ml.data.DataframeUtils;
import ml.data.Mapping;
import ml.data.Question;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

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
                    return list.stream().flatMap(e -> {
                        if (StringUtils.isNotBlank(params.get("\\$ip"))) {
                            return Stream.of(params.get("\\$ip").split("[, ]+")).filter(StringUtils::isNotBlank)
                                    .map(s -> {
                                        Map<String, String> linkedHashMap = new LinkedHashMap<>(params);
                                        linkedHashMap.put("\\$ip", s);
                                        return linkedHashMap;
                                    }).map(pa -> remapUncroppred(pa, browser, e)).distinct();
                        }
                        return Stream.of(remapUncroppred(params, browser, e));
                    }).filter(Objects::nonNull)
                            .peek(o -> CommonsFX.addProgress(progress, 1. / keys.size() / list.size()))
                            .collect(Collectors.toList());
                }
                CommonsFX.addProgress(progress, 1. / keys.size());
                return replaceString(params, v);
            });
        }
        CommonsFX.update(progress, 1);
    }

    public static Map<String, String> adjustParams(Map<String, Object> mapaSubstituicao, int days, String ipParam,
            String index) {
        Map<String, String> paramText = new LinkedHashMap<>();
        Set<String> credentialText = new LinkedHashSet<>();
        for (String ipValue : ipParam.split("[, ]+")) {
            String authenticationSuccess = " AND \\\"AUTHENTICATION_SUCCESS\\\"";
            Map<String, String> credentialMap =
                    KibanaApi.getGeridCredencial(ipValue + authenticationSuccess, index, days);
            if (credentialMap.isEmpty()) {
                credentialMap = KibanaApi.getGeridCredencial(ipValue, index, days);
            }
            LOG.info("GETTING GERID CREDENTIALS {} {}", ipValue, credentialMap.keySet());
            credentialText.addAll(credentialMap.values());
            List<String> collect = credentialMap.keySet().stream().collect(Collectors.toList());
            paramText.merge("\\$orIps", ipValue, (o, n) -> ReportHelper.mergeStrings(o, n, " OR "));
            for (String credencial : collect) {
                Map<String, String> iPsByCredencial =
                        KibanaApi.getIPsByCredencial("\\\"" + credencial + "\\\"" + authenticationSuccess, index, days);
                iPsByCredencial.remove("189.9.32.130");

                credentialText.addAll(iPsByCredencial.values());
                String collect2 = iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
                paramText.merge("\\$otherIps", collect2, ReportHelper::mergeStrings);
                iPsByCredencial.keySet().forEach(
                        i -> paramText.merge("\\$orIps", i, (o, n) -> ReportHelper.mergeStrings(o, n, " OR ")));
                LOG.info("GETTING GERID IP by CREDENTIALS {} {}", credencial, iPsByCredencial.keySet());
            }

            paramText.merge("\\$creds", credentialMap.keySet().stream().map(CredentialInvestigator::credentialInfo)
                    .collect(Collectors.joining("\n")), ReportHelper::mergeStrings);
        }
        mergeImage(mapaSubstituicao,
                credentialText.stream().distinct().map(ReportHelper::textToImage).collect(Collectors.toList()));
        return paramText;
    }

    public static Map<String, String> adjustParams(String ipParam, int days, Property<Number> progress) {
        Map<String, String> paramText = new LinkedHashMap<>();
        for (String ipValue : ipParam.split("[, ]+")) {
            KibanaApi.kibanaFullScan(ipValue, days, progress)
                    .forEach((k, v) -> paramText.merge("\\$" + k, v, ReportHelper::mergeStrings));
            paramText.merge("\\$orIps", ipValue, (o, n) -> ReportHelper.mergeStrings(o, n, " OR "));
            paramText.merge("\\$otherIps", ipValue, ReportHelper::mergeStrings);
        }
        return paramText;
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
                        .findFirst().ifPresent(o -> collection.set(collection.indexOf(o), img));
            }
        }
        build.getItems().remove(selectedItem);
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

    public static Image textToImage(String s) {
        String highlight = "(\\d+\\.\\d+\\.\\d+\\.\\d+|\\d{11})";
        return PhantomJSUtils.textToImage(s, highlight);
    }

    private static WritableImage crop(Map<String, Object> info, File outFile) {
        Double widthProp = toDouble(info.get("width"));
        Double heightProp = toDouble(info.get("height"));
        Double xOffset = toDouble(info.get("x"));
        Double yOffset = toDouble(info.get("y"));
        return ImageFXUtils.cropProportionally(outFile, widthProp, heightProp, xOffset, yOffset);
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
        CommonsFX.runInPlatform(() -> {
            if (imageObj.containsKey("zoom")) {
                Double zoom = StringSigaUtils.toDouble(imageObj.getOrDefault("zoom", 1));
                browser.zoomProperty().set(zoom);
            }
            loadSite(engine, finalURL);
        });
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

    private static boolean isLoading(WebEngine engine) {
        return engine.getLoadWorker().getState() == State.RUNNING;
    }

    private static void loadSite(WebEngine engine2, String url) {
        RunnableEx.ignore(() -> {
            if (!Objects.equals(engine2.getLocation(), url)) {
                engine2.load(url);
            }
            LOG.info("LOADED {}", url);
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void mergeImage(Map<String, Object> mapaSubstituicao, List<Object> collect) {
        mapaSubstituicao.merge("gerid", collect, (o, n) -> {
            ((List<?>) o).addAll((List) n);
            return o;
        });
    }

    private static String mergeStrings(String o, String n) {
        return mergeStrings(o, n, "\n");
    }

    private static String mergeStrings(String o, String n, String delimiter) {
        return Stream.of(o, n).flatMap(m -> Stream.of(m.split(delimiter))).distinct()
                .collect(Collectors.joining(delimiter));
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

    @SuppressWarnings("unchecked")
    private static void saveCSV(File srcFile, Map<String, Object> params, File outFile) {
        DataframeBuilder dataframe = DataframeBuilder.builder(srcFile);
        List<String> cols2 = dataframe.columns().stream().map(e -> e.getKey()).collect(Collectors.toList());
        if (params.containsKey("mappings")) {
            Map<String, Object> mapping = JsonExtractor.accessMap(params, "mappings");
            mapping.forEach((k, v) -> {
                if (!cols2.contains(k)) {
                    if (v instanceof String) {
                        dataframe.rename((String) v, k);
                        dataframe.putFormat(k, String.class);
                    } else {
                        Method method = Mapping.getMethod(JsonExtractor.access(v, String.class, "method"));
                        Object[] ob = new Object[method.getParameterCount()];
                        String[] dependencies = JsonExtractor.accessList(v, "dependencies").toArray(new String[0]);
                        List<Object> otherParams = JsonExtractor.accessList(v, "otherParams");
                        for (int i = 0; i < otherParams.size(); i++) {
                            ob[i + dependencies.length] = otherParams.get(i);
                        }
                        dataframe.addCrossFeature(k, o -> {
                            for (int i = 0; i < o.length; i++) {
                                ob[i] = o[i];
                            }
                            return method.invoke(null, ob);
                        }, dependencies);
                        dataframe.putFormat(k, (Class<? extends Comparable<?>>) method.getReturnType());
                    }
                }
            });
        }
        List<String> o = JsonExtractor.accessList(params, "questions");
        List<Question> a = o.stream().map(t -> Question.parseQuestion(dataframe, t)).collect(Collectors.toList());
        for (Question question : a) {
            dataframe.filterOut(question.getColName(), question);
        }
        String columns = params.getOrDefault("columns", "").toString();
        List<String> finalHeaders = Stream.of(columns.split("\\|")).collect(Collectors.toList());
        List<Map<String, Object>> items2 = new ArrayList<>();
        dataframe.build();
        dataframe.sortHeaders(finalHeaders);

        List<String> cols = dataframe.cols();
        cols.removeIf(s -> s.matches(columns));
        dataframe.removeCol(cols.toArray(new String[0]));
        if (params.containsKey("sort")) {
            Object object = params.get("sort");
            DataframeUtils.sort(dataframe, Objects.toString(object));
        }
        dataframe.forEachRow(items2::add);
        if (items2.isEmpty()) {
            Map<String, Object> e = new LinkedHashMap<>();
            finalHeaders.forEach(c -> e.put(c, ""));
            items2.add(e);
        }
        ExcelService.getExcel(items2, outFile);

    }

    private static File saveHtmlImage(WebView browser2, File out) {
        LOG.info("SAVING to {}", out.getName());
        return SupplierEx.get(() -> ImageFXUtils.take(browser2, out));
    }

    private static WritableImage saveImage(Map<String, Object> info, File outFile, WebView browser2) {
        CommonsFX.runInPlatformSync(() -> saveHtmlImage(browser2, outFile));
        return crop(info, outFile);
    }

}
