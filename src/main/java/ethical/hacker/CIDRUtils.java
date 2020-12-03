package ethical.hacker;

import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class CIDRUtils {
    private static final Logger LOG = HasLogging.log();
    private static final String NETWORKS_CSV = "csv/networks.csv";
    private static DataframeML networkFile;

    public static String convertToString(InetAddress o) {
        return o.getHostAddress();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> findNetwork(String ip) {
        File outFile = ResourceFXUtils.getOutFile(NETWORKS_CSV);
        if (!outFile.exists()) {
            makeNetworkCSV();
        }
        networkFile =
                SupplierEx.orElse(networkFile, () -> DataframeBuilder.build(outFile));
        Map<?, ?> d = networkFile.findFirst("network", v -> isSameNetworkAddress(Objects.toString(v, ""), ip));
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
        LOG.info("{}", findNetwork("187.34.119.82"));
    }

    public static InetAddress toInetAddress(String ip) throws UnknownHostException {
        List<Byte> collect =
                Stream.of(ip.split("\\.")).map(t -> Integer.valueOf(t).byteValue()).collect(Collectors.toList());
        byte[] addr = new byte[] { collect.get(0), collect.get(1), collect.get(2), collect.get(3) };
        return InetAddress.getByAddress(addr);
    }

    private static void makeNetworkCSV() {
        List<Path> firstFileMatch = FileTreeWalker.getFirstFileMatch(ResourceFXUtils.getOutFile(),
                p -> p.getFileName().toString().matches("(\\d+\\.){3}\\d+\\.json"));
        List<Map<String, Object>> collect = firstFileMatch.stream().map(FunctionEx.makeFunction(CIDRUtils::readMap))
                .distinct().collect(Collectors.toList());
        CSVUtils.appendLines(ResourceFXUtils.getOutFile(NETWORKS_CSV), collect);
    }

    private static Map<String, Object> readMap(Path e) throws IOException {
        return JsonExtractor.accessMap(JsonExtractor.toObject(e.toFile(), "network", "as_owner", "country"));
    }
}
