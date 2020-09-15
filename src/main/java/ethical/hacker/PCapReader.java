
package ethical.hacker;

import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class PCapReader {
    private static final Logger LOG = HasLogging.log();

    private PCapReader() {
    }

    public static void readPCAPngFile(File file) throws IOException {
        final Pcap pcap = Pcap.openStream(file);
        pcap.loop(packet -> {
            if (packet.hasProtocol(Protocol.TCP)) {
                TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                Buffer buffer1 = tcpPacket.getPayload();
                if (buffer1 != null) {
                    LOG.info("TCP: " + buffer1);
                }
            } else if (packet.hasProtocol(Protocol.UDP)) {

                UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                Buffer buffer2 = udpPacket.getPayload();
                if (buffer2 != null) {
                    LOG.info("UDP: " + buffer2);
                }
            }
            return true;
        });
    }
}