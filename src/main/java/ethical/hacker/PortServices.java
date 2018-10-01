package ethical.hacker;

import com.aspose.imaging.internal.bouncycastle.util.Arrays;

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

    public static PortServices getServiceByPort(int port) {
        PortServices[] values = values();
        for (PortServices portServices : values) {
            if (Arrays.contains(portServices.ports, port)) {
                return portServices;
            }

        }
        return null;
    }
}
