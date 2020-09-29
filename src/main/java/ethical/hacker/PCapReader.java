
package ethical.hacker;

import io.pkts.Pcap;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import utils.ClassReflectionUtils;

public final class PCapReader {

    private PCapReader() {
    }

    public static List<Map<String, String>> readPCAPngFile(File file) throws IOException {
        final Pcap pcap = Pcap.openStream(file);
        List<Map<String, String>> packets = new ArrayList<>();
        pcap.loop(packet -> {
            if (packet.hasProtocol(Protocol.TCP)) {
                TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                Map<String, String> description = ClassReflectionUtils.getDescriptionRecursive(tcpPacket);
                packets.add(description);
            } else if (packet.hasProtocol(Protocol.UDP)) {

                UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                Map<String, String> description = ClassReflectionUtils.getDescriptionRecursive(udpPacket);
                packets.add(description);
            }
            return true;
        });
        return packets;
    }
}