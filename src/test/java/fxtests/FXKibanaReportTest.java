package fxtests;

import java.util.Map;
import java.util.stream.Collectors;
import kibana.ConsultasInvestigator;
import kibana.KibanaApi;
import kibana.ReportApplication;
import kibana.ReportHelper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.ImageFXUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {

    @Test
    public void testAutomatedSearch() {
        show(ConsultasInvestigator.class).makeAutomatedSearch();
    }

    @Test
    public void testAutomatedSearchNetwork() {
        show(ConsultasInvestigator.class).makeAutomatedNetworkSearch();
    }

    @Test
    public void testWordReport() {
        ImageFXUtils.setShowImage(false);
        ReportApplication show2 = show(ReportApplication.class);
        // show2.setIp("177.9.205.246");
        show2.makeReportConsultas();
    }

    @Test
    public void testWordReportGeridCredenciais() {
        String finalIP = "200.193.192.134";
        measureTime("KibanaApi.getGeridCredencial", () -> {
            Map<String, String> geridCredencial = KibanaApi.getGeridCredencial(finalIP, "inss-*-prod-*");
            geridCredencial.values().stream().map(ReportHelper::textToImage).collect(Collectors.toList());
            getLogger().info("{}", geridCredencial.keySet());
            return geridCredencial;
        });
    }

}