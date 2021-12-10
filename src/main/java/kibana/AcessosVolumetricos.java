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
import utils.*;
import utils.ex.HasLogging;

public class AcessosVolumetricos {
    private static final String TIPO_DO_TRAFEGO = "Tipo de tráfego";
    private static final Logger LOG = HasLogging.log();
    private static final String TRAFEGO = "Tráfego";
    private static final String TRAFEGO_ESPERADO = "Tráfego esperado";
    private final DataframeML recurringIps;

    public AcessosVolumetricos() {
        File outFile = ResourceFXUtils.getOutFile("xlsx/Acessos volumétricos - Firewall - IPs recorrentes.xlsx");
        recurringIps = DataframeBuilder.build(outFile.exists() ? outFile :

                new File("C:" + "\\Users\\guigu" + "\\OneDrive - Dataprev"
                + "\\Acessos volumétricos - Firewall - IPs recorrentes.xlsx"));
        LOG.info("File READ");
        recurringIps.map("IP", StringSigaUtils::formatIP);
    }

    public DataframeML getVolumetria(String queryFile, String result) {
        String keyHeader = "key";
        List<Map<String, String>> destinationSearch =
                JsonExtractor.remap(KibanaApi.makeKibanaSearch(queryFile, 1. / 2, "*", keyHeader, "value"));
        DataframeML dataframeML = new DataframeML();
        destinationSearch.forEach(dataframeML::add);

        dataframeML.map("IP", keyHeader, e -> e);
        dataframeML.map("Hostname", keyHeader, KibanaApi::getHostname);

        dataframeML.map(TRAFEGO, "value", e -> StringSigaUtils.getFileSize(StringSigaUtils.toLong(e)));
        DataframeUtils.crossFeatureObject(dataframeML, TIPO_DO_TRAFEGO, ip -> getTipoTrafego(recurringIps, ip),
                keyHeader,
                TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, TRAFEGO_ESPERADO, ip -> {
            Map<String, Object> findFirst = recurringIps.findFirst("IP", e -> Objects.equals(e, ip[0]));
            return StringSigaUtils.getKey(findFirst, TRAFEGO_ESPERADO);
        }, keyHeader, TRAFEGO);
        DataframeUtils.crossFeatureObject(dataframeML, "motivação",
                ip -> !StringUtils.equalsIgnoreCase("Acima", Objects.toString(ip[1])) ? ""
                        : getPorts(Objects.toString(ip[0])),
                "IP", TIPO_DO_TRAFEGO);

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
        DataframeML volumetria = acessos.getVolumetria("sourceEspecialQuery.json", "sourceEspecial");
        DataframeML volumetria2 = acessos.getVolumetria("destinationEspecialQuery.json", "destinationEspecial");
        volumetria2.add(new SimpleMap("IP", "Tabela Destination"));
        volumetria.forEachRow(volumetria2::add);
        DataframeUtils.save(volumetria2,
                ResourceFXUtils.getOutFile("csv/" + "ips" + DateFormatUtils.currentTime("ddMMyyyy") + ".csv"));

    }

    private static String getPorts(String ip) {
        return KibanaApi.destinationPorts(ip, 1);
    }

    private static String getTipoTrafego(DataframeML build, Object[] ip) {
        Map<String, Object> findFirst = build.findFirst("IP", e -> Objects.equals(e, ip[0]));
        if (findFirst == null || !findFirst.containsKey(TRAFEGO_ESPERADO)) {
            return "acima";
        }
        Object object = findFirst.get(TRAFEGO_ESPERADO);
        String[] split = Objects.toString(object, "").split("[^\\d\\.]+");
        DoubleSummaryStatistics stats = Stream.of(split).mapToDouble(StringSigaUtils::toLong).summaryStatistics();
        Double a = StringSigaUtils.toDouble(ip[1]) * (Objects.toString(ip[1]).contains("TB") ? 1000 : 1);
        return stats.getMax() < a ? "Acima" : "Esperado";
    }

}
