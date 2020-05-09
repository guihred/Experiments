package ethical.hacker;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.ExtractUtils;
import utils.HasLogging;

public class TracerouteScanner {

    public static final String NMAP_SCAN_REGEX = "Nmap scan report for ([\\d\\.]+)"
        + "|Nmap scan report for ([^\\s]+) \\(([\\d\\.]+)\\)";
    private static final Logger LOG = HasLogging.log();
    private static final String NMAP_FILES = "C:\\Program Files (x86)\\Nmap\\nmap.exe";
    private static final String REUSED_ROUTE_REGEX = "-\\s*Hops (\\d+)-(\\d+) are the same as for ([\\d\\.]+)";
    private static final String REUSED_ROUTE_REGEX_1 = "-\\s*Hop (\\d+) is the same as for ([\\d\\.]+)";
    private static final String HOP_REGEX = "\\d+\\s+[\\d\\.]+ ms\\s+([\\d\\.]+)"
        + "|\\d+\\s+[\\d\\.]+ ms\\s+[\\w\\.]+ \\(([\\d\\.]+)\\)";

    public static final String IP_TO_SCAN = getIPtoScan();

    public static final String NETWORK_ADDRESS = getNetworkAddress();

    public static void main(final String[] args) {
        Map<String, List<String>> scanNetwork = scanNetworkRoutes(NETWORK_ADDRESS);
        scanNetwork.forEach((h, p) -> LOG.info("Host {} route = {}", h, p));
    }

    public static ObservableMap<String, List<String>> scanNetworkRoutes(final String networkAddress) {
        ObservableMap<String, List<String>> synchronizedObservableMap = FXCollections
            .synchronizedObservableMap(FXCollections.observableHashMap());
        String nmapCommand = getNmapCommand();
        ObservableList<String> executeInConsole = ConsoleUtils
            .executeInConsoleInfoAsync(nmapCommand + " --traceroute -sn " + networkAddress);
        Map<String, List<String>> hostsPorts = new HashMap<>();
        StringProperty host = new SimpleStringProperty("");
        executeInConsole.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (String line : c.getAddedSubList()) {
                    if (line.matches(NMAP_SCAN_REGEX)) {
                        host.set(line.replaceAll(NMAP_SCAN_REGEX, "$1$3"));
                        hostsPorts.put(host.get(), new ArrayList<>());
                    }
                    if (line.matches(REUSED_ROUTE_REGEX + "|" + REUSED_ROUTE_REGEX_1 + "|" + HOP_REGEX)) {
                        hostsPorts.computeIfAbsent(host.get(), e -> new ArrayList<>()).add(line);
                    }
                }
            }
            Map<String, List<String>> netRoutes = hostsPorts.entrySet().stream()
                .collect(toMap(Entry<String, List<String>>::getKey, e -> extractHops(hostsPorts, e)));
            synchronizedObservableMap.putAll(netRoutes);
        });

        return synchronizedObservableMap;
    }

    private static List<String> extractHops(final Map<String, List<String>> hostsPorts,
        final Entry<String, List<String>> e) {
        return e.getValue().stream().flatMap(l -> turnReferencesIntoHops(hostsPorts, l)).collect(toList());
    }

    private static String getIPtoScan() {
        return Stream.of("66", "102", "8", "1").collect(joining("."));
    }

    private static String getNetworkAddress() {
        if (ExtractUtils.isNotProxied()) {
            return IP_TO_SCAN + "/30";
        }

        return IP_TO_SCAN + "/28";

    }

    private static String getNmapCommand() {
        if (SystemUtils.IS_OS_LINUX) {
            return "nmap ";
        }
        return "\"" + NMAP_FILES + "\"";
    }

    private static Stream<? extends String> turnReferencesIntoHops(final Map<String, List<String>> hostsPorts,
        final String line) {
        try {
            if (line.matches(REUSED_ROUTE_REGEX)) {
                String host = line.replaceAll(REUSED_ROUTE_REGEX, "$3");
                if (!hostsPorts.containsKey(host)) {
                    return Stream.empty();
                }
                String hops = line.replaceAll(REUSED_ROUTE_REGEX, "$1,$2");
                int[] array = Stream.of(hops.split(",")).mapToInt(Integer::parseInt).toArray();
                List<String> list = hostsPorts.get(host);
                return list.subList(array[0] - 1, Integer.min(list.size(), array[1])).stream()
                    .map(ml -> ml.replaceAll(HOP_REGEX, "$1$2"));
            }
            if (line.matches(REUSED_ROUTE_REGEX_1)) {
                String host = line.replaceAll(REUSED_ROUTE_REGEX_1, "$2");
                if (!hostsPorts.containsKey(host)) {
                    return Stream.empty();
                }
                String hops = line.replaceAll(REUSED_ROUTE_REGEX_1, "$1");
                int index = Integer.parseInt(hops) - 1;
				if (index < hostsPorts.get(host).size()) {
					String subList = hostsPorts.get(host).get(index);
					return Stream.of(subList).map(ml -> ml.replaceAll(HOP_REGEX, "$1$2"));
				}
            }
            return Stream.of(line.replaceAll(HOP_REGEX, "$1$2"));
        } catch (Exception e) {
            LOG.error("ERROR LINE={}", line);
            LOG.error("", e);
            return Stream.empty();
        }
    }
}
