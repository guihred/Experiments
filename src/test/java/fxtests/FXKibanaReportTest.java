package fxtests;

import ethical.hacker.WebBrowserApplication;
import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import paintexp.tool.RectBuilder;
import utils.DateFormatUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {
    WebBrowserApplication show;

    @Test
    public void testWordReport() throws IOException {

        String finalIP = "191.242.238.223";

        Map<String, Object> mapaSubstituicao =
                JsonExtractor.accessMap(JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/modeloRelatorio.json")));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("\\$ip", finalIP);
        params.put("\\$date", DateFormatUtils.currentDate());
        for (String key : mapaSubstituicao.keySet().stream().collect(Collectors.toList())) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    return ((List<?>) v).stream().map(e -> remap(finalIP, params, e)).collect(Collectors.toList());
                }
                return replaceString(params, v);
            });
        }

        File file = ResourceFXUtils.getOutFile("docx/resultado" + finalIP + ".docx");
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
    }

    private Image getImage(String finalIP, Map<String, Object> imageObj) {
        show = show == null ? show(WebBrowserApplication.class) : show;
        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");
        String replaceAll = kibanaURL.replaceAll("191.96.73.211", finalIP);

        interactNoWait(() -> show.loadSite(replaceAll));

        measureTime("WordService.getWord", () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                interactNoWait(() -> {
                    boolean loading = show.isLoading();
                    atomicBoolean.set(loading);
                    return loading;
                });
                sleep(5000);
            }
            interactNoWait(RunnableEx.make(() -> {
                File saveHtmlImage = show.saveHtmlImage();
                String externalForm = ResourceFXUtils.convertToURL(saveHtmlImage).toExternalForm();
                Image value = new Image(externalForm);
                Rectangle a = new Rectangle();
                RectBuilder.copyImagePart(value, new WritableImage(0, 0), a);

                image.setValue(value);
            }));
        });
        return image.getValue();
    }

    @SuppressWarnings("unchecked")
    private Object remap(String finalIP, Map<String, String> params, Object e) {
        if (e instanceof Map) {
            return getImage(finalIP, (Map<String, Object>) e);
        }
        return replaceString(params, e);
    }

    private static Object replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

}