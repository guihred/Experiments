package extract;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.CSVUtils;
import utils.FileTreeWalker;
import utils.QuickSortML;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class CIDRUtils {
    private static final String NETWORK = "network";
    private static final Logger LOG = HasLogging.log();
    private static final String NETWORKS_CSV = "csv/networks.csv";
    private static DataframeML networkFile;

    public static String convertToString(InetAddress o) {
        return o.getHostAddress();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> findNetwork(String ip) {
        networkFile =
                SupplierEx.orElse(networkFile, () -> {
                    File outFile = ResourceFXUtils.getOutFile(NETWORKS_CSV);
                    if (!outFile.exists()) {
                        makeNetworkCSV();
                    }
                    return DataframeBuilder.build(outFile);
                });
        Map<?, ?> d = networkFile.findFirst(NETWORK, v -> isSameNetworkAddress(Objects.toString(v, ""), ip));
        return (Map<String, String>) d;
    }
    public static boolean isSameNetworkAddress(String cidr, String ip) {
        if (StringUtils.isBlank(cidr)) {
            return false;
        }

        String[] network = cidr.split("/");

        int net = Stream.of(network[0].split("\\.")).mapToInt(Integer::parseInt).reduce(0, (a, i) -> (a << 8) + i);
        int fullAddress = Stream.of(ip.split("\\.")).mapToInt(Integer::parseInt).reduce(0, (a, i) -> (a << 8) + i);
        int fullMask = 0;
        final int mask = Integer.parseInt(network[1]);
        for (int i = mask; i > 0; i--) {
            fullMask = 1 | fullMask << 1;
        }
        fullMask <<= 32 - mask;
        return (fullAddress & fullMask) == net;
    }

    public static void main(String[] args) {
        LOG.info("{}", makeNetworkCSV());
    }

    public static List<Map<String, Object>> makeNetworkCSV() {
        File outFile = ResourceFXUtils.getOutFile(NETWORKS_CSV);

        RunnableEx.run(() -> Files.deleteIfExists(outFile.toPath()));
        List<Path> firstFileMatch = FileTreeWalker.getFirstFileMatch(ResourceFXUtils.getOutFile(),
                p -> p.getFileName().toString().matches("(\\d+\\.){3}\\d+\\.json"));
        List<Map<String, Object>> networkLoaded =
                firstFileMatch.stream().map(FunctionEx.makeFunction(CIDRUtils::readMap))
                .filter(e -> e.size() == 3)
                .distinct().collect(Collectors.toList());
        QuickSortML.sortMapList(networkLoaded, NETWORK, true);
        CSVUtils.appendLines(outFile, networkLoaded);
        return networkLoaded;
    }

    public static InetAddress toInetAddress(String ip) throws UnknownHostException {
        List<Byte> bytes =
                Stream.of(ip.split("\\.")).map(t -> Integer.valueOf(t).byteValue()).collect(Collectors.toList());
        byte[] addr = new byte[] { bytes.get(0), bytes.get(1), bytes.get(2), bytes.get(3) };
        return InetAddress.getByAddress(addr);
    }

    private static Map<String, Object> readMap(Path e) throws IOException {
        return JsonExtractor.accessMap(JsonExtractor.toObject(e.toFile(), NETWORK, "as_owner", "country"));
    }
}
