package ethical.hacker;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class PortScanner {

    private static final String NMAP_FILES = "C:\\Program Files (x86)\\Nmap\\nmap.exe";
    private static final int STEP = 100;
    private static final Logger LOG = HasLogging.log(PortScanner.class);

    public static boolean isPortOpen(String ip, int porta, int timeout) {
        try (Socket socket = new Socket();) {
            socket.connect(new InetSocketAddress(ip, porta), timeout);
            return true;
        } catch (Exception ex) {
            LOG.trace("", ex);
            return false;
        }
    }

    public static Map<String, List<String>> scanNetworkOpenPorts(String networkAddress) {
        Locale.setDefault(Locale.ENGLISH);
        String hostRegex = "Nmap scan report for ([\\d\\.]+)";
        String portRegex = "\\d+/.+";
        List<String> executeInConsole = ResourceFXUtils
                .executeInConsoleInfo(
                        "\"" + NMAP_FILES + "\" --open -sV --top-ports 10 " + networkAddress);
        Map<String, List<String>> hostsPorts = new HashMap<>();
        String host = "";
        for (String line : executeInConsole) {
            if (line.matches(hostRegex)) {
                host = line.replaceAll(hostRegex, "$1");
                hostsPorts.put(host, new ArrayList<>());
            }
            if (line.matches(portRegex) && hostsPorts.containsKey(host)) {

                hostsPorts.get(host).add(line);
            }

        }

        return hostsPorts;
    }

    public static Map<String, List<String>> scanPossibleOSes(String networkAddress) {
        Locale.setDefault(Locale.ENGLISH);
        String hostRegex = "Nmap scan report for ([\\d\\.]+)";
        String osRegex = "Aggressive OS guesses: (.+)|Running: (.+)|Running \\(JUST GUESSING\\): (.+)";
        List<String> executeInConsole = ResourceFXUtils
                .executeInConsoleInfo("\"" + NMAP_FILES + "\" -p 22,80,445,65123,56123 -O " + networkAddress);
        Map<String, List<String>> hostsPorts = new HashMap<>();
        String host = "";
        for (String line : executeInConsole) {
            if (line.matches(hostRegex)) {
                host = line.replaceAll(hostRegex, "$1");
                hostsPorts.put(host, new ArrayList<>());
            }
            if (line.matches(osRegex) && hostsPorts.containsKey(host)) {

                hostsPorts.get(host).add(line.replaceAll(osRegex, "$1$2$3"));
            }

        }

        return hostsPorts;
    }

    public static List<Integer> scanPortsHost(String ip) {
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        final int timeout = 200;
        List<Thread> arrayList = new ArrayList<>();
        for (int i = 1; i < 65535; i += STEP) {
            int j = i;
            Thread thread = new Thread(() -> {
                for (int porta = j; porta <= j + STEP; porta++) {
                    if (isPortOpen(ip, porta, timeout)) {
                        LOG.info("porta {} aberta em {} ", porta, ip);
                        PortServices service = PortServices.getServiceByPort(porta);
                        if (service != null) {
                            LOG.info("service = {} ", service.getDescription());
                        }
                        synchronizedList.add(porta);
                    }
                }
            });
            arrayList.add(thread);
            thread.start();
        }
        while (arrayList.stream().anyMatch(Thread::isAlive)) {
            //NOOP 
        }
        Collections.sort(synchronizedList);
        return synchronizedList;
    }

    public static void main(String[] args) {
        //        List<Integer> scanHost = scanPortsHost("localhost");
        //        String openPorts = scanHost.stream().map(Objects::toString).collect(Collectors.joining(","));
        //        LOG.info("Available ports = {}", openPorts);
        Map<String, List<String>> scanNetwork = scanPossibleOSes("10.122.25.0/27");

        scanNetwork.forEach((h, p) -> LOG.info("Host {} ports = {}", h, p));

    }
}