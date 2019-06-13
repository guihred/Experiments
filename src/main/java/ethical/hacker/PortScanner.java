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

    private static final String OS_REGEX = "Aggressive OS guesses: (.+)" + "|Running: (.+)"
        + "|Running \\(JUST GUESSING\\): (.+)" + "|MAC Address: [A-F:0-9]+ \\((.+)\\)\\s*" + "|OS details: (.+)";
    private static final String NMAP_FILES = "\"C:\\Program Files (x86)\\Nmap\\nmap.exe\"";
    private static final int STEP = 100;
    private static final Logger LOG = HasLogging.log();
    private static final String PORT_REGEX = "\\d+/.+";

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
        String ip = "localhost";
        List<PortServices> services = scanPortsHost(ip);
        services.forEach(
            p -> LOG.info("Host {} service = {} ports = {}", ip, p.getDescription(), Arrays.toString(p.getPorts())));
    }

    public static ObservableMap<String, List<String>> scanNetworkOpenPorts(String networkAddress) {
        return scanNetworkOpenPorts(networkAddress, Collections.emptyList());
    }

    public static ObservableMap<String, List<String>> scanNetworkOpenPorts(String networkAddress,
        List<Integer> portsSelected) {
        Locale.setDefault(Locale.ENGLISH);
        int nPorts = 5;
        String s = portsSelected.isEmpty() ? "--top-ports " + nPorts
            : "-p" + portsSelected.stream().map(Object::toString).collect(Collectors.joining(","));

        ObservableList<String> executeInConsole = ConsoleUtils
            .executeInConsoleInfoAsync(String.format("%s -sV %s %s", NMAP_FILES, s, networkAddress));
        ObservableMap<String, List<String>> hostsPorts = FXCollections.observableHashMap();
        StringProperty host = new SimpleStringProperty("");
        executeInConsole.addListener((Change<? extends String> c) -> {
            while (c.next()) {
                for (String line : c.getAddedSubList()) {
                    if (line.matches(TracerouteScanner.NMAP_SCAN_REGEX)) {
                        host.set(line.replaceAll(TracerouteScanner.NMAP_SCAN_REGEX, "$1$3"));
                        hostsPorts.put(host.get(), new ArrayList<>());
                        hostsPorts.get(host.get()).add(line.replaceAll(TracerouteScanner.NMAP_SCAN_REGEX, "$2"));
                    }
                    if (line.matches(PORT_REGEX)) {
                        if (!hostsPorts.containsKey(host.get())) {
                            hostsPorts.put(host.get(), new ArrayList<>());
                        }
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
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i < 2 * Short.MAX_VALUE; i += STEP) {
            int j = i;
            Thread thread = new Thread(() -> {
                for (int porta = j; porta <= j + STEP; porta++) {
                    if (isPortOpen(ip, porta, timeout)) {
                        synchronizedList.add(PortServices.getServiceByPort(porta));
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        while (threads.stream().anyMatch(Thread::isAlive)) {
            // NOOP
        }
        Collections.sort(synchronizedList, Comparator.comparing(PortServices::getType));
        return synchronizedList;
    }

    public static ObservableMap<String, List<String>> scanPossibleOSes(String networkAddress) {

        ObservableList<String> executeInConsole = ConsoleUtils
            .executeInConsoleInfoAsync(NMAP_FILES + " -p 22,80,445,65123,56123 --traceroute -O " + networkAddress);
        ObservableMap<String, List<String>> hostsPorts = FXCollections.observableHashMap();
        StringProperty host = new SimpleStringProperty("");
        executeInConsole.addListener((Change<? extends String> c) -> addPortOnChange(hostsPorts, host, c));
        return hostsPorts;
    }

    private static void addPortOnChange(ObservableMap<String, List<String>> hostsPorts, StringProperty host,
        Change<? extends String> change) {
        while (change.next()) {
            for (String line : change.getAddedSubList()) {
                if (line.matches(TracerouteScanner.NMAP_SCAN_REGEX)) {
                    host.set(line.replaceAll(TracerouteScanner.NMAP_SCAN_REGEX, "$1$3"));
                    hostsPorts.put(host.get(), new ArrayList<>());
                }
                if (line.matches(OS_REGEX)) {
                    if (!hostsPorts.containsKey(host.get())) {
                        hostsPorts.put(host.get(), new ArrayList<>());
                    }
                    List<String> list = hostsPorts.get(host.get());
                    String replaceAll = line.replaceAll(OS_REGEX, "$1$2$3$4$5");
                    list.addAll(Stream.of(replaceAll.split(", ")).collect(Collectors.toList()));
                    hostsPorts.remove(host.get());
                    LOG.trace("OS of {} = {}", host.get(), list);
                    hostsPorts.put(host.get(), list);

                }
            }
        }
    }
}
