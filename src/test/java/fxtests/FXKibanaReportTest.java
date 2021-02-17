package fxtests;

import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import kibana.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.ExcelService;
import utils.ExtractUtils;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {
    @Test
    public void testAcessosVolumetricos() {
        measureTime("AcessosVolumetricos", () -> {
            ExtractUtils.insertProxyConfig();
            AcessosVolumetricos.getVolumetria("destinationQuery.json", "destination");
            AcessosVolumetricos.getVolumetria("sourceQuery.json", "source");
        });
    }

    @Test
    public void testAutomatedSearch() {
        show(ConsultasInvestigator.class).makeAutomatedSearch();
    }

    @Test
    public void testAutomatedSearchNetwork() {
        show(ConsultasInvestigator.class).makeAutomatedNetworkSearch();
    }

    @Test
    public void testConsultasInvestigator() {
        ImageFXUtils.setShowImage(false);
        show(ConsultasInvestigator.class);
        ProgressIndicator lookup = lookupFirst(ProgressIndicator.class);
        List<Button> buttons = lookupList(Button.class).stream().limit(4).collect(Collectors.toList());
        for (Node e1 : buttons) {
            clickOn(e1);
            waitProgress(lookup);
        }
        waitProgress(lookup);
        Set<Node> queryAll2 = lookup(".tab").queryAll();
        for (Node node : queryAll2) {
            tryClickOn(node);
            Set<Node> queryAs =
                    lookup(".tab-content-area").queryAll().stream().filter(Node::isVisible).collect(Collectors.toSet());
            from(queryAs).lookup(TableRow.class::isInstance).queryAll().stream().limit(1).forEach(this::doubleClickOn);
            waitProgress(lookup);
        }
    }

    @Test
    public void testKibanaApi() {
        measureTime("KibanaApi.kibanaFullScan", () -> KibanaApi.kibanaFullScan("187.22.201.244", 1));
    }

    @Test
    public void testKibanaInvestigator() {
        interactNoWait(() -> ImageFXUtils.setClipboardContent("177.37.183.109"));
        show(KibanaInvestigator.class);
        clickOn("#filterList");
        holding(KeyCode.CONTROL, () -> type(KeyCode.V));
        holding(KeyCode.CONTROL, () -> type(KeyCode.V));
        holding(KeyCode.CONTROL, () -> type(KeyCode.V));
        clickButtonsWait();
        waitProgress(lookupFirst(ProgressIndicator.class));
    }

    @Test
    public void testLoadKibanaApi() {
        measureTime("ExcelService.getExcel", () -> ExcelService
                .getExcel(ResourceFXUtils.toFile("networks/Lista de IP da Caixa.xlsx"), (List<Object> l) -> {
                    if (l.isEmpty()) {
                        return null;
                    }
                    long count = l.stream().filter(Objects::nonNull).count();
                    if (count != 1) {
                        return null;
                    }
                    String string = convert(l.get(0));
                    if (string.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        Map<String, String> kibanaFullScan = KibanaApi.kibanaFullScan(string, 1);
                        List<String> kibanaScanned = kibanaFullScan.values().stream().collect(Collectors.toList());
                        kibanaScanned.add(1, "Guilherme");
                        return kibanaScanned;
                    }
                    return null;
                }, ResourceFXUtils.getOutFile("apiResult2.xlsx")));
    }

    @Test
    public void testTalosIsInBlacklist() {
        measureTime("KibanaApi.isInBlacklist", () -> KibanaApi.isInBlacklist("200.201.175.19"));
    }

    @Test
    public void testTimelionScan() {
        measureTime("TimelionApi.timelionScan", () -> TimelionApi.timelionScan(FXCollections.observableArrayList(),
                TimelionApi.TIMELINE_USERS, new HashMap<>(), "now-1d"));
    }

    @Test
    public void testWordIPbyGeridCredenciais() {
        String credencial = "\\\"179.98.197.200\\\" AND \\\"supplied credentials\\\"";
        measureTime("KibanaApi.getGeridCredencial", () -> {
            Map<String, String> geridCredencial = KibanaApi.getIPsByCredencial(credencial, "inss-*-prod-*", 1);
            geridCredencial.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
            getLogger().info("{}", geridCredencial.keySet());
            return geridCredencial;
        });
    }

    @Test
    public void testWordReport() {
        ImageFXUtils.setShowImage(false);
        ReportApplication show2 = show(ReportApplication.class);
        TextField lookupFirst = lookupFirst(TextField.class);
        interact(() -> lookupFirst.setText("177.9.205.246"));
        show2.makeReportConsultas();
    }

    @Test
    public void testWordReportGeridCredenciais() {
        String finalIP = "179.98.197.200 AND \\\"supplied credentials\\\"";

        measureTime("KibanaApi.getGeridCredencial", () -> {
            Map<String, String> geridCredencial = KibanaApi.getGeridCredencial(finalIP, "inss-*-prod-*", 1);
            getLogger().info("{}", geridCredencial.keySet());
            geridCredencial.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
            return geridCredencial;
        });
    }

    private static String convert(Object o) {
        if (o instanceof Double) {
            long longValue = ((Double) o).longValue();
            return String.format(Locale.getDefault(), "%,d", longValue);
        }
        return Objects.toString(o, "");
    }

}