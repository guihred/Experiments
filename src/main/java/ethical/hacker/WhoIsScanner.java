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
                .map(Path::toFile).orElse(ResourceFXUtils.getOutFile(screenshotsFolder + url + ".html"));
        Document renderPage;
        if (!htmlFile.exists() || containsWait(htmlFile)) {
            File pngFile = ResourceFXUtils.getOutFile(screenshotsFolder + url + ".png");
            renderPage = ExtractUtils.renderPage(
                    "https://www.ssllabs.com/ssltest/analyze.html?d=" + url + "&ignoreMismatch=on&latest", pngFile,
                    "Please wait...");
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
        for (int i = 0; i < asList.size(); i++) {
            String url = asList.get(i);
            LOG.info("SCANNING {} -- {}/{}", url, i, asList.size());
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