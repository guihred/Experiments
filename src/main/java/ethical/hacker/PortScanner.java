package ethical.hacker;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.HasLogging;

public class PortScanner {

    private static final int STEP = 100;
    private static final Logger LOG = HasLogging.log(PortScanner.class);

    public static boolean isPortOpen(String ip, int porta, int timeout) {
        HasLogging.log(PortScanner.class);
        try (Socket socket = new Socket();) {
            socket.connect(new InetSocketAddress(ip, porta), timeout);
            return true;
        } catch (Exception ex) {
            LOG.trace("", ex);
            return false;
        }
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
            ;
        }
        Collections.sort(synchronizedList);
        return synchronizedList;
    }

    public static void main(String[] args) {
        List<Integer> scanHost = scanPortsHost("localhost");
        String openPorts = scanHost.stream().map(Objects::toString).collect(Collectors.joining(","));
        LOG.info("Available ports = {}", openPorts);
    }
}
