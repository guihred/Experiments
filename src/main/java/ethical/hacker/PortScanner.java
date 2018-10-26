package ethical.hacker;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public class PortScanner {

    private static final String NMAP_FILES = "C:\\Program Files (x86)\\Nmap\\nmap.exe";
    private static final int STEP = 100;
	private static final Logger LOG = HasLogging.log();

    public static boolean isPortOpen(String ip, int porta, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, porta), timeout);
            return true;
        } catch (Exception ex) {
            LOG.trace("", ex);
            return false;
        }
    }

    public static void main(String[] args) {
        //        Map<String, List<String>> scanNetwork = scanPossibleOSes(TracerouteScanner.NETWORK_ADDRESS);
        //
        //		scanNetwork.forEach((h, p) -> LOG.info("Host {} ports = {}", h, p));
        String ip = "localhost";
        List<PortServices> services = scanPortsHost(ip);
        services.forEach(p -> LOG.info("Host {} service = {} ports = {}", ip, p.getDescription(),
                Arrays.toString(p.getPorts())));
    }

    public static ObservableMap<String, List<String>> scanNetworkOpenPorts(String networkAddress) {
        Locale.setDefault(Locale.ENGLISH);
        String hostRegex = "Nmap scan report for ([\\d\\.]+)";
        String portRegex = "\\d+/.+";
        ObservableList<String> executeInConsole = ConsoleUtils
                .executeInConsoleInfoAsync(
                        "\"" + NMAP_FILES + "\" -sV --top-ports 10 " + networkAddress);
        ObservableMap<String, List<String>> hostsPorts = FXCollections.observableHashMap();
        StringProperty host = new SimpleStringProperty("");
        executeInConsole.addListener((Change<? extends String> c) -> {
            while (c.next()) {
                for (String line : c.getAddedSubList()) {
                    if (line.matches(hostRegex)) {
                        host.set(line.replaceAll(hostRegex, "$1"));
                        hostsPorts.put(host.get(), new ArrayList<>());
                    }
                    if (line.matches(portRegex) && hostsPorts.containsKey(host.get())) {
                        List<String> list = hostsPorts.get(host.get());
                        list.add(line);
                        hostsPorts.remove(host.get());
                        hostsPorts.put(host.get(), new ArrayList<>(list));
                    }
                }
            }
        });
        return hostsPorts;
    }

    public static List<PortServices> scanPortsHost(String ip) {
        List<PortServices> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        final int timeout = 200;
        List<Thread> arrayList = new ArrayList<>();
        for (int i = 1; i < 65535; i += STEP) {
            int j = i;
            Thread thread = new Thread(() -> {
                for (int porta = j; porta <= j + STEP; porta++) {
                    if (isPortOpen(ip, porta, timeout)) {
                        synchronizedList.add(PortServices.getServiceByPort(porta));
                    }
                }
            });
            arrayList.add(thread);
            thread.start();
        }
        while (arrayList.stream().anyMatch(Thread::isAlive)) {
            //NOOP 
        }
        Collections.sort(synchronizedList, Comparator.comparing(PortServices::getType));
        return synchronizedList;
    }

    public static ObservableMap<String, List<String>> scanPossibleOSes(String networkAddress) {
        String hostRegex = "Nmap scan report for ([\\d\\.]+)";
		String osRegex = "Aggressive OS guesses: (.+)|Running: (.+)|Running \\(JUST GUESSING\\): (.+)|MAC Address: [A-F:0-9]+ \\((.+)\\)\\s*|OS details: (.+)";
        ObservableList<String> executeInConsole = ConsoleUtils.executeInConsoleInfoAsync(
						"\"" + NMAP_FILES + "\" -p 22,80,445,65123,56123 --traceroute -O " + networkAddress);
        ObservableMap<String, List<String>> hostsPorts = FXCollections.observableHashMap();
        StringProperty host = new SimpleStringProperty("");
        executeInConsole.addListener((Change<? extends String> c) -> {
            while (c.next()) {
                for (String line : c.getAddedSubList()) {
                    if (line.matches(hostRegex)) {
                        host.set(line.replaceAll(hostRegex, "$1"));
                        hostsPorts.put(host.get(), new ArrayList<>());
                    }
                    if (line.matches(osRegex) && hostsPorts.containsKey(host.get())) {
                        List<String> list = hostsPorts.get(host.get());
                        String replaceAll = line.replaceAll(osRegex, "$1$2$3$4$5");
                        list.addAll(Stream.of(replaceAll.split(", ")).collect(Collectors.toList()));
                        hostsPorts.remove(host.get());
                        LOG.info("OS of {} = {}", host.get(), list);
                        hostsPorts.put(host.get(), list);

                    }
                }
            }
        });
        return hostsPorts;
    }
}
