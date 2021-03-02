package extract.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;

public class WebScanner {
    private static final Logger LOG = HasLogging.log();
    private String name = "";
    private String waitStr = "";
    private final Map<String, String> cookies = new HashMap<>();
    private String[] subFolder = new String[] {};

    private File print;

    public WebScanner cookie(String string, String string2) {
        cookies.put(string, string2);
        return this;
    }

    public Document evaluateURL(String url) throws IOException {
        String screenshotsFolder = "screenshots/";
        File htmlFile = FileTreeWalker
                .getFirstFileMatch(ResourceFXUtils.getOutFile("screenshots"),
                        p -> p.getName(p.getNameCount() - 1).toString().startsWith(name + ".html"))
                .stream().max(Comparator.comparing(p -> ResourceFXUtils.computeAttributes(p.toFile()).size()))
                .map(Path::toFile).orElse(ResourceFXUtils.getOutFile(screenshotsFolder + name + ".html"));
        Document renderPage;
        if (!htmlFile.exists() || containsWait(htmlFile)) {
            print = ResourceFXUtils.getOutFile(screenshotsFolder + name + ".png");
            renderPage = PhantomJSUtils.renderPage(url, cookies, waitStr, print);
            Files.write(htmlFile.toPath(), renderPage.outerHtml().getBytes(StandardCharsets.UTF_8));
        } else {
            renderPage = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());
        }

        repositionFiles(screenshotsFolder, renderPage);
        return renderPage;
    }

    public File getPrint() {
        return print;
    }

    public WebScanner name(String url1) {
        name = url1;
        return this;
    }


    public WebScanner subFolder(String... subFolder1) {
        subFolder = subFolder1;
        return this;
    }

    public WebScanner waitStr(String waitStr1) {
        waitStr = waitStr1;
        return this;
    }

    private boolean containsWait(File htmlFile) throws IOException {
        String fileContent = com.google.common.io.Files.toString(htmlFile, StandardCharsets.UTF_8);
        return Stream.of(subFolder).allMatch(t -> !fileContent.contains(t)) || fileContent.contains(waitStr);
    }

    private void repositionFiles(String screenshotsFolder, Document renderPage) throws IOException {
        List<String> tables = JsoupUtils.getTables(renderPage);
        File outFile = ResourceFXUtils.getOutFile(screenshotsFolder + name + ".txt");
        Files.write(outFile.toPath(), tables, StandardCharsets.UTF_8);
        List<Path> firstFileMatch = FileTreeWalker.getFirstFileMatch(ResourceFXUtils.getOutFile("screenshots"),
                p -> p.getName(p.getNameCount() - 1).toString().startsWith(name));
        String text = Stream.of(subFolder).map(renderPage::select).map(Elements::text).filter(StringUtils::isNotBlank)
                .findFirst().orElse("");
        firstFileMatch.forEach(ConsumerEx.make(p -> {
            if (StringUtils.isBlank(text)) {
                Files.delete(p);
                return;
            }
            File out1File = ResourceFXUtils.getOutFile(
                    screenshotsFolder + text.replaceAll("[\\| :\\\\]+", "_").trim() + "/" + p.toFile().getName());
            if (out1File.getName().endsWith(".png")) {
                print = out1File;
            }
            if (!out1File.equals(p.toFile())) {
                ExtractUtils.copy(p.toFile(), out1File);
                Files.delete(p);
            }
        }, (path, ex) -> LOG.error("ERROR COPYING {}", path, ex)));
    }

}