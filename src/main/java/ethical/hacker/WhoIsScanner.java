package ethical.hacker;

import extract.JsoupUtils;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import ml.data.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.PhantomJSUtils;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class WhoIsScanner {
    private static final String REVERSE_DNS = "HostName";

    private static final String SANS_API_URL = "http://isc.sans.edu/api/ip/";

    public static final String IP_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";

    private static final Logger LOG = HasLogging.log();
    private String name = "";
    private String waitStr = "";
    private final Map<String, String> cookies = new HashMap<>();
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
        Map<String, String> ipInformation = getIpInformation(this, ip);
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

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        return fillIPInformation(builder, ipColumn, new SimpleDoubleProperty(0));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn, DoubleProperty count) {
        builder.filterOut(ipColumn, s -> !s.toString().matches("^10\\..+") && s.toString().matches(IP_REGEX));

        WhoIsScanner whoIsScanner = new WhoIsScanner();
        ObservableMap<String, Map<String, String>> ipInfoCache = FXCollections.observableHashMap();
        builder.addCrossFeature("", e -> {
            Map<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("Network", getFromCache(whoIsScanner, ipInfoCache, e, "network"));
            hashMap.put("Owner", getFromCache(whoIsScanner, ipInfoCache, e, "asname", "as_owner"));
            hashMap.put("Reverse DNS", getFromCache(whoIsScanner, ipInfoCache, e, REVERSE_DNS));
            hashMap.put("Country", getFromCache(whoIsScanner, ipInfoCache, e, "country", "ascountry"));
            return hashMap;
        }, ipColumn);
        DataframeML build = builder.build(count);
        build.removeCol("");
        return build;
    }

    public static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = getIPColumn(builder);
        return fillIPInformation(builder, ipColumn);
    }

    public static Map<String, String> getIpInformation(WhoIsScanner whoIsScanner, String ip) {
        if (ip.matches("^10\\..+")) {
            Map<String, String> hashMap = new HashMap<>();
            InetAddress ia = SupplierEx.get(() -> toInetAddress(ip));
            hashMap.put(REVERSE_DNS, ia.getCanonicalHostName());
            return hashMap;
        }
        Map<String, String> first =
                SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(ip), () -> whoIsScanner.whoIsScan(ip));
        if (ip.matches("^200\\.152\\..+")) {
            InetAddress ia = SupplierEx.get(() -> toInetAddress(ip));
            first.put(REVERSE_DNS, ia.getCanonicalHostName());
        }
        return first;
    }

    public static String getKey(Map<String, String> first, String... keys) {
        return Stream.of(keys).map(first::get).filter(Objects::nonNull).findFirst().orElse(null);

    }

    public static String getLastNumberField(BaseDataframe dataframe) {
        List<String> numberCols = dataframe.getFormatMap().entrySet().stream().filter(e -> e.getValue() != null)
                .filter(e -> Number.class.isAssignableFrom(e.getValue()))
                .map(Entry<String, Class<? extends Comparable<?>>>::getKey).collect(Collectors.toList());
        return numberCols.get(numberCols.size() - 1);
    }

    public static String getReverseDNS(String ip) throws UnknownHostException {
        InetAddress ia = toInetAddress(ip);
        return ia.getCanonicalHostName();

    }

    public static void main(String[] args) {
        List<String> asList = Arrays.asList("www.previc.gov.br", "smtp2.dataprev.gov.br", "pius.previc.gov.br",
                "owa.dataprev.gov.br", "ouvidoria.previdencia.gov.br", "mx71.registrarh-saude.dataprev.gov.br",
                "mx02.registrarh-saude.dataprev.gov.br", "mx01.registrarh-saude.dataprev.gov.br", "mx.inss.gov.br",
                "mx.dataprev.gov.br", "mailint.dataprev.gov.br", "cioba.previdencia.gov.br",
                "cgeridinss.dataprev.gov.br", "agendaconselho.dataprev.gov.br", "cgeridinss.dataprev.gov.br",
                "chat.dataprev.gov.br", "cioba.previdencia.gov.br", "consultaprocessos.inss.gov.br",
                "correio.dataprev.gov.br", "correio.inss.gov.br", "correiov2.dataprev.gov.br", "correiov2.inss.gov.br",
                "correiov3.inss.gov.br", "correiovs.dataprev.gov.br", "cplp.dataprev.gov.br", "databox.dataprev.gov.br",
                "dtp-gerid.dataprev.gov.br", "erecursos.previdencia.gov.br", "estatisticasweb.dataprev.gov.br",
                "estatisticaswebatualiza.dataprev.gov.br", "eu.dataprev.gov.br", "exemplo20080624.previdencia.gov.br",
                "treina-sei.inss.gov.br", "geridmps.dataprev.gov.br", "geridprevic.dataprev.gov.br",
                "hagendaconselho.dataprev.gov.br", "hdtp.gerid.dataprev.gov.br", "hempregabrasil.mte.gov.br",
                "homo-sei.inss.gov.br", "hportal.inss.gov.br", "hprevic.gerid.dataprev.gov.br", "hu.dataprev.gov.br",
                "imap.dataprev.gov.br", "m.dataprev.gov.br", "mailint.dataprev.gov.br", "mx.dataprev.gov.br",
                "mx.inss.gov.br", "mx01.registrarh-saude.dataprev.gov.br", "ouvidoria.previdencia.gov.br",
                "owa.dataprev.gov.br", "pesquisas.dataprev.gov.br", "pius.previc.gov.br", "portal.dataprev.gov.br",
                "previc.gerid.dataprev.gov.br", "projetos.dataprev.gov.br", "sf.dataprev.gov.br",
                "saa2.previdencia.gov.br", "sdtp.gerid.dataprev.gov.br", "sei.inss.gov.br", "sirc.gov.br",
                "smtp2.dataprev.gov.br", "waw-erecursos", "www-dtpprojetos.prevnet", "www-gerdic", "www.inss.gov.br",
                "www.previc.gov.br", "www.sirc.gov.br", "wwwhom.inss.gov.br").stream().distinct()
                .collect(Collectors.toList());
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        for (int i = 0; i < asList.size(); i++) {
            String url = asList.get(i);
            LOG.info("SCANNING {} -- {}/{}", url, i, asList.size());
            RunnableEx.run(() -> whoIsScanner.name(url).waitStr("Please wait...")
                    .subFolder("#gradeA", "#warningBox", "ratingTitle", "reportTitle").evaluateURL(
                            "https://www.ssllabs.com/ssltest/analyze.html?d=" + url + "&ignoreMismatch=on&latest"));
        }
    }

    public static String reorderAndLog(DataframeML dataframe, String numberField) {
        DataframeUtils.sort(dataframe, numberField);
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, "Rede", numberField);
        createSeries.forEach(s2 -> LOG.info("{}", s2));
        List<Entry<Object, Double>> series = DataframeUtils.createSeries(dataframe, "Owner", numberField);
        series.forEach(s1 -> LOG.info("{}", s1));
        dataframe.removeCol("filters");
        return DataframeUtils.toString(dataframe, 30);

    }

    private static String getFromCache(WhoIsScanner whoIsScanner, ObservableMap<String, Map<String, String>> ipInfo,
            Object[] e, String... string) {
        return getKey(ipInfo.computeIfAbsent(e[0].toString(), ip -> getIpInformation(whoIsScanner, ip)), string);
    }

    private static String getIPColumn(DataframeBuilder builder) {
        return builder.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst().orElse(null);
    }

    private static InetAddress toInetAddress(String ip) throws UnknownHostException {
        List<Byte> collect =
                Stream.of(ip.split("\\.")).map(t -> Integer.valueOf(t).byteValue()).collect(Collectors.toList());
        return InetAddress.getByAddress(new byte[] { collect.get(0), collect.get(1), collect.get(2), collect.get(3) });
    }

}