package ethical.hacker;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class PortServices {

    private static final Map<Integer, String> TCP_SERVICES = new LinkedHashMap<>();
    private static final Map<Integer, String> UDP_SERVICES = new LinkedHashMap<>();
    private static final Logger LOG = HasLogging.log();
    static {
        loadServiceNames();
    }
    private final String description;
    private final String type;
    private final int[] ports;

    private PortServices(String name, String type, int... ports) {
        description = name;
        this.type = type;
        this.ports = ports;
    }

    public String getDescription() {
        return description;
    }

    public int[] getPorts() {
        return ports;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", description, type, Arrays.toString(ports));
    }

    public static PortServices getServiceByPort(Integer port) {
        if (TCP_SERVICES.isEmpty() || UDP_SERVICES.isEmpty()) {
            loadServiceNames();
        }
        String tcpService = TCP_SERVICES.get(port);
        String udpService = UDP_SERVICES.get(port);
        if (tcpService != null && udpService != null) {
            return new PortServices(Stream.of(tcpService, udpService).distinct().collect(Collectors.joining(",\t")),
                    "TCP/UDP", port);
        }
        if (tcpService != null) {
            return new PortServices(tcpService, "TCP", port);
        }
        if (udpService != null) {
            return new PortServices(tcpService, "UDP", port);
        }
        return new PortServices("", "", port);
    }

    public static String getServiceDescriptionByPort(Integer port) {
        return getServiceByPort(port) + "";
    }

    public static Map<Integer, String> getTcpServices() {
        if (TCP_SERVICES.isEmpty()) {
            loadServiceNames();
        }
        return TCP_SERVICES;
    }

    public static void loadServiceNames() {
        RunnableEx.run(() -> {
            try (BufferedReader bRead = Files.newBufferedReader(ResourceFXUtils.toPath("nmap-services"))) {
                bRead.lines().forEach(line -> classifyService(line.trim()));
            }
            DataframeBuilder.build("ports.csv").forEachRow(e -> {
                String protocol = Objects.toString(e.get("Port"), "");
                String description = Objects.toString(e.get("Description"), "");
                Integer port = StringSigaUtils.toInteger(protocol);
                if (StringUtils.containsIgnoreCase(protocol, "tcp")) {
                    TCP_SERVICES.putIfAbsent(port, description);
                }
                if (StringUtils.containsIgnoreCase(protocol, "udp")) {
                    UDP_SERVICES.putIfAbsent(port, description);
                }
            });
        });
    }

    public static void main(String[] args) {
        loadServiceNames();
        String udpServices = UDP_SERVICES.entrySet().stream()
                .sorted(Comparator.comparing(Entry<Integer, String>::getKey)).map(Entry<Integer, String>::toString)
                .collect(Collectors.joining("\n\t", "\n", ""));
        LOG.info("UDP = {}", udpServices);
        String tcpServices =
                TCP_SERVICES.entrySet().stream().sorted(Comparator.comparing(Entry<Integer, String>::getKey))
                        .map(Entry<Integer, String>::toString).collect(Collectors.joining("\n\t", "\n\t", ""));
        LOG.info("TCP = {}", tcpServices);
    }

    private static void classifyService(String line) {
        if (line.length() == 0 || line.charAt(0) == '#') {
            return;
        }
        String[] toks = line.split("\\s+");
        if (toks.length < 2) {
            return;
        }

        String[] portAndProtocol = toks[1].split("/");
        if (portAndProtocol.length != 2) {
            return;
        }

        String serviceName = toks[0];
        Integer port = Integer.valueOf(portAndProtocol[0]);
        String protocol = portAndProtocol[1].trim();
        if ("tcp".equalsIgnoreCase(protocol)) {
            TCP_SERVICES.put(port, serviceName);
        } else if ("udp".equalsIgnoreCase(protocol)) {
            UDP_SERVICES.put(port, serviceName);
        } else {
            LOG.error("Unrecognized protocol in line: \"{}\"", line);
        }

    }

}
