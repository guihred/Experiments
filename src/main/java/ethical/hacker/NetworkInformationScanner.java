package ethical.hacker;

import extract.CIDRUtils;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.ConsoleUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class NetworkInformationScanner {

    private static final Logger LOG = HasLogging.log();

    private NetworkInformationScanner() {
    }

    public static List<String> displayNetworkInformation() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        Map<Class<?>, FunctionEx<Object, String>> toStringMAp = new HashMap<>();
        toStringMAp.put(byte[].class, o -> convertToString((byte[]) o));
        List<String> arrayList = new ArrayList<>();
        while (e.hasMoreElements()) {
            NetworkInterface n = e.nextElement();
            if (n.getInterfaceAddresses().isEmpty() || !n.isUp() || n.isLoopback()
                || !isLocallyAdministered(n.getHardwareAddress())) {
                continue;
            }
            String description = ClassReflectionUtils.getDescription(n, toStringMAp);
            if (!description.isEmpty()) {
                LOG.trace("{}", description);
                arrayList.add(description);
            }
        }
        return arrayList;
    }

    public static List<Map<String, String>> getIpConfigInformation() {
        return SupplierEx.get(() -> {
            List<String> executeInConsoleInfo = ConsoleUtils.executeInConsoleInfo("ipconfig /all");
            List<Map<String, String>> elements = new ArrayList<>();
            for (String line : executeInConsoleInfo) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                Map<String, String> e = new LinkedHashMap<>();
                elements.add(e);
                if (line.matches("\\S+.*")) {
                    e.put("Group", line.trim());
                } else if (line.matches("(.+):(.+)")) {
                    e.put("Property", line.replaceAll("([^\\.]+)\\..+", "$1").trim());
                    e.put("Value", line.replaceAll(".+:(.+)", "$1").trim());
                } else {
                    e.put("Value", line.trim());
                }
            }
            return elements;
        }, Collections.emptyList());
    }



    public static List<Map<String, String>> getNetworkInformation() {
        return SupplierEx.get(() -> {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            Map<Class<?>, FunctionEx<Object, String>> toStringMAp = new HashMap<>();
            toStringMAp.put(byte[].class, o -> convertToString((byte[]) o));
            List<Map<String, String>> arrayList = new ArrayList<>();
            toStringMAp.put(InetAddress.class, o -> CIDRUtils.convertToString((InetAddress) o));
            while (e.hasMoreElements()) {
                NetworkInterface n = e.nextElement();
                if (n.getInterfaceAddresses().isEmpty() || !n.isUp() || n.isLoopback()
                    || !isLocallyAdministered(n.getHardwareAddress())) {
                    continue;
                }
                Map<String, String> description = ClassReflectionUtils.getDescriptionMap(n, toStringMAp);
                if (!description.isEmpty()) {
                    LOG.trace("{}", description);
                    arrayList.add(description);
                }
            }
            return arrayList;
        }, Collections.emptyList());
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

    private static boolean isLocallyAdministered(byte[] macAddress) {
        return (2 & macAddress[0]) == 0;
    }

}
