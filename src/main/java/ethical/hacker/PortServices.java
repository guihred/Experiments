package ethical.hacker;

import com.aspose.imaging.internal.bouncycastle.util.Arrays;
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

public enum PortServices {
    FTP("File Transfer Protocol", "TCP", 20, 21),
    SSH("Secure Shell", "TCP", 22),
    TELNET("Telnet", "TCP", 23),
    SMTP("Simple Mail Transfer Protocol", "TCP", 25),
    DNS("Domain Name System", "TCP/UDP", 53),
    DHCP("Dynamic Host Configuration Protocol", "UDP", 67, 68),
    TFTP("Trivial File Transfer Protocol", "UDP", 69),
    HTTP("Hypertext Transfer Protocol", "TCP", 80),
    POP("Post Office Protocol", "TCP", 110),
    NTP("Network Time Protocol", "UDP", 123),
    NETBIOS("NetBIOS", "TCP/UDP", 137, 138, 139),
    IMAP("Internet Message Access Protocol", "TCP", 143),
    SNMP("Simple Network Management Protocol", "TCP/UDP", 161, 162),
    BGP("Border Gateway Protocol", "TCP", 179),
    LDAP("Lightweight Directory Access Protocol", "TCP/UDP", 389),
    HTTPS("Hypertext Transfer Protocol over SSL/TLS", "TCP", 443),
    LDAPS("Lightweight Directory Access Protocol over TLS/SSL", "TCP/UDP", 636),
    FTP_TLS_SSL("FTP over TLS/SSL", "TCP", 989, 990),;

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

    public String getType() {
        return type;
    }

    public int[] getPorts() {
        return ports;
    }

    public static void loadServiceNames() {
        try (InputStream inStr = ResourceFXUtils.toStream("nmap-services");
                InputStreamReader inStrReader = new InputStreamReader(inStr, Charset.defaultCharset());
                BufferedReader bRead = new BufferedReader(inStrReader)) {
            String line;

            while ((line = bRead.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                String[] toks = line.split("\\s+");
                if (toks.length < 2) {
                    continue;
                }
                String serviceName = toks[0];
                Integer port = 0;
                String protocol = "";
                String[] portAndProtocol = toks[1].split("/");
                if (portAndProtocol.length != 2) {
                    continue;
                }
                port = Integer.valueOf(portAndProtocol[0]);
                protocol = portAndProtocol[1].trim();
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

    public static PortServices getServiceByPort(int port) {
        PortServices[] values = values();
        for (PortServices portServices : values) {
            if (Arrays.contains(portServices.ports, port)) {
                return portServices;
            }

        }
        return null;
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
