package ethical.hacker;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public class TracerouteScanner {

	private static final Logger LOG = HasLogging.log();
	private static final String NMAP_FILES = "C:\\Program Files (x86)\\Nmap\\nmap.exe";
	private static final String REUSED_ROUTE_REGEX = "-\\s*Hops (\\d+)-(\\d+) are the same as for ([\\d\\.]+)";
	private static final String REUSED_ROUTE_REGEX_1 = "-\\s*Hop (\\d+) is the same as for ([\\d\\.]+)";
	private static final String HOP_REGEX = "\\d+\\s+[\\d\\.]+ ms\\s+([\\d\\.]+)|\\d+\\s+[\\d\\.]+ ms\\s+[\\w\\.]+ \\(([\\d\\.]+)\\)";

    public static final String NETWORK_ADDRESS = "10.69.64.31/28";

	public static void main(String[] args) {
		Map<String, List<String>> scanNetwork = scanNetworkRoutes(NETWORK_ADDRESS);
		scanNetwork.forEach((h, p) -> LOG.info("Host {} route = {}", h, p));
	}

	public static ObservableMap<String, List<String>> scanNetworkRoutes(String networkAddress) {
		ObservableMap<String, List<String>> synchronizedObservableMap = FXCollections
				.synchronizedObservableMap(FXCollections.observableHashMap());
        new Thread(() -> {

			Locale.setDefault(Locale.ENGLISH);
			String hostRegex = "Nmap scan report for ([\\d\\.]+)|Nmap scan report for [\\w\\.]+ \\(([\\d\\.]+)\\)";
        List<String> executeInConsole = ConsoleUtils
					.executeInConsoleInfo("\"" + NMAP_FILES + "\" --traceroute -sn " + networkAddress);
			Map<String, List<String>> hostsPorts = new HashMap<>();
			String host = "";
			for (String line : executeInConsole) {
				if (line.matches(hostRegex)) {
					host = line.replaceAll(hostRegex, "$1$2");
					hostsPorts.put(host, new ArrayList<>());
				}
				if (line.matches(REUSED_ROUTE_REGEX + "|" + REUSED_ROUTE_REGEX_1 + "|" + HOP_REGEX)
						&& hostsPorts.containsKey(host)) {
					hostsPorts.get(host).add(line);
				}
			}
			Map<String, List<String>> netRoutes = hostsPorts.entrySet().stream()
					.collect(Collectors.toMap(Entry<String, List<String>>::getKey, e -> extractHops(hostsPorts, e)));
			synchronizedObservableMap.putAll(netRoutes);
        }).start();

		return synchronizedObservableMap;
	}

	private static List<String> extractHops(Map<String, List<String>> hostsPorts, Entry<String, List<String>> e) {
		return e.getValue().stream().flatMap(l -> {
			if (l.matches(REUSED_ROUTE_REGEX)) {
				String hops = l.replaceAll(REUSED_ROUTE_REGEX, "$1,$2");
				int[] array = Stream.of(hops.split(",")).mapToInt(Integer::parseInt).toArray();
				String host = l.replaceAll(REUSED_ROUTE_REGEX, "$3");
				List<String> subList = hostsPorts.get(host).subList(array[0] - 1, array[1]);
				return subList.stream().map(ml -> ml.replaceAll(HOP_REGEX, "$1$2"));
			}
			if (l.matches(REUSED_ROUTE_REGEX_1)) {
				String hops = l.replaceAll(REUSED_ROUTE_REGEX_1, "$1");
				String host = l.replaceAll(REUSED_ROUTE_REGEX_1, "$2");
				String subList = hostsPorts.get(host).get(Integer.parseInt(hops) - 1);
				return Stream.of(subList).map(ml -> ml.replaceAll(HOP_REGEX, "$1$2"));
			}
			return Stream.of(l.replaceAll(HOP_REGEX, "$1$2"));
		}).collect(Collectors.toList());
	}
}
