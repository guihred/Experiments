package kibana;

import extract.web.JsonExtractor;
import extract.web.WhoIsScanner;
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
import org.slf4j.Logger;
import utils.DateFormatUtils;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;

public class AcessosVolumetricos {
    private static final Logger LOG = HasLogging.log();
    private static final String TRAFEGO = "Tráfego";
    private static final String TRAFEGO_ESPERADO = "Tráfego esperado";

    public static void getVolumetria(String queryFile, String result) {
        List<Map<String, String>> destinationSearch =
                JsonExtractor.remap(KibanaApi.makeKibanaSearch(queryFile, "*", 1, "key", "value"));
        DataframeML dataframeML = new DataframeML();
        destinationSearch.forEach(dataframeML::add);

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

        dataframeML.map(TRAFEGO, "value", e -> StringSigaUtils.getFileSize(StringSigaUtils.toLong(e)));
        DataframeML build = DataframeBuilder.build(new File(
                "C:" + "\\Users\\guigu" + "\\OneDrive - Dataprev"
                        + "\\Acessos volumétricos - Firewall - IPs recorrentes.xlsx"));
        build.map("IP", StringSigaUtils::formatIP);
        DataframeUtils.crossFeatureObject(dataframeML, "Tipo de Tráfego", ip -> getTipoTrafego(build, ip), "key",
                TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, TRAFEGO_ESPERADO, ip -> {
            Map<String, Object> findFirst = build.findFirst("IP", e -> Objects.equals(e, ip[0]));
            return ExplorerHelper.getKey(findFirst, TRAFEGO_ESPERADO);
        }, "key", TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, "motivação",
                ip -> !StringUtils.equalsIgnoreCase("Acima", Objects.toString(ip[1])) ? ""
                        : KibanaApi.scanByIp(ip[0].toString(), 1).get("Ports").get(),
                "IP", "Tipo de Tráfego");

        DataframeUtils.save(dataframeML,
                ResourceFXUtils.getOutFile("csv/" + result + DateFormatUtils.currentTime("ddMMyyyy") + ".csv"));
        String string = DataframeUtils.toString(dataframeML);
        LOG.info("{}", string);
    }

    public static void main(String[] args) {
        ExtractUtils.insertProxyConfig();
        getVolumetria("sourceQuery.json", "source");
        getVolumetria("destinationQuery.json", "destination");
    }

    private static String getTipoTrafego(DataframeML build, Object[] ip) {
        Map<String, Object> findFirst = build.findFirst("IP", e -> Objects.equals(e, ip[0]));
        if (findFirst != null && findFirst.containsKey(TRAFEGO_ESPERADO)) {
            Object object = findFirst.get(TRAFEGO_ESPERADO);
            String[] split = Objects.toString(object, "").split("[^\\d\\.]+");
            DoubleSummaryStatistics stats = Stream.of(split).mapToDouble(StringSigaUtils::toLong).summaryStatistics();
            Double a = StringSigaUtils.toDouble(ip[1]) * (Objects.toString(ip[1]).contains("TB") ? 1000 : 1);
            return stats.getMax() < a ? "Acima" : "Esperado";
        }
        return "acima";
    }

}
