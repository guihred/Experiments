package ethical.hacker;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.FunctionEx;
import utils.HasLogging;

public class NetworkInformationScanner {

	private static final Logger LOG = HasLogging.log();
    public static void main(String[] args) throws SocketException {
        displayNetworkInformation();
    }

    public static void displayNetworkInformation() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        Map<Class<?>, FunctionEx<Object, String>> toStringMAp = new HashMap<>();
        toStringMAp.put(byte[].class, o -> convertToString((byte[]) o));
        while (e.hasMoreElements()) {
            NetworkInterface n = e.nextElement();
            if (n.getInterfaceAddresses().isEmpty() || !n.isUp() || n.isLoopback()
                    || !isLocallyAdministered(n.getHardwareAddress())) {
                continue;
            }
            String description = ClassReflectionUtils.getDescription(n, toStringMAp);
            if (!description.isEmpty()) {
                LOG.info("{}", description);
            }
        }
    }

    private static boolean isLocallyAdministered(byte[] macAddress) {
        return (2 & macAddress[0]) == 0;
    }

    private static Stream<String> convertStream(byte[] ipOrMacAddress) {
        if (ipOrMacAddress.length == 16) {
            String[] a = new String[ipOrMacAddress.length / 2];
            for (int j = 0; j < ipOrMacAddress.length; j += 2) {
                a[j / 2] = Integer.toHexString(
                        Byte.toUnsignedInt(ipOrMacAddress[j]) * 256 + Byte.toUnsignedInt(ipOrMacAddress[j + 1]));
            }
            return Stream.of(a);
        }
        String[] a = new String[ipOrMacAddress.length];
        for (int j = 0; j < a.length; j++) {
            int unsignedInt = Byte.toUnsignedInt(ipOrMacAddress[j]);
            a[j] = ipOrMacAddress.length == 4 ? Integer.toString(unsignedInt) : String.format("%02X", unsignedInt);
        }
        return Stream.of(a);
    }

    private static String convertToString(byte[] invoke) {
        String delimiter = delimiter(invoke);
        return Stream.of(invoke).flatMap(NetworkInformationScanner::convertStream)
                .collect(Collectors.joining(delimiter));
    }

    private static String delimiter(byte[] invoke) {
        switch (invoke.length) {
            case 16:
                return ":";
            case 4:
                return ".";
            default:
                return "-";
        }
    }

}
