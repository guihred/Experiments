package ethical.hacker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ConsumerEx;
import utils.HasLogging;

public class NetworkInformationScanner {

    private static final String METHOD_REGEX = "is(\\w+)|get(\\w+)";
    private static final Logger LOG = HasLogging.log(NetworkInformationScanner.class);

    public static void main(String[] args) throws SocketException {
        displayNetworkInformation();
    }

    public static void displayNetworkInformation() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        List<Method> infoMethod = Stream.of(InetAddress.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers())).filter(m -> m.getName().matches(METHOD_REGEX))
                .filter(m -> m.getParameterCount() == 0).collect(Collectors.toList());
        while (e.hasMoreElements()) {
            NetworkInterface n = e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = ee.nextElement();
				if (i.isLinkLocalAddress()) {
                    continue;
                }

                StringBuilder description = new StringBuilder("\n");
                infoMethod
                        .forEach(ConsumerEx.makeConsumer(o -> {
                            Object invoke = o.invoke(i);

                            description.append("\t");
                            description.append(o.getName().replaceAll(METHOD_REGEX, "$1$2"));
                            description.append(" = ");
                            description.append(invoke);
                            description.append("\n");
                        }));
                LOG.info("{}", description);
            }
        }
    }
}
