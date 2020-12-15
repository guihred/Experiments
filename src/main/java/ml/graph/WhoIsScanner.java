package ml.graph;

import extract.CIDRUtils;
import extract.JsoupUtils;
import extract.PhantomJSUtils;
import extract.VirusTotalApi;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringEscapeUtils;
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
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class WhoIsScanner {
    public static final String REVERSE_DNS = "HostName";

    private static final String SANS_API_URL = "http://isc.sans.edu/api/ip/";

    public static final String IP_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";

    public static final Logger LOG = HasLogging.log();
    private String name = "";
    private String waitStr = "";
    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> cache = new HashMap<>();
    private String[] subFolder = new String[] {};

    private File print;

    public boolean containsWait(File htmlFile) throws IOException {
        String string = com.google.common.io.Files.toString(htmlFile, StandardCharsets.UTF_8);
        return Stream.of(subFolder).allMatch(t -> !string.contains(t)) || string.contains(waitStr);
    }

    public WhoIsScanner cookie(String string, String string2) {
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

    public Map<String, String> getIpInformation(String ip) {
        Map<String, String> ipInformation = WhoIsScanner.getIpInformation(this, ip);
        ipInformation.putIfAbsent("id", ip);
        LOG.info("{}", ipInformation);
        return ipInformation;
    }

    public File getPrint() {
        return print;
    }

    public WhoIsScanner name(String url1) {
        name = url1;
        return this;
    }

    public String reverseDNS(String ip) {
        return cache.computeIfAbsent(ip, WhoIsScanner::getReverseDNS);
    }

    public ObservableList<Map<String, String>> scanIps(String ip) {
        ObservableList<Map<String, String>> observableArrayList = FXCollections.observableArrayList();
        String[] split = ip.split("[\\s,;]+");
        RunnableEx.runNewThread(() -> {
            for (String string : split) {
                RunnableEx.run(() -> observableArrayList.add(whoIsScan(string)));
            }
        });
        return observableArrayList;
    }

    public WhoIsScanner subFolder(String... subFolder1) {
        subFolder = subFolder1;
        return this;
    }

    public WhoIsScanner waitStr(String waitStr1) {
        waitStr = waitStr1;
        return this;
    }

    public Map<String, String> whoIsScan(String ip) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Document document = reloadIfExists(ip);
        document.getElementsByTag("ip").forEach(
                e -> e.children().forEach(m -> map.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        LOG.info("{}", map);
        return map;
    }

    private Document reloadIfExists(String ip) throws IOException {
        File outFile = ResourceFXUtils.getOutFile("xml/" + ip + ".xml");
        if (outFile.exists()) {
            return Jsoup.parse(outFile, StandardCharsets.UTF_8.name());
        }
        String scanIP = SANS_API_URL + ip;
        Document document = JsoupUtils.getDocument(scanIP, cookies);
        Files.write(outFile.toPath(), Arrays.asList(document.outerHtml()), StandardCharsets.UTF_8);
        return document;
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

    public static Document evaluateURL(String url, String name, String waitStr, String... subFolder)
            throws IOException {
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        return whoIsScanner.name(name).waitStr(waitStr).subFolder(subFolder).evaluateURL(url);
    }

    public static Map<String, String> getIpInformation(WhoIsScanner whoIsScanner, String ip) {
        if (ip.matches("^10\\..+")) {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put(REVERSE_DNS, whoIsScanner.reverseDNS(ip));
            return hashMap;
        }
        Map<String, String> first =
                SupplierEx.getFirst(() -> CIDRUtils.findNetwork(ip), () -> VirusTotalApi.getIpTotalInfo(ip),
                        () -> whoIsScanner.whoIsScan(ip));
        if (ip.matches("^200\\.152\\..+")) {
            first.put(REVERSE_DNS, whoIsScanner.reverseDNS(ip));
        }
        return first;
    }

    public static String getReverseDNS(String ip) {
        return SupplierEx.get(() -> CIDRUtils.toInetAddress(ip).getCanonicalHostName());
    }

}