package extract;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class CIDRUtils {
    private static final String NETWORK = "network";
    private static final String NETWORKS_CSV = "csv/networks.csv";
    private static DataframeML networkFile;

    public static String getReverseDNS(String ip) {
        return SupplierEx.get(() -> toInetAddress(ip).getCanonicalHostName(), ip);
    }

    public static boolean isPrivateNetwork(String ip) {
        List<String> asList = Arrays.asList("10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16");
        return asList.stream().anyMatch(net -> isSameNetworkAddress(net, ip));
    }

    public static String addressToPattern(String cidr) {
        if (StringUtils.isBlank(cidr) || !cidr.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+")) {
            return cidr;
        }
        
        String[] network = cidr.split("/");
        
        List<Integer> net = Stream.of(network[0].split("\\.")).map(Integer::valueOf).collect(Collectors.toList());
        final int mask = Integer.parseInt(network[1]);
        int mas = mask;
        StringBuilder pattern = new StringBuilder();
        for (Integer integer : net) {
            if (mas <= 0) {
                break;
            }
            if (mas < 8) {
                int i = integer & toPartialMask(mas);
                final int MAX_BYTE = 255;
                int j = ~toPartialMask(mas) & MAX_BYTE;
                String i2 = j == 0 ? "" : j + "";

                pattern.append(("" + i).replaceAll(".{" + i2.length() + "}$", "") + "*");
            }
            if (mas > 8) {
                pattern.append(integer + ".");
            }
            if (mas == 8) {
                pattern.append(integer + ".*");
            }

            mas -= 8;
        }
        return pattern.toString().replaceAll("\\.0+", ".");
    }

    public static String convertToString(InetAddress o) {
        return o.getHostAddress();
    }
    public static Map<String, String> findNetwork(String ip) {
        networkFile =
                SupplierEx.orElse(networkFile, () -> {
                    File outFile = ResourceFXUtils.getOutFile(NETWORKS_CSV);
                    if (!outFile.exists()) {
                        makeNetworkCSV();
                    }
                    return DataframeBuilder.build(outFile);
                });
        return searchInFile(networkFile, NETWORK, ip);
    }

    public static boolean isSameNetworkAddress(String cidr, String ip) {
        if (StringUtils.isBlank(cidr) || StringUtils.isBlank(ip)) {
            return false;
        }
        if (!cidr.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+") && !ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return false;
        }

        String[] network = cidr.split("/");
        int net = Stream.of(network[0].split("\\.")).mapToInt(StringSigaUtils::toInteger).reduce(0,
                (a, i) -> (a << 8) + i);
        int fullAddress =
                Stream.of(ip.split("\\.")).mapToInt(StringSigaUtils::toInteger).reduce(0, (a, i) -> (a << 8) + i);
        final int mask = Integer.parseInt(network[1]);
        int fullMask = toFullMask(mask);
        return (fullAddress & fullMask) == net;
    }

    public static void main(String[] args) {
        makeNetworkCSV();
    }

    public static synchronized List<Map<String, Object>> makeNetworkCSV() {
        File outFile = ResourceFXUtils.getOutFile(NETWORKS_CSV);

        RunnableEx.run(() -> Files.deleteIfExists(outFile.toPath()));
        List<Path> firstFileMatch = FileTreeWalker.getFirstFileMatch(ResourceFXUtils.getOutFile(),
                p -> p.getFileName().toString().matches("(\\d+\\.){3}\\d+\\.json"));
        List<Map<String, Object>> networkLoaded =
                firstFileMatch.stream().map(FunctionEx.makeFunction(CIDRUtils::readMap))
                .filter(e -> e.size() == 3)
                .distinct().collect(Collectors.toList());
        List<Path> first2FileMatch = FileTreeWalker.getFirstFileMatch(ResourceFXUtils.getOutFile(),
                p -> p.getFileName().toString().matches("(\\d+\\.){3}\\d+\\.xml"));
        List<Map<String, Object>> xmlLoaded = first2FileMatch.stream().map(FunctionEx.makeFunction(CIDRUtils::readXML))
                .distinct().collect(Collectors.toList());
        networkLoaded.addAll(xmlLoaded);
        QuickSortML.sortMapList(networkLoaded, NETWORK, true);
        CSVUtils.appendLines(outFile, networkLoaded);
        return networkLoaded;
    }

    public static Map<String, String> searchInFile(DataframeML dataframe, String network2, String ip) {
        return dataframe.findFirst(network2,
                v -> Objects.equals(v, ip) || isSameNetworkAddress(Objects.toString(v, ""), ip));
    }

    public static InetAddress toInetAddress(String ip) throws UnknownHostException {
        List<Byte> bytes =
                Stream.of(ip.split("\\.")).map(t -> Integer.valueOf(t).byteValue()).collect(Collectors.toList());
        byte[] addr = new byte[] { bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3) };
        return InetAddress.getByAddress(addr);
    }

    public static InetAddress toIPByName(String name) throws UnknownHostException {
        return InetAddress.getByName(name);
    }

    private static Map<String, Object> readMap(Path e) throws IOException {
        return JsonExtractor.accessMap(JsonExtractor.toObject(e.toFile(), NETWORK, "as_owner", "country"));
    }

    private static Map<String, Object> readXML(Path xmlFile) throws IOException {
        Document document = Jsoup.parse(xmlFile.toFile(), StandardCharsets.UTF_8.name());
        Map<String, Object> map2 = new HashMap<>();
        document.getElementsByTag("ip").forEach(
                e -> e.children().forEach(m -> map2.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        Map<String, Object> map = new HashMap<>();
        map.put(NETWORK, map2.get(NETWORK));
        map.put("as_owner", map2.get("asname"));
        map.put("country", map2.get("ascountry"));
        return map;
    }

    private static int toFullMask(final int mask) {
        int fullMask = 0;
        for (int i = mask; i > 0; i--) {
            fullMask = 1 | fullMask << 1;
        }
        fullMask <<= 32 - mask;
        return fullMask;
    }

    private static int toPartialMask(final int mask) {
        int fullMask = 0;
        for (int i = mask; i > 0; i--) {
            fullMask = 1 | fullMask << 1;
        }
        fullMask <<= 8 - mask;
        return fullMask;
    }
}
