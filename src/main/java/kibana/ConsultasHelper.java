package kibana;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static utils.StringSigaUtils.toDouble;

import extract.web.CIDRUtils;
import extract.web.WhoIsScanner;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class ConsultasHelper {
    private static final double THRESHOLD_PARAMS = .45;
    private static final double THRESHOLD_NETWORK = .40;
    private static final Logger LOG = HasLogging.log();
    public static final String IGNORE_IPS_REGEX = "::1|127.0.0.1";
    private static final List<String> EXCLUDE_OWNERS =
            Arrays.asList("CAIXA ECONOMICA FEDERAL", "SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO",
                    "Tribunal Regional Federal da Terceira Regiao",
                    "BANCO DO BRASIL S.A.", "Itau Unibanco S.A.", "Google LLC", "BANCO MERCANTIL DO BRASIL S/A");

    private ConsultasHelper() {
    }

    public static void automatedSearch(Collection<QueryObjects> queries, Collection<String> applicationList,
            DoubleProperty progressp, Integer days2, Map<String, String> filter2) {

        CommonsFX.update(progressp, 0);
        Map<String, String> filter1 = new HashMap<>();
        for (String application : applicationList) {
            for (QueryObjects queryObjects : queries) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, application);
                String[] params = queryObjects.getParams();
                String numberCol = params[queryObjects.getParams().length - 1];
                List<Map<String, String>> kibanaQuery = queryObjects.searchRemap(filter1, days2);
                String fieldQuery = queryObjects.getQuery();
                DoubleSummaryStatistics summaryStatistics = getStatistics(params, numberCol, kibanaQuery);
                List<Map<String, String>> aboveAvgInfo =
                        getAboveAvgInfo(summaryStatistics, kibanaQuery, numberCol, params, days2);
                if (!aboveAvgInfo.isEmpty()) {
                    mergeFilter(filter2, params, fieldQuery, aboveAvgInfo);
                    String join = join(aboveAvgInfo);
                    LOG.info("\n\t{}\n\t{}\n{}", application, fieldQuery, join);
                }
                CommonsFX.addProgress(progressp, 1. / applicationList.size() / queries.size());
            }
        }
        CommonsFX.update(progressp, 1);
    }

    public static String fixParam(String fieldQuery, String first) {
        if (QueryObjects.CLIENT_IP_QUERY.equals(fieldQuery) && first.endsWith(".prevnet")) {
            return SupplierEx.get(()->CIDRUtils.toIPByName(first).getHostAddress(),first);
        }
        if (QueryObjects.URL_QUERY.equals(fieldQuery) && first.startsWith("/")) {
            return KibanaApi.getURL(first);
        }
        return first;
    }

    public static String merge(String a, String b) {
        return concat(of(a.split("\n")), of(b.split("\n"))).distinct().sorted().collect(joining("\n"));
    }

    public static void networkSearch(Map<String, String> filter, Collection<QueryObjects> queries,
            Collection<String> applicationList, Integer day, DoubleProperty progress) {
        CommonsFX.update(progress, 0);
        Map<String, String> filter1 = new HashMap<>();
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        for (String application : applicationList) {
            for (QueryObjects queryObjects : queries) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, application);
                String[] params = queryObjects.getParams();
                List<Map<String, String>> kibanaQuery = queryObjects.searchRemap(filter1, day);
                List<Map<String, String>> whoIsInfo = kibanaQuery.parallelStream()
                        .filter(m -> !getFirst(params, m).matches(ConsultasHelper.IGNORE_IPS_REGEX)).map(e -> {
                            e.putAll(whoIsScanner.getIpInformation(getFirst(params, e)));
                            return e;
                        }).collect(Collectors.toList());
                String numberCol = params[queryObjects.getParams().length - 1];
                Map<String,
                        Double> netHistogram = whoIsInfo.stream()
                                .collect(Collectors.groupingBy(
                                        m -> getNameAndNetwork(m),
                                        Collectors.summingDouble(m -> getNumber(numberCol, m))));
                DoubleSummaryStatistics summaryStatistics =
                        netHistogram.values().stream().mapToDouble(e -> e).summaryStatistics();
                double avg = summaryStatistics.getAverage();
                double max = summaryStatistics.getMax();
                double min = summaryStatistics.getMin();
                double range = (max - min) * THRESHOLD_NETWORK;
                List<String> networks = netHistogram.entrySet().stream().filter(m -> m.getValue() > avg + range)
                        .filter(m -> EXCLUDE_OWNERS.stream().noneMatch(ow -> m.getKey().startsWith(ow)))
                        .map(s -> "\t" + s).collect(Collectors.toList());
                if (!networks.isEmpty()) {
                    List<String> nets =
                            networks.stream().map(e -> e.replaceAll(".+?\t(.+)=.+", "$1")).collect(Collectors.toList());
                    String queryField = queryObjects.getQuery();
                    String topNets = networks.stream().collect(Collectors.joining("\t\n"));
                    LOG.info("\n\tTOP NETWORKS\n\t{}\n\t{}\n{}", application, queryField, topNets);
                    List<Map<String, String>> aboveAvgInfo = kibanaQuery.parallelStream()
                            .filter(m -> !getFirst(params, m).matches(ConsultasHelper.IGNORE_IPS_REGEX)).filter(e -> {
                                String first = getFirst(params, e);
                                return nets.stream().anyMatch(net -> CIDRUtils.isSameNetworkAddress(net, first));
                            }).collect(Collectors.toList());
                    mergeFilter(filter, params, queryField, aboveAvgInfo);
                }

                CommonsFX.addProgress(progress, 1. / applicationList.size() / queries.size());
            }
        }
        CommonsFX.update(progress, 1);
    }

    private static Map<String, String> completeInformation(String[] params, WhoIsScanner whoIsScanner,
            Map<String, String> e) {
        String field = getFirst(params, e);
        if (field.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> ipInformation = whoIsScanner.getIpInformation(field);
            ipInformation.remove("last_analysis_stats");
            ipInformation.remove("malicious");
            e.putAll(ipInformation);
        }
        return e;
    }

    private static List<Map<String, String>> getAboveAvgInfo(DoubleSummaryStatistics summaryStatistics,
            List<Map<String, String>> makeKibanaQuery, String numberCol, String[] params, Integer day) {
        if (summaryStatistics.getCount() <= 1 || summaryStatistics.getSum() == 0) {
            return Collections.emptyList();
        }
        double avg = summaryStatistics.getAverage();
        double max = summaryStatistics.getMax();
        double min = summaryStatistics.getMin();
        final double range = (max - min) * THRESHOLD_PARAMS;
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        return makeKibanaQuery.parallelStream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                .filter(m -> getNumber(numberCol, m) > avg + range)
                .map(e -> completeInformation(params, whoIsScanner, e))
                .filter(m -> !EXCLUDE_OWNERS.contains(StringSigaUtils.getKey(m, "as_owner", "asname")))
                .filter(m -> isNotBlocked(day, getFirst(params, m))).collect(toList());
    }

    private static String getFirst(String[] params, Map<String, String> m) {
        return m.getOrDefault(params[0], m.values().iterator().next());
    }

    private static String getNameAndNetwork(Map<String, String> m) {
        return StringSigaUtils.getKey(m, "Descrição", "as_owner", "asname") + "\t"
                + StringSigaUtils.getKey(m, "network", "id");
    }

    private static Double getNumber(String numberCol, Map<String, String> m) {
        return toDouble(m.getOrDefault(numberCol, m.get(numberCol + 0)));
    }

    private static DoubleSummaryStatistics getStatistics(String[] params, String numberCol,
            List<Map<String, String>> makeKibanaQuery) {
        return makeKibanaQuery.stream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                .mapToDouble(m -> getNumber(numberCol, m)).summaryStatistics();
    }

    private static boolean isNotBlocked(Integer days, String ip) {
        if (ip.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> blocked = KibanaApi.makeKibanaSearch("policiesQuery.json", days, ip, "key");
            return blocked.values().stream().noneMatch(s -> s.contains("block"));
        }
        return true;
    }

    private static String join(List<Map<String, String>> collect) {
        return collect.stream().map(e -> "\t" + e.values().stream().collect(joining("\t"))).collect(joining("\n"));
    }

    private static void mergeFilter(Map<String, String> filter, String[] params, String fieldQuery,
            List<Map<String, String>> aboveAvgInfo) {
        CommonsFX.runInPlatform(() -> {
            for (Map<String, String> map : aboveAvgInfo) {
                String first = fixParam(fieldQuery, getFirst(params, map));
                filter.merge(fieldQuery, first, ConsultasHelper::merge);
            }
        });
    }

}
