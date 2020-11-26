package fxtests;

import static kibana.ConsultasInvestigator.IGNORE_IPS_REGEX;
import static utils.StringSigaUtils.toDouble;

import ethical.hacker.ReportApplication;
import ethical.hacker.WhoIsScanner;
import java.util.*;
import java.util.stream.Collectors;
import kibana.ConsultasInvestigator;
import kibana.KibanaApi;
import kibana.QueryObjects;
import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.ImageFXUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {

    private List<String> excludeOwners = Arrays.asList("CAIXA ECONOMICA FEDERAL",
            "SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO", "BANCO DO BRASIL S.A.", "Itau Unibanco S.A.");

    @Test
    public void testAutomatedSearch() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br", "cadastro-cat.inss.gov.br");
        for (QueryObjects queryObjects : queryList) {
            if (queryObjects.getLineChart() == null) {
                for (String string : asList) {
                    filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    List<Double> collect =
                            makeKibanaQuery.stream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                                    .map(m -> getNumber(numberCol, m)).collect(Collectors.toList());
                    DoubleSummaryStatistics summaryStatistics =
                            collect.stream().mapToDouble(e -> e).summaryStatistics();
                    if (summaryStatistics.getCount() <= 1 || summaryStatistics.getSum() == 0) {
                        continue;
                    }
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .45;
                    WhoIsScanner whoIsScanner = new WhoIsScanner();
                    String collect2 =
                            makeKibanaQuery.parallelStream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                                    .filter(m -> getNumber(numberCol, m) > avg + range).map(e -> {
                                        String field = getFirst(params, e);
                                        if (field.matches(WhoIsScanner.IP_REGEX)) {

                                            Map<String, String> ipInformation = whoIsScanner.getIpInformation(field);
                                            ipInformation.remove("last_analysis_stats");
                                            ipInformation.remove("malicious");
                                            e.putAll(ipInformation);
                                        }
                                        return e;
                                    }).filter(m -> !excludeOwners.contains(m.getOrDefault("as_owner", "")))
                                    .filter(m -> {
                                        String field = getFirst(params, m);
                                        return isNotBlocked(days, field);
                                    }).map(e -> "\t" + e.values().stream().collect(Collectors.joining("\t")))
                                    .collect(Collectors.joining("\n"));
                    if (StringUtils.isNotBlank(collect2)) {
                        getLogger().info("\n\t{}\n\t{}\n{}", string, query, collect2);
                    }
                }
            }
        }
    }

    @Test
    public void testAutomatedSearchNetwork() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br", "cadastro-cat.inss.gov.br");
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        for (QueryObjects queryObjects : queryList) {
            for (String string : asList) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
                if (queryObjects.getLineChart() == null
                        && QueryObjects.CLIENT_IP_QUERY.equals(queryObjects.getQuery())) {
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    List<Map<String, String>> whoIsInfo = makeKibanaQuery.parallelStream()
                            .filter(m -> !getFirst(params, m).matches(ConsultasInvestigator.IGNORE_IPS_REGEX))
                            .map(e -> {
                                Map<String, String> ipInformation = whoIsScanner.getIpInformation(getFirst(params, e));
                                ipInformation.remove("last_analysis_stats");
                                ipInformation.remove("malicious");
                                e.putAll(ipInformation);
                                return e;
                            }).collect(Collectors.toList());
                    Map<String,
                            Double> collect = whoIsInfo.stream()
                                    .collect(Collectors.groupingBy(
                                            m -> WhoIsScanner.getKey(m, "as_owner", "") + "\t"
                                                    + WhoIsScanner.getKey(m, "network", "id"),
                                            Collectors.summingDouble(m -> getNumber(numberCol, m))));
                    DoubleSummaryStatistics summaryStatistics =
                            collect.values().stream().mapToDouble(e -> e).summaryStatistics();
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .40;
                    String collect3 = collect.entrySet().stream().filter(m -> m.getValue() > avg + range)
                            .filter(m -> excludeOwners.stream().noneMatch(ow -> m.getKey().startsWith(ow)))
                            .map(s -> "\t" + s).collect(Collectors.joining("\n"));
                    if (StringUtils.isNotBlank(collect3)) {
                        getLogger().info("\n\tTOP NETWORKS\n\t{}\n\t{}\n{}", string, query, collect3);
                    }
                }
            }
        }
    }

    @Test
    public void testWordReport() {
        ImageFXUtils.setShowImage(false);
        ReportApplication show2 = show(ReportApplication.class);
        show2.setIp("177.9.205.246");
        show2.makeReportConsultas();
    }

    @Test
    public void testWordReportGeridCredenciais() {
        String finalIP = "187.46.91.147";
        measureTime("KibanaApi.getGeridCredencial", () -> KibanaApi.getGeridCredencial(finalIP));
    }


    private static String getFirst(String[] params, Map<String, String> m) {
        return m.getOrDefault(params[0], m.values().iterator().next());
    }

    private static Double getNumber(String numberCol, Map<String, String> m) {
        return toDouble(m.getOrDefault(numberCol, m.get(numberCol + 0)));
    }

    private static boolean isNotBlocked(Integer days, String ip) {
        if (ip.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> blocked = KibanaApi.makeKibanaSearch("policiesQuery.json", ip, days, "key");
            if (blocked.values().stream().anyMatch(s -> s.contains("block"))) {
                return false;
            }
        }
        return true;
    }

}