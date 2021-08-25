package kibana;

import static kibana.KibanaApi.IP_REGEX;

import com.google.common.io.Files;
import extract.web.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class CredentialInvestigator extends KibanaInvestigator {

    private static final String ACESSO = ProjectProperties.getField();
    private static final String ACESSO_GWDC = ProjectProperties.getField();
    private static final Logger LOG = HasLogging.log();
    private static final Map<String, String> COOKIES = new LinkedHashMap<>();
    private static DataframeML dataframeLookup;
    @FXML
    private Pane pane;
    private ComboBox<String> indexCombo;

    @Override
    public void initialize() {
        ExtractUtils.addAuthorizationConfig();
        SimpleTableViewBuilder.of(commonTable).items(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)))
                .copiable().savable().deletable().multipleSelection()
                .onSortClicked((c, a) -> QuickSortML.sortMapList(items, c, a));

        indexCombo = new SimpleComboBoxBuilder<>(Arrays.asList("inss-*-prod-*", "mte-log4j-prod-*")).select(0).build();
        pane.getChildren().add(2, SimpleVBoxBuilder.newVBox("Index", indexCombo));
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable()
                .pasteable(s -> StringSigaUtils.getMatches(s,
                        "(" + IP_REGEX + "|\\d{11}|[\\w\\.]+@[\\w\\.]+|[\\w]+\\.[\\w]+)"))
                .onDoubleClick(resultsFilter::setText);
        RunnableEx.runNewThread(CredentialInvestigator::getCookies);
    }

    @Override
    public void onActionKibanaScan() {

        if (COOKIES.isEmpty()) {
            RunnableEx.run(CredentialInvestigator::getCookies);
        }
        super.onActionKibanaScan();
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        primaryStage.setTitle("Credential Investigator");
        RunnableEx.runNewThread(() -> {
            File outFile = ResourceFXUtils.getOutFile("csv/paloAlto.csv");
            final int oneDay = 24;
            if (JsonExtractor.isRecentFile(outFile, oneDay)) {
                setDataframeLookup(outFile, progressIndicator.progressProperty());
            }
        });

    }

    @Override
    protected Map<String, String> executeCall(String query, DoubleProperty progress, List<String> cols) {
        Integer d = days.getSelectionModel().getSelectedItem();
        String index = indexCombo.getSelectionModel().getSelectedItem();
        CommonsFX.update(progress, 0);
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, SupplierEx<String>> scanByIp = scanCredentials(query, d, index, result);

        scanByIp.forEach((k, v) -> {
            result.put(k, SupplierEx.get(v, ""));
            CommonsFX.addProgress(progress, 1. / scanByIp.size());
        });
        CommonsFX.update(progress, 1);

        return result;
    }

    public static String credentialInfo(String credencial) {
        List<String> include = Arrays.asList("search", "mail", "l", "rgUf", "memberOf", "pwdChangedTime");
        return credentialInfo(credencial, include);
    }
    public static String credentialInfo(String credencial, List<String> include) {
        if (StringUtils.isBlank(credencial)) {
            return "";
        }

        return SupplierEx.get(() -> Stream.of(credencial.split("\n")).filter(StringUtils::isNotBlank)
                .map(c -> FunctionEx.apply(s -> getCredentialInfo(s, include), c, new SimpleMap("search", c)))
                .map(m -> m.values().stream().distinct().map(StringUtils::trim).collect(Collectors.joining(" - ")))
                .map(Objects::toString).distinct().collect(Collectors.joining("\n")), "");
    }

    public static Map<String, String> getCredentialInfo(String credencial, List<String> include) throws IOException {
        File outFile = ResourceFXUtils.getOutFile("html/" + credencial + ".html");
        Document document = getCachedDocument(credencial, outFile);
        String memberOf = "memberOf";

        return document.select("input").stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.attr("id"), Objects.toString(e.attr("value"), "").trim()))
                .filter(e -> StringUtils.isNotBlank(e.getValue())).filter(e -> StringUtils.isNotBlank(e.getKey()))
                .filter(e -> include.contains(e.getKey()))
                .filter(e -> !memberOf.equals(e.getKey()) || e.getValue().contains("vpn")).distinct().peek(e -> {
                    if (memberOf.equals(e.getKey())) {
                        e.setValue(StringSigaUtils.getMatches(e.getValue(), "([\\-\\w]+vpn[\\-\\w]+)"));
                    }
                }).collect(Collectors.groupingBy(Entry<String, String>::getKey, LinkedHashMap::new,
                        Collectors.mapping(Entry<String, String>::getValue, Collectors.joining(" - "))));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Document getCachedDocument(String credencial, File outFile) throws IOException {
        if (outFile.exists()) {
            return JsoupUtils.normalParse(outFile);
        }
        if (COOKIES.isEmpty()) {
            RunnableEx.run(CredentialInvestigator::getCookies);
        }
        return SupplierEx.get(() -> {
            Document doc = JsoupUtils.getDocument(ACESSO_GWDC + "?action=search&object=personUser&filter=" + credencial,
                    COOKIES);
            Files.write(doc.toString(), outFile, StandardCharsets.UTF_8);
            return doc;
        });
    }

    private static void getCookies() throws IOException {
        if (COOKIES.isEmpty()) {
            File outFile = ResourceFXUtils.getOutFile("html/test.html");
            RunnableEx.make(() -> JsoupUtils.getDocument(ACESSO_GWDC, COOKIES), e -> {
                InstallCert.installCertificate(ACESSO);
                JsoupUtils.getDocument(ACESSO_GWDC, COOKIES);
            }).run();
            String cookiesString = COOKIES.entrySet().stream().map(Objects::toString).collect(Collectors.joining("; "));
            Map<String, String> headers = getHeaders();
            headers.put("Cookie", cookiesString);
            Map<String, String> postContent =
                    PhantomJSUtils.postContent(ACESSO_GWDC + "?action=&next_action=&object=&filter=",
                            "uid=" + ExtractUtils.getHTTPUsername() + "&password=" + ExtractUtils.getHTTPPassword(),
                            ContentType.APPLICATION_FORM_URLENCODED, headers, outFile);
            String string = postContent.get("Location");
            JsoupUtils.getDocument(ACESSO_GWDC + string, COOKIES);
            LOG.info("Cookies Acquired");
        }
    }

    private static Map<String, String> getHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", JsoupUtils.USER_AGENT);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Origin", ACESSO);
        headers.put("DNT", "1");

        headers.put("Connection", "keep-alive");
        headers.put("Referer", ACESSO_GWDC);
        headers.put("Upgrade-Insecure-Requests", "1");
        return headers;
    }

    private static Map<String, Object> lookupDisplay(Map<String, Object> m) {
        return m.entrySet().stream()
                .filter(e -> e.getKey().matches("Receive Time|client_os|srcregion|private_ip|Source User|public_ip"))
                .collect(Collectors.toMap(Entry<String, Object>::getKey, Entry<String, Object>::getValue));
    }

    private static Map<String, SupplierEx<String>> scanCredentials(String query, Integer days1, String index,
            Map<String, String> result) {
        Map<String, SupplierEx<String>> scanByIp = new LinkedHashMap<>();
        scanByIp.put("IP", () -> {
            if (query.matches(IP_REGEX)) {
                return query;
            }
            Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(query, index, days1);
            return iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
        });
        scanByIp.put("GeoIP", () -> KibanaApi.geoLocation(result.getOrDefault("IP", "")));
        scanByIp.put("Credencial", () -> {
            if (!query.matches(IP_REGEX)) {
                return query;
            }
            if (!index.contains("*")) {
                return KibanaApi.scanByIp(query, days1).get("Top Users").get();
            }
            Map<String, String> iPsByCredencial = KibanaApi.getGeridCredencial(query, index, days1);
            return iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
        });
        List<String> include = Arrays.asList("search", "mail", "l", "rgUf", "memberOf", "pwdChangedTime");

        scanByIp.put("Credencial Info", () -> credentialInfo(result.getOrDefault("Credencial", ""), include));
        List<String> include2 = Arrays.asList("search", "cn", "cpf", "accountStatus", "l", "rgUf");
        scanByIp.put("Extra Info", () -> credentialInfo(result.getOrDefault("Credencial", ""), include2));
        scanByIp.put("Login", () -> KibanaApi.getLoginTimeCredencial(query, index, days1));
        scanByIp.put("Message", () -> {
            if (query.matches(IP_REGEX)) {
                Map<String, String> iPsByCredencial = KibanaApi.getGeridCredencial(query, index, days1);
                return iPsByCredencial.values().stream().map(String::trim).collect(Collectors.joining("\n"));
            }
            Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(query, index, days1);
            return iPsByCredencial.values().stream().map(String::trim).collect(Collectors.joining("\n"));
        });
        scanPaloAlto(query, result, scanByIp);
        return scanByIp;
    }

    private static void scanPaloAlto(String query, Map<String, String> result,
            Map<String, SupplierEx<String>> scanByIp) {
        File outFile = ResourceFXUtils.getOutFile("csv/paloAlto.csv");
        final int oneDay = 24;
        if (!JsonExtractor.isRecentFile(outFile, oneDay)) {
            return;
        }
        scanByIp.put("Lookup", () -> {
            String searchByIp = Stream.of(result.getOrDefault("IP", "").split("\n")).map(ip -> {
                String header = !CIDRUtils.isPrivateNetwork(ip) ? "public_ip" : "private_ip";
                List<Map<String, Object>> findFirst =
                        dataframeLookup.findAll(header, s -> StringUtils.equals(query, Objects.toString(s, "")));
                return findFirst.stream().map(CredentialInvestigator::lookupDisplay).map(KibanaApi::display)
                        .collect(Collectors.joining("\n"));
            }).collect(Collectors.joining("\n"));
            if (StringUtils.isNotBlank(searchByIp)) {
                return searchByIp;
            }
            String orDefault = result.getOrDefault("Credencial Info", "");
            List<String> matches = StringSigaUtils.matches(orDefault, "([a-z\\.]+@[a-z\\.]+)");
            List<Map<String, Object>> findFirst = dataframeLookup.findAll("Source User", matches::contains);
            return findFirst.stream().map(CredentialInvestigator::lookupDisplay).map(KibanaApi::display)
                    .collect(Collectors.joining("\n"));
        });
    }

    private static void setDataframeLookup(File outFile, DoubleProperty doubleProperty) {
        dataframeLookup = SupplierEx.orElse(dataframeLookup,
                () -> DataframeBuilder.builder(outFile).build(doubleProperty));
    }

}
