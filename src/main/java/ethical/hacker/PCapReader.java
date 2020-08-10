
package ethical.hacker;

import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import java.io.File;
import java.io.IOException;

public class PCapReader {

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\CiscoCNNA\\one.pcapng");
        final Pcap pcap = Pcap.openStream(file);

        pcap.loop(packet -> {
            if (packet.hasProtocol(Protocol.TCP)) {
                TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                Buffer buffer1 = tcpPacket.getPayload();
                if (buffer1 != null) {
                    System.out.println("TCP: " + buffer1);
                }
            } else if (packet.hasProtocol(Protocol.UDP)) {

                UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                Buffer buffer2 = udpPacket.getPayload();
                if (buffer2 != null) {
                    System.out.println("UDP: " + buffer2);
                }
            }
            return true;
        });
    }
}