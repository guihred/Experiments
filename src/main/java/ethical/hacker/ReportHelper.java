package ethical.hacker;

import static utils.StringSigaUtils.toDouble;

import extract.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker.State;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import paintexp.tool.RectBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class ReportHelper {
    private static final Logger LOG = HasLogging.log();

    private ReportHelper(){}

    public static void addParameters(Map<String, Object> mapaSubstituicao, Map<String, String> params,
            WebView browser) {
        for (String key : mapaSubstituicao.keySet().stream().collect(Collectors.toList())) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    return ((List<?>) v).stream().map(e -> remap(params, e, browser)).filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                return replaceString(params, v);
            });
        }
    }

    public static boolean isLoading(WebEngine engine) {
        return engine.getLoadWorker().getState() == State.RUNNING;
    }

    public static void loadSite(WebEngine engine2, String url) {
        RunnableEx.ignore(() -> {
            engine2.load(url);
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

    public static String replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public static File saveHtmlImage(WebView browser2) {
        return SupplierEx.get(() -> {
            Bounds bounds = browser2.getBoundsInLocal();
            return ImageFXUtils.take(browser2, bounds.getWidth(), bounds.getHeight());
        });
    }

    public static Image textToImage(String s) {
        String collect2 = Stream.of(s.split("\n")).filter(StringUtils::isNotBlank)
                .map(str -> "<p>" + str.replaceAll("(\\d+\\.\\d+\\.\\d+\\.\\d+)", "<font>$1</font>") + "</p>")
                .collect(Collectors.joining("\n"));
        String format =
                String.format("<!DOCTYPE html>\n<html>\n<head>\n<style>\nfont {background-color: yellow;}</style>\n"
                        + "</head><body>%s</body>\n</html>", collect2);
        return PhantomJSUtils.saveHtmlImage(format);
    }

    private static Image getImage(Map<String, Object> imageObj, WebView browser,
            Map<String, String> params) {
        File outFile = ResourceFXUtils
                .getOutFile("print/" + replaceString(params, imageObj.getOrDefault("name", "erro")) + ".png");
        if (outFile.exists()) {
            return new Image(ResourceFXUtils.convertToURL(outFile).toExternalForm());
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
            image.setValue(saveImage(imageObj, outFile, browser));
        });
        return image.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Object remap(Map<String, String> params, Object e, WebView browser) {
        if (e instanceof Map) {
            return getImage((Map<String, Object>) e, browser, params);
        }
        if (e instanceof Image) {
            return e;
        }
        return replaceString(params, e);
    }

    private static WritableImage saveImage(Map<String, Object> info, File outFile, WebView browser2)
            throws IOException {
        File saveHtmlImage = CommonsFX.runInPlatformSync(() -> saveHtmlImage(browser2));
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

}
