package ethical.hacker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class PortServices {

    private static final Map<Integer, String> TCP_SERVICES = new LinkedHashMap<>();
    private static final Map<Integer, String> UDP_SERVICES = new LinkedHashMap<>();
    private static final Logger LOG = HasLogging.log();

    private final String description;
    private final String type;
    private final int[] ports;

    PortServices(String name, String type, int... ports) {
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

    public static PortServices getServiceByPort(Integer port) {
        if (TCP_SERVICES.isEmpty() || UDP_SERVICES.isEmpty()) {
            loadServiceNames();
        }
        String tcpService = TCP_SERVICES.get(port);
        String udpService = UDP_SERVICES.get(port);
        if (tcpService != null && udpService != null) {
            return new PortServices(tcpService + ",\t" + udpService, "TCP/UDP", port);
        }
        if (tcpService != null) {
            return new PortServices(tcpService, "TCP", port);
        }
        if (udpService != null) {
            return new PortServices(tcpService, "UDP", port);
        }
        return new PortServices("Unknown", "", port);
    }

    public static void loadServiceNames() {
        try (InputStream inStr = ResourceFXUtils.toStream("nmap-services");
                InputStreamReader inStrReader = new InputStreamReader(inStr, Charset.defaultCharset());
                BufferedReader bRead = new BufferedReader(inStrReader)) {
            String line;

            while ((line = bRead.readLine()) != null) {
                line = line.trim();
                String[] toks;
                String[] portAndProtocol;
                if (line.length() == 0 || line.charAt(0) == '#' || (toks = line.split("\\s+")).length < 2
                        || (portAndProtocol = toks[1].split("/")).length != 2) {
                    continue;
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

        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static void main(String[] args) {
        loadServiceNames();
        String udpServices = UDP_SERVICES.entrySet().stream().map(Entry<Integer, String>::toString)
                .collect(Collectors.joining("\n\t", "\n", ""));
        LOG.info("UDP = {}", udpServices);
        String tcpServices = TCP_SERVICES.entrySet().stream().map(Entry<Integer, String>::toString)
                .collect(Collectors.joining("\n\t", "\n\t", ""));
        LOG.info("TCP = {}", tcpServices);
    }

}
