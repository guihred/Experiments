package kibana;

import extract.JsonExtractor;
import extract.WhoIsScanner;
import java.io.File;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.ExplorerHelper;
import org.apache.commons.lang3.StringUtils;
import utils.DateFormatUtils;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;

public class AcessosVolumetricos {
    public static void main(String[] args) {
        ExtractUtils.insertProxyConfig();
        getVolumetria("destinationQuery.json", "destination");
        getVolumetria("sourceQuery.json", "source");

    }

    private static String getTipoTrafego(DataframeML build, Object[] ip) {
        Map<String, Object> findFirst = build.findFirst("IP", e -> Objects.equals(e, ip[0]));
        if (findFirst != null && findFirst.containsKey("Tráfego esperado")) {
            Object object = findFirst.get("Tráfego esperado");
            String[] split = Objects.toString(object, "").split("[^\\d\\.]+");
            DoubleSummaryStatistics stats = Stream.of(split).mapToDouble(StringSigaUtils::toLong).summaryStatistics();

            Double a = StringSigaUtils.toDouble(ip[1]);
            return stats.getMax() < a ? "Acima" : "Esperado";
        }
        return "acima";
    }

    private static void getVolumetria(String queryFile, String result) {
        List<Map<String, String>> destinationSearch =
                JsonExtractor.remap(KibanaApi.makeKibanaSearch(queryFile, "*", 1, "key", "value"));
        DataframeML dataframeML = new DataframeML();
        destinationSearch.forEach(e -> dataframeML.add(e));

        WhoIsScanner whoIsScanner = new WhoIsScanner();
        dataframeML.map("IP", "key", e -> e);
        dataframeML.map("Hostname", "key", e -> {
            String ip = Objects.toString(e, "");
            Map<String, String> ipInformation = whoIsScanner.getIpInformation(ip);
            String key = ExplorerHelper.getKey(ipInformation, "as_owner", "Nome", "HostName", "Descrição", "asname");
            if (!key.equals(ip)) {
                return key;
            }
            return ExplorerHelper.getKey(ipInformation, "as_owner", "Nome", "Descrição", "asname");
        });

        dataframeML.map("Tráfego", "value", e -> StringSigaUtils.getFileSize(StringSigaUtils.toLong(e)));
        DataframeML build = DataframeBuilder.build(new File(
                "C:\\Users\\guigu\\OneDrive - Dataprev\\Acessos volumétricos - Firewall - IPs recorrentes.xlsx"));
        build.map("IP", StringSigaUtils::formatIP);
        DataframeUtils.crossFeatureObject(dataframeML, "Tipo de Tráfego", ip -> getTipoTrafego(build, ip), "key",
                "Tráfego");
        DataframeUtils.crossFeatureObject(dataframeML, "Tráfego esperado", ip -> {
            Map<String, Object> findFirst = build.findFirst("IP", e -> Objects.equals(e, ip[0]));
            return ExplorerHelper.getKey(findFirst, "Tráfego esperado");
        }, "key", "Tráfego");
        DataframeUtils.crossFeatureObject(dataframeML, "motivação",
                ip -> !StringUtils.equalsIgnoreCase("Acima", Objects.toString(ip[1])) ? ""
                        : KibanaApi.scanByIp(ip[0].toString(), 1).get("Ports").get(),
                "IP", "Tipo de Tráfego");

        DataframeUtils.save(dataframeML,
                ResourceFXUtils.getOutFile("csv/" + result + DateFormatUtils.currentTime("ddMMyyyy") + ".csv"));
        HasLogging.log().info("{}", DataframeUtils.toString(dataframeML));
    }

}
