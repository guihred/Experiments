package kibana;

import extract.web.JsonExtractor;
import java.io.File;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
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
    private final DataframeML recurringIps;

    public AcessosVolumetricos() {
        recurringIps = DataframeBuilder.build(new File("C:" + "\\Users\\guigu" + "\\OneDrive - Dataprev"
                + "\\Acessos volumétricos - Firewall - IPs recorrentes.xlsx"));
        recurringIps.map("IP", StringSigaUtils::formatIP);
    }

    public DataframeML getVolumetria(String queryFile, String result) {
        String keyHeader = "key";
        List<Map<String, String>> destinationSearch =
                JsonExtractor.remap(KibanaApi.makeKibanaSearch(queryFile, 1, "*", keyHeader, "value"));
        DataframeML dataframeML = new DataframeML();
        destinationSearch.forEach(dataframeML::add);

        dataframeML.map("IP", keyHeader, e -> e);
        dataframeML.map("Hostname", keyHeader, KibanaApi::getHostname);

        dataframeML.map(TRAFEGO, "value", e -> StringSigaUtils.getFileSize(StringSigaUtils.toLong(e)));
        DataframeUtils.crossFeatureObject(dataframeML, "Tipo de Tráfego", ip -> getTipoTrafego(recurringIps, ip),
                keyHeader,
                TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, TRAFEGO_ESPERADO, ip -> {
            Map<String, Object> findFirst = recurringIps.findFirst("IP", e -> Objects.equals(e, ip[0]));
            return StringSigaUtils.getKey(findFirst, TRAFEGO_ESPERADO);
        }, keyHeader, TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, "motivação",
                ip -> !StringUtils.equalsIgnoreCase("Acima", Objects.toString(ip[1])) ? ""
                        : getPorts(Objects.toString(ip[0])),
                "IP", "Tipo de Tráfego");

        DataframeUtils.save(dataframeML,
                ResourceFXUtils.getOutFile("csv/" + result + DateFormatUtils.currentTime("ddMMyyyy") + ".csv"));
        String string = DataframeUtils.toString(dataframeML);
        LOG.info("{}", string);
        return dataframeML;
    }


    public static void main(String[] args) {
        ExtractUtils.insertProxyConfig();
        AcessosVolumetricos acessos = new AcessosVolumetricos();
        acessos.getVolumetria("sourceQuery.json", "source");
        acessos.getVolumetria("destinationQuery.json", "destination");
    }

    private static String getPorts(String ip) {
        return KibanaApi.destinationPorts(ip, 1);
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
