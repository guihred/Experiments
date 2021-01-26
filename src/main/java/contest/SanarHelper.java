package contest;

import com.google.common.io.Files;
import extract.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.SupplierEx;

public class SanarHelper {
    private static final String PREFIX = "https://www.e-sanar.com.br/aluno/curso/292";

    public static void main(String[] args) {

        String url1 = PREFIX + ",preparatorio-para-farmacia.html";
        File outFile = ResourceFXUtils.getOutFile("farmacia.html");
        Document doc = SupplierEx.getFirst(() -> {
            return Jsoup.parse(outFile, StandardCharsets.UTF_8.toString());
        }, () -> {
            return loadFile(url1, outFile);
        });
        doc.select("#accordion .panel-default").forEach(panel -> {
            String folder = SanarCrawler.fixName(panel.select("h4").text());
            List<String> collect = panel.select("tr").stream().map(tr -> {
                return SanarCrawler.fixName(tr.text());
            }).collect(Collectors.toList());
            System.out.println(folder + "=" + collect);
            for (String name : collect) {
                List<String> asList = Arrays.asList(".mp4", ".pdf");
                for (String extension : asList) {
                    String finalName = SanarCrawler.fixName(name).replaceAll("\\.\\w$", "");
                    String out = finalName + extension;
                    File videoFileName = ResourceFXUtils.getOutFile("videos/" + out);
                    File pdfFileName = ResourceFXUtils.getOutFile("eSanar/" + out);
                    Path firstPathByExtension =
                            FileTreeWalker.getFirstPathByExtension(SanarCrawler.consulta, out);
                    File f = videoFileName.exists() ? videoFileName
                            : pdfFileName.exists() ? pdfFileName
                                    : firstPathByExtension != null ?
                            firstPathByExtension.toFile() : null;
                    if (f != null) {
                        File outFile2 = ResourceFXUtils.getOutFile("eSanar/" + folder + "/" + out);
                        f.renameTo(outFile2);
                    }
                }
            }

        });

    }

    private static Document loadFile(String url1, File outFile) throws IOException {
        PhantomJSUtils phantomJSUtils = new PhantomJSUtils();
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ESANARSESSID", "m5otcssorltp87a8s62fmoqtc5");
        cookies.put("intercom-id-sju7o7kl", "1d4fda3e-8f5f-4a98-b129-1f73eb81f9e5");
        cookies.put("intercom-session-sju7o7kl",
                "WlFGTXRkc1NlSWZxdGxXL0NmTlk5RmV1ZWVDOVVaWUE5aDNlbWYxNDU5eFl"
                        + "QQmg1QWl6VTk1SGxsNngvdXRlRi0tS1pNNDlhVWtiMndZRFFWazdKWkRBQT09"
                        + "--60174817d9b3174e8d0eccfa9f2b797787d1c209");
        cookies.put("muxData", "mux_viewer_id=e3480ca3-e1f1-44ee-b6b5-d8d7655e92c7&msn=0.12306064932172656");
        Document render = phantomJSUtils.render(url1, cookies, 10);
        phantomJSUtils.quit();
        Document normalise = render.normalise();
        Files.write(normalise.toString(), outFile, StandardCharsets.UTF_8);
        return normalise;
    }
}
