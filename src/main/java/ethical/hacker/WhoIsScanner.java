package ethical.hacker;

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
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.DataframeUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import utils.*;

public class WhoIsScanner {
    public static final String IP_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";

    private static final Logger LOG = HasLogging.log();

    private final Map<String, String> cookies;

    public WhoIsScanner() {
        cookies = new HashMap<>();
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

    public Map<String, String> whoIsScan(String ip) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Document document = reloadIfExists(ip);
        document.getElementsByTag("ip").forEach(
                e -> e.children().forEach(m -> map.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        LOG.info("{}", map);
        return map;
    }

    private Document reloadIfExists(String ip) throws IOException {
        String scanIP = "http://isc.sans.edu/api/ip/" + ip;
        File outFile = ResourceFXUtils.getOutFile("xml/" + ip + ".xml");
        if (outFile.exists()) {
            return Jsoup.parse(outFile, StandardCharsets.UTF_8.name());
        }
        Document document = ExtractUtils.getDocument(scanIP, cookies);
        Files.write(outFile.toPath(), Arrays.asList(document.outerHtml()), StandardCharsets.UTF_8);
        return document;
    }

    public static boolean containsWait(File htmlFile) throws IOException {
        String string = com.google.common.io.Files.toString(htmlFile, StandardCharsets.UTF_8);
        return !string.contains("ratingTitle") && !string.contains("reportTitle") || string.contains("Please wait...");
    }

    public static void evaluateURL(String url) throws IOException {
        String screenshotsFolder = "screenshots/";
        File htmlFile = ResourceFXUtils
                .getFirstFileMatch(ResourceFXUtils.getOutFile("screenshots"),
                        p -> p.getName(p.getNameCount() - 1).toString().startsWith(url + ".html"))
                .stream().max(Comparator.comparing(p -> ResourceFXUtils.computeAttributes(p.toFile()).size()))
                .map(Path::toFile)
                .orElse(ResourceFXUtils.getOutFile(screenshotsFolder + url + ".html"));
        Document renderPage;
        if (!htmlFile.exists() || containsWait(htmlFile)) {
            File pngFile = ResourceFXUtils.getOutFile(screenshotsFolder + url + ".png");
            renderPage = ExtractUtils.renderPage(
                    "https://www.ssllabs.com/ssltest/analyze.html?d=" + url + "&ignoreMismatch=on&latest",
                    pngFile, "Please wait...");
            Files.write(htmlFile.toPath(), renderPage.outerHtml().getBytes(StandardCharsets.UTF_8));
        } else {
            renderPage = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());
        }

        List<String> tables = ExtractUtils.getTables(renderPage);
        File outFile = ResourceFXUtils.getOutFile(screenshotsFolder + url + ".txt");
        Files.write(outFile.toPath(), tables, StandardCharsets.UTF_8);
        List<Path> firstFileMatch = ResourceFXUtils.getFirstFileMatch(ResourceFXUtils.getOutFile("screenshots"),
                p -> p.getName(p.getNameCount() - 1).toString().startsWith(url));
        String text2 = renderPage.select("#gradeA").text();
        String text = StringUtils.isNotBlank(text2) ? text2 : renderPage.select("#warningBox").text();
        firstFileMatch.forEach(ConsumerEx.make(p -> {
            File out1File = ResourceFXUtils
                    .getOutFile(screenshotsFolder + text.replaceAll("[ :]+", "_").trim() + "/" + p.toFile().getName());
            if (StringUtils.isNotBlank(text)) {
                if (!out1File.equals(p.toFile())) {
                    ExtractUtils.copy(p.toFile(), out1File);
                    Files.delete(p);
                }

            } else {
                Files.delete(p);
            }
        }, (Path e, Throwable p) -> LOG.error("ERROR COPYING {}", e, p)));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        return fillIPInformation(builder, ipColumn, new SimpleDoubleProperty(0));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn, DoubleProperty count) {
        builder.filter(ipColumn, s -> !s.toString().matches("^10\\..+") && s.toString().matches(IP_REGEX));
        DataframeML dataframe = builder.build();
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        DataframeUtils.crossFeatureObject(dataframe, "Rede", e -> {
            count.set(1 + count.get());
            return SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("network"),
                    () -> whoIsScanner.whoIsScan(e[0].toString()).get("network"));
        }, ipColumn);
        DataframeUtils.crossFeatureObject(dataframe, "Owner",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("as_owner"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("asname")),
                ipColumn);
        DataframeUtils.crossFeatureObject(dataframe, "Country",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("country"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("ascountry")),
                ipColumn);
        return dataframe;
    }

    public static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = getIPColumn(builder);
        return fillIPInformation(builder, ipColumn);
    }

    public static String getLastNumberField(DataframeML dataframe) {
        List<String> numberCols =
                dataframe.getFormatMap().entrySet().stream().filter(e -> Number.class.isAssignableFrom(e.getValue()))
                        .map(Entry<String, Class<? extends Comparable<?>>>::getKey).collect(Collectors.toList());
        return numberCols.get(numberCols.size() - 1);
    }

    public static String getReverseDNS(String ip) throws UnknownHostException {
        List<Byte> collect =
                Stream.of(ip.split("\\.")).map(t -> Integer.valueOf(t).byteValue()).collect(Collectors.toList());
        InetAddress ia =
                InetAddress.getByAddress(new byte[] { collect.get(0), collect.get(1), collect.get(2), collect.get(3) });
        return ia.getCanonicalHostName();

    }

    public static void main(String[] args) {
        List<String> asList = Arrays.asList("caged.maisemprego.mte.gov.br", "www3.dataprev.gov.br", "dataprev.gov.br",
                "ppfacil.dataprev.gov.br", "caged.maisemprego.mte.gov.br", "vip-pgerid01.dataprev.gov.br",
                "geriddtp.dataprev.gov.br", "saa.previdencia.gov.br", "psdcwlg.dataprev.gov.br",
                "captcha.dataprev.gov.br", "geridinss.dataprev.gov.br", "pssomteapr01.dataprev.gov.br",
                "tarefas.inss.gov.br", "www11.dataprev.gov.br", "meu.inss.gov.br", "vip-agendamentoapr01.inss.gov.br",
                "pservicoexternoapr01.dataprev.gov.br", "vip-pmeuinssprxr.inss.gov.br", "vip-psat.inss.gov.br",
                "vip-auxilioemergencial.dataprev.gov.br", "portal.dataprev.gov.br", "vip-ppmf.inss.gov.br",
                "mobdigital.inss.gov.br", "vip-ppmfapr03.dataprev.gov.br", "consultacadastral.inss.gov.br",
                "www5.dataprev.gov.br", "www2.dataprev.gov.br", "www9.dataprev.gov.br", "pcnisweb01.dataprev.gov.br",
                "pcnisappweb01.inss.gov.br", "pesocialweb01.dataprev.gov.br", "b2b.dataprev.gov.br",
                "www8.dataprev.gov.br", "www6.dataprev.gov.br", "vip-pcomprevohs.inss.gov.br",
                "vip-pcomprevapacheinter.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br",
                "portal.dataprev.gov.br", "vip-auxilio-emergencial-gerencia.dataprev.gov.br",
                "extratoir-weblog-prod.inss.gov.br", "psispagbenapr.dataprev.gov.br", "vip-sisgpep-prod.inss.gov.br",
                "vip-psisrec.inss.gov.br", "www99.dataprev.gov.br", "vip-pcniswebapr02.inss.gov.br",
                "vip-pedocapr01.dataprev.gov.br", "dadosabertos.dataprev.gov.br", "edoc.inss.gov.br",
                "ppfacil.dataprev.gov.br", "edoc-mobile.dataprev.gov.br", "vip-pmoodle.dataprev.gov.br",
                "edoc4.inss.gov.br", "www-ohsrevartrecben.dataprev.gov.br", "vip-pcoaf.dataprev.gov.br",
                "gru.inss.gov.br", "rppss.cnis.gov.br", "vip-psiacwebapr01.dataprev.gov.br",
                "vip-psiacproxyrev.dataprev.gov.br", "homol-store.dataprev.gov.br", "store.dataprev.gov.br",
                "degustacao.dataprev.gov.br", "vip-psicapweb.dataprev.gov.br", "sinpat.dataprev.gov.br",
                "pportalmaisemprego.dataprev.gov.br", "vip-psineaberto.dataprev.gov.br",
                "mte-auto-atendimento.dataprev.gov.br", "mte-posto-atendimento.dataprev.gov.br");
        for (int i = 0; i < asList.size(); i++) {
            String url = asList.get(i);
            LOG.info("SCANING {} -- {}/{}", url, i, asList.size());
            RunnableEx.run(() -> WhoIsScanner.evaluateURL(url));
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

    private static String getIPColumn(DataframeBuilder builder) {
        return builder.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst().orElse(null);
    }
}