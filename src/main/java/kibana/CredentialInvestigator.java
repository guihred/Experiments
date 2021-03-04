package kibana;

import com.google.common.io.Files;
import extract.web.JsoupUtils;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
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

    private static final Logger LOG = HasLogging.log();
    private static final String IP_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    private static Map<String, String> cookies = new LinkedHashMap<>();
    @FXML
    private Pane pane;
    private ComboBox<String> indexCombo;

    @Override
    public void initialize() {
        ExtractUtils.insertProxyConfig();
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(commonTable).copiable().savable()
                .onSortClicked((c, a) -> QuickSortML.sortMapList(items, c, a));

        indexCombo = new SimpleComboBoxBuilder<>(Arrays.asList("inss-*-prod-*", "mte-log4j-prod-*")).select(0).build();
        pane.getChildren().add(2, SimpleVBoxBuilder.newVBox("Index", indexCombo));
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable()
                .pasteable(s -> StringSigaUtils.getMatches(s, "(" + IP_REGEX + "|\\d{11})"));
        RunnableEx.runNewThread(() -> getCookies());
    }

    @Override
    public void onActionKibanaScan() {

        if (cookies.isEmpty()) {
            RunnableEx.run(() -> getCookies());
        }
        super.onActionKibanaScan();
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        primaryStage.setTitle("Credential Investigator");
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
        if (StringUtils.isBlank(credencial)) {
            return "";
        }
        return SupplierEx.get(() -> Stream.of(credencial.split("\n")).filter(StringUtils::isNotBlank)
                .map(FunctionEx.makeFunction(CredentialInvestigator::getCredentialInfo)).filter(Objects::nonNull)
                .map(m -> m.values().stream().distinct().map(StringUtils::trim).collect(Collectors.joining(" - ")))
                .map(Objects::toString).distinct().collect(Collectors.joining("\n")), "");
    }

    public static Map<String, String> getCredentialInfo(String credencial) throws IOException {
        File outFile = ResourceFXUtils.getOutFile("html/" + credencial + ".html");
        Document document;
        if (!outFile.exists()) {
            if (cookies.isEmpty()) {
                RunnableEx.run(() -> getCookies());
            }
            document = JsoupUtils.getDocument(
                    "https://www-acesso/gwdc/?action=search&object=personUser&filter=" + credencial, cookies);
            Files.write(document.toString(), outFile, StandardCharsets.UTF_8);
        } else {
            document = JsoupUtils.normalParse(outFile);
        }
        List<String> include = Arrays.asList("search", "mail", "l", "rgUf");

        Map<String, String> info = document.select("input").stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.attr("id"), Objects.toString(e.attr("value"), "").trim()))
                .filter(e -> StringUtils.isNotBlank(e.getValue())).filter(e -> StringUtils.isNotBlank(e.getKey()))
                .filter(e -> include.contains(e.getKey())).distinct().collect(Collectors.groupingBy(e -> e.getKey(),
                        LinkedHashMap::new, Collectors.mapping(e -> e.getValue(), Collectors.joining(" - "))));
        LOG.info("{} {}", credencial, info);
        return info;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void getCookies() throws IOException {
        File outFile = ResourceFXUtils.getOutFile("html/test.html");
        if (cookies.isEmpty()) {
            JsoupUtils.getDocument("https://www-acesso/gwdc/", cookies);
            String collect = cookies.entrySet().stream().map(Objects::toString).collect(Collectors.joining("; "));
            Map<String, String> headers = getHeaders();
            headers.put("Cookie", collect);
            Map<String, String> postContent =
                    PhantomJSUtils.postContent("https://www-acesso/gwdc/?action=&next_action=&object=&filter=",
                            "uid=" + ExtractUtils.getHTTPUsername() + "&password=" + ExtractUtils.getHTTPPassword(),
                            ContentType.APPLICATION_FORM_URLENCODED, headers, outFile);
            String string = postContent.get("Location");
            System.out.println(postContent);
            JsoupUtils.getDocument("https://www-acesso/gwdc/" + string, cookies);
        }
    }

    private static Map<String, String> getHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Origin", "https://www-acesso");
        headers.put("DNT", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Referer", "https://www-acesso/gwdc/");
        headers.put("Upgrade-Insecure-Requests", "1");
        return headers;
    }

    private static Map<String, SupplierEx<String>> scanCredentials(String query, Integer days, String index,
            Map<String, String> result) {
        Map<String, SupplierEx<String>> scanByIp = new LinkedHashMap<>();
        scanByIp.put("IP", () -> {
            if (query.matches(IP_REGEX)) {
                return query;
            }
            Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(query, index, days);
            return iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
        });
        scanByIp.put("GeoIP", () -> KibanaApi.geoLocation(result.getOrDefault("IP", "")));
        scanByIp.put("Credencial", () -> {
            if (query.matches(IP_REGEX)) {
                Map<String, String> iPsByCredencial = KibanaApi.getGeridCredencial(query, index, days);
                return iPsByCredencial.keySet().stream().collect(Collectors.joining("\n"));
            }
            return query;
        });
        scanByIp.put("Credencial Info", () -> credentialInfo(result.getOrDefault("Credencial", "")));
        scanByIp.put("Login", () -> KibanaApi.getLoginTimeCredencial(query, index, days));
        scanByIp.put("Message", () -> {
            if (query.matches(IP_REGEX)) {
                Map<String, String> iPsByCredencial = KibanaApi.getGeridCredencial(query, index, days);
                return iPsByCredencial.values().stream().map(s -> s.trim()).collect(Collectors.joining("\n"));
            }
            Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(query, index, days);
            return iPsByCredencial.values().stream().map(s -> s.trim()).collect(Collectors.joining("\n"));
        });
        return scanByIp;
    }

}
