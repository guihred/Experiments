package kibana;

import static utils.StringSigaUtils.toDouble;

import com.google.common.io.Files;
import extract.PPTService;
import extract.WordService;
import extract.web.JsonExtractor;
import extract.web.JsoupUtils;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
import ml.data.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class ReportHelper {
    private static final List<String> REMOVE_IPS = ProjectProperties.getFieldList();
    private static final String OR_IPS_KEY = "\\$orIps";
    private static final String SPLIT_REGEX = "[, ]+";
    private static final String IP_KEY = "\\$ip";
    private static final Logger LOG = HasLogging.log();

    private static PhantomJSUtils phantomJs;

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
        List<String> urls = new ArrayList<>();
        for (String key : keys) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    List<?> list = (List<?>) v;
                    return list.stream().flatMap(e -> {
                        if (StringUtils.isNotBlank(params.get(IP_KEY))) {
                            Set<Object> set = new LinkedHashSet<>();
                            return Stream.of(params.get(IP_KEY).split(SPLIT_REGEX)).filter(StringUtils::isNotBlank)
                                    .map(s -> {
                                        Map<String, String> linkedHashMap = new LinkedHashMap<>(params);
                                        linkedHashMap.put(IP_KEY, s);
                                        return linkedHashMap;
                                    }).map(pa -> remapUncroppred(pa, browser, e, urls))
                                    .filter(o -> !(o instanceof Image)
                                            || set.add(ClassReflectionUtils.invoke(o, "impl_getUrl")))
                                    .distinct();
                        }
                        return Stream.of(remapUncroppred(params, browser, e, urls));
                    }).filter(Objects::nonNull)
                            .peek(o -> CommonsFX.addProgress(progress, 1. / keys.size() / list.size()))
                            .collect(Collectors.toList());
                }
                CommonsFX.addProgress(progress, 1. / keys.size());
                return replaceString(params, v);
            });
        }
        String collect = urls.stream().distinct()
                .map(s -> "<tr><td><iframe style=\"border: 0\" src=\"" + s
                        + "\" height=\"660\" width=\"1200\"></iframe></tr></td>")
                .collect(Collectors.joining("\n", "<html><head><meta charset=\"UTF-8\"></head><body><table>\n",
                        "\n</table></body></html>"));
        LOG.info("\n{}", collect);
        RunnableEx.run(() -> {

            String reportName = getReportName(mapaSubstituicao, params).replaceAll("(pptx|docx)", "html");
            Files.append(collect, ResourceFXUtils.getOutFile("html/" + reportName), StandardCharsets.UTF_8);
        });
        CommonsFX.update(progress, 1);
    }

    public static Map<String, String> adjustParams(Map<String, Object> mapaSubstituicao, int days, String ipParam,
            String index, Boolean searchCredencial, DoubleProperty doubleProperty) {
        Map<String, String> paramText = new LinkedHashMap<>();
        Set<String> credentialText = new LinkedHashSet<>();
        CommonsFX.update(doubleProperty, 0);
        String[] split = ipParam.split(SPLIT_REGEX);
        for (String ipValue : split) {
            String authenticationSuccess = " AND \\\"AUTHENTICATION_SUCCESS\\\"";
            Map<String, String> credentialMap =
                    KibanaApi.getGeridCredencial(ipValue + authenticationSuccess, index, days);
            if (credentialMap.isEmpty()) {
                credentialMap = KibanaApi.getGeridCredencial(ipValue, index, days);
            }
            CommonsFX.addProgress(doubleProperty, 1. / (credentialMap.size() + 1) / split.length);
            LOG.info("GETTING GERID CREDENTIALS {} {}", ipValue, credentialMap.keySet());
            credentialText.addAll(credentialMap.values());
            List<String> collect = credentialMap.keySet().stream().collect(Collectors.toList());
            paramText.merge(OR_IPS_KEY, ipValue, (o, n) -> ReportHelper.mergeStrings(o, n, " OR "));
            if (searchCredencial) {
                for (String credencial : collect) {

                    Map<String, String> iPsByCredencial =
                            KibanaApi.getIPsByCredencial("\\\"" + credencial + "\\\"" + authenticationSuccess, index, days);
                    REMOVE_IPS.forEach(iPsByCredencial::remove);
                    credentialText.addAll(iPsByCredencial.values());
                    String collect2 = iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
                    paramText.merge("\\$otherIps", collect2, ReportHelper::mergeStrings);
                    CommonsFX.addProgress(doubleProperty, 1. / collect.size() / split.length);
                    iPsByCredencial.keySet().forEach(
                            i -> paramText.merge(OR_IPS_KEY, i, (o, n) -> ReportHelper.mergeStrings(o, n, " OR ")));
                    LOG.info("GETTING GERID IP by CREDENTIALS {} {}", credencial, iPsByCredencial.keySet());
                }
            }

            paramText.merge("\\$creds", credentialMap.keySet().stream().map(CredentialInvestigator::credentialInfo)
                    .collect(Collectors.joining("\n")), ReportHelper::mergeStrings);
        }
        mergeImage(mapaSubstituicao,
                credentialText.stream().distinct().map(ReportHelper::textToImage).collect(Collectors.toList()));
        CommonsFX.update(doubleProperty, 1);

        return paramText;
    }

    public static Map<String, String> adjustParams(String ipParam, int days, Property<Number> progress) {
        Map<String, String> paramText = new LinkedHashMap<>();
        for (String ipValue : ipParam.split(SPLIT_REGEX)) {
            KibanaApi.kibanaFullScan(ipValue, days, progress)
                    .forEach((k, v) -> paramText.merge("\\$" + k, v, ReportHelper::mergeStrings));
            paramText.merge(OR_IPS_KEY, ipValue, (o, n) -> ReportHelper.mergeStrings(o, n, " OR "));
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

    public static PhantomJSUtils getPhantomJs() {
        return phantomJs == null ? phantomJs = new PhantomJSUtils(true) : phantomJs;
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

    public static void quit() {
        if (phantomJs != null) {
            phantomJs.quit();
        }
    }

    public static String replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public static File reportName(Map<String, Object> mapaSubstituicao, Map<String, String> params2) {
        String replaceString = getReportName(mapaSubstituicao, params2);

        String extension = ReportHelper.getExtension(replaceString);
        return ResourceFXUtils.getOutFile(extension + "/" + replaceString);
    }

    public static Image textToImage(String s) {
        String highlight = "(\\d+\\.\\d+\\.\\d+\\.\\d+|\\d{11})";
        return PhantomJSUtils.textToImage(s, highlight);
    }

    @SuppressWarnings("unchecked")
    private static void addMapping(DataframeBuilder dataframe, List<String> cols2, String k, Object v) {

        if (v instanceof Map) {
            Method method = Mapping.getMethod(JsonExtractor.access(v, String.class, "method"));
            Object[] ob = new Object[method.getParameterCount()];
            String[] dependencies = JsonExtractor.<String>accessList(v, "dependencies").stream().filter(cols2::contains)
                    .toArray(i -> new String[i]);
            List<Object> otherParams = JsonExtractor.accessList(v, "otherParams");
            for (int i = 0; i < otherParams.size(); i++) {
                ob[i + dependencies.length] = otherParams.get(i);
            }
            if (dependencies.length + otherParams.size() != method.getParameterCount()) {
                HasLogging.log().error("\"{}\" METHOD {} DOES NOT MATCH Parameters {} {}", k, method, dependencies,
                        otherParams);
                return;
            }
            dataframe.addCrossFeature(k, o -> {
                for (int i = 0; i < o.length; i++) {
                    ob[i] = o[i];
                }
                return method.invoke(null, ob);
            }, dependencies);
            dataframe.putFormat(k, (Class<? extends Comparable<?>>) method.getReturnType());
        }
        if (cols2.contains(k)) {
            return;
        }
        if (v instanceof List) {
            for (Object m : (List<?>) v) {
                dataframe.rename(Objects.toString(m, ""), k);
            }
            dataframe.putFormat(k, String.class);
            return;
        }
        if (v instanceof String) {
            dataframe.rename((String) v, k);
            dataframe.putFormat(k, String.class);
        }
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

    private static String getReportName(Map<String, Object> mapaSubstituicao, Map<String, String> params2) {
        String replaceString = ReportHelper.replaceString(params2, mapaSubstituicao.get("name"))
                .replaceAll("\\.(inss|gov|br|prevnet|dataprev)|vip-p?", "");
        return replaceString;
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
        RunnableEx.measureTime("Load Site " + imageObj.get("name"),
                () -> image.setValue(loadURL(browser, outFile, engine)));
        return image.getValue();
    }

    private static boolean isLoading(WebEngine engine) {
        return engine.getLoadWorker().getState() == State.RUNNING;
    }

    private static void loadSite(WebEngine engine2, String url) {
        RunnableEx.ignore(() -> {
            if (!Objects.equals(engine2.getLocation(), url)) {
                engine2.setUserAgent(
                        JsoupUtils.USER_AGENT + "\nAuthorization: Basic " + ExtractUtils.getEncodedAuthorization()
                                + "\nProxy-Authorization: Basic " + ExtractUtils.getEncodedAuthorization()

                );
                engine2.load(url);
            }
            LOG.info("LOADED {}", url);
        });
    }

    private static Image loadURL(WebView browser, File outFile, WebEngine engine) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        while (atomicBoolean.get()) {
            RunnableEx.sleepSeconds(6);
            CommonsFX.runInPlatform(() -> atomicBoolean.set(isLoading(engine)));
            RunnableEx.sleepSeconds(6);
        }
        CommonsFX.runInPlatformSync(() -> saveHtmlImage(browser, outFile));
        String externalForm = ResourceFXUtils.convertToURL(outFile).toExternalForm();
        Image value = new Image(externalForm);
        if (!ImageFXUtils.isWhiteImage(value)) {
            return value;
        }
        return new Image(externalForm);
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

    private static Object remapUncroppred(Map<String, String> params, WebView browser, Object e, List<String> urls) {
        if (e instanceof Map) {
            Map<String, Object> e2 = JsonExtractor.accessMap(e);
            if (e2.containsKey("url")) {
                String kibanaURL = Objects.toString(e2.get("url"), "");
                urls.add(replaceString(params, kibanaURL));
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
        DataframeBuilder dataframe = DataframeBuilder.builder(srcFile);
        List<String> cols2 = dataframe.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .collect(Collectors.toList());
        if (params.containsKey("mappings")) {
            Map<String, Object> mapping = JsonExtractor.accessMap(params, "mappings");
            mapping.forEach((k, v) -> addMapping(dataframe, cols2, k, v));
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
