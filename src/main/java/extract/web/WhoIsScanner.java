package extract.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class WhoIsScanner {
    public static final String REVERSE_DNS = "HostName";

    private static final String SANS_API_URL = "http://isc.sans.edu/api/ip/";

    public static final String IP_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";

    private static final Logger LOG = HasLogging.log();
    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> cache = new HashMap<>();

    private Map<String, DataframeML> dataframeLookup = new HashMap<>();

    public Map<String, String> getGeoIpInformation(String ip) {
        Map<String, String> ipInformation = WhoIsScanner.getGeoIpInformation(this, ip);
        ipInformation.putIfAbsent("id", ip);
        LOG.info("{}", ipInformation);
        return ipInformation;
    }

    public Map<String, String> getIpInformation(String ip) {
        Map<String, String> ipInformation = WhoIsScanner.getIpInformation(this, ip);
        ipInformation.putIfAbsent("id", ip);
        LOG.info("{}", ipInformation);
        return ipInformation;
    }

    public String reverseDNS(String ip) {
        return cache.computeIfAbsent(ip, CIDRUtils::getReverseDNS);
    }

    public ObservableList<Map<String, String>> scanIps(String ip) {
        ObservableList<Map<String, String>> observableArrayList = FXCollections.observableArrayList();
        String[] ips = ip.split("[\\s,;]+");
        RunnableEx.runNewThread(() -> {
            for (String string : ips) {
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
        File outFile = ResourceFXUtils.getOutFile("xml/" + ip + ".xml");
        if (outFile.exists()) {
            return Jsoup.parse(outFile, StandardCharsets.UTF_8.name());
        }
        String scanIP = SANS_API_URL + ip;
        Document document = JsoupUtils.getDocument(scanIP, cookies);
        Files.write(outFile.toPath(), Arrays.asList(document.outerHtml()), StandardCharsets.UTF_8);
        return document;
    }

    private static Map<String, String> addDescricao(Map<String, String> internalScan) {
        internalScan.put("Descrição", internalScan.values().stream().map(Objects::toString)
                .filter(StringUtils::isNotBlank).map(String::trim).distinct().collect(Collectors.joining(" - ")));
        return internalScan;
    }

    private static Map<String, String> getGeoIpInformation(WhoIsScanner whoIsScanner, String name) {
        String ip = !name.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/*\\d*$")
                ? SupplierEx.getIgnore(() -> CIDRUtils.toIPByName(name).getHostAddress(), name)
                : name;
        if (CIDRUtils.isPrivateNetwork(ip) || ip.matches("^200\\.152\\..+")) {
            return lookupInternalInfo(whoIsScanner, ip);
        }
        return SupplierEx.getFirst(() -> addDescricao(IpStackApi.getIPGeoInformation(ip)),
                () -> CIDRUtils.findNetwork(ip), () -> whoIsScanner.whoIsScan(ip),
                () -> VirusTotalApi.getIpTotalInfo(ip));
    }

    private static Map<String, String> getIpInformation(WhoIsScanner whoIsScanner, String name) {
        String ip = !name.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/*\\d*$")
                ? SupplierEx.getIgnore(() -> CIDRUtils.toIPByName(name).getHostAddress(), name)
                : name;
        if (CIDRUtils.isPrivateNetwork(ip) || ip.matches("^200\\.152\\..+")) {
            return lookupInternalInfo(whoIsScanner, ip);
        }
        return SupplierEx.getFirst(() -> CIDRUtils.findNetwork(ip), () -> whoIsScanner.whoIsScan(ip),
                () -> VirusTotalApi.getIpTotalInfo(ip));
    }

    private static Map<String, String> lookupInternalInfo(WhoIsScanner whoIsScanner, String ip) {
        String reverseDNS = whoIsScanner.reverseDNS(ip);
        Map<String, String> internalScan = SupplierEx.getFirst(() -> lookupSDM(whoIsScanner, ip, reverseDNS), () -> {
            DataframeML networksFile =
                    whoIsScanner.dataframeLookup.computeIfAbsent("networks/redes3.csv", DataframeBuilder::build);
            return CIDRUtils.strMap(CIDRUtils.searchInFile(networksFile, "Sub-Rede", ip));
        }, () -> {
            DataframeML networksFile =
                    whoIsScanner.dataframeLookup.computeIfAbsent("networks/redes2.csv", DataframeBuilder::build);
            return CIDRUtils.strMap(CIDRUtils.searchInFile(networksFile, "network", ip));
        }, () -> {
            DataframeML networksFile =
                    whoIsScanner.dataframeLookup.computeIfAbsent("networks/redes1.csv", DataframeBuilder::build);
            return CIDRUtils.strMap(CIDRUtils.searchInFile(networksFile, "network", ip));
        }, LinkedHashMap::new);
        addDescricao(internalScan);

        internalScan.put(REVERSE_DNS, reverseDNS);
        return internalScan;
    }

    private static Map<String, String> lookupSDM(WhoIsScanner whoIsScanner, String ip, String reverseDNS) {
        DataframeML networksFile =
                whoIsScanner.dataframeLookup.computeIfAbsent("networks/SDMResources.csv", DataframeBuilder::build);
        Map<String,
                Object> first =
                        SupplierEx.getFirst(
                                () -> networksFile.findFirst("IP0",
                                        v -> Objects.equals(v, ip)
                                                || StringUtils.equalsIgnoreCase(Objects.toString(v), ip)),
                                () -> networksFile.findFirst("ID do IC alternativo",
                                        v -> Objects.equals(v, ip) || Objects.equals(v, reverseDNS)),
                                () -> networksFile.findFirst("IP1", v -> Objects.equals(v, ip)),
                                () -> networksFile.findFirst("IP2", v -> Objects.equals(v, ip)));
        return CIDRUtils.strMap(first);
    }

}