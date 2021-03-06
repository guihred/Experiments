package ethical.hacker;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.ex.HasLogging;

public final class PingTraceRoute {
    private static final Logger LOG = HasLogging.log();

    private PingTraceRoute() {
    }

    public static Map<String, String> getInformation(String address) {
        Map<String, String> responses = new HashMap<>();
        String ipRegex = ".+\\[(.+)\\].+";
        responses.put(ipRegex, "$1");// IP
        String sent = ".+Enviados = (\\d+),.+";
        responses.put(sent, "$1");// SENT
        String received = ".+Recebidos = (\\d+),.+";
        responses.put(received, "$1");// RECEIVED
        String ttl = ".+TTL=(\\d+)[^\\d]*+";
        responses.put(ttl, "$1");// RECEIVED
        String lost = ".+Perdidos = (\\d+).+";
        responses.put(lost, "$1");// RECEIVED
        Map<String, String> executeInConsole =
                ConsoleUtils.executeInConsole("ping " + address + " -f -n 1 ", responses);
        // Pacotes: Enviados = 4, Recebidos = 0, Perdidos = 4
        Map<String, String> info = new LinkedHashMap<>();

        String arg = executeInConsole.get(received);
        info.put("IP", executeInConsole.get(ipRegex));
        info.put("SENT", executeInConsole.get(sent));
        info.put("RECEIVED", arg);
        info.put("LOST", executeInConsole.get(lost));
        info.put("TTL", executeInConsole.get(ttl));
        if (!"0".equals(arg)) {
            List<String> traceRoute = traceRoute(address);
            info.put("ROUTE", traceRoute.stream().collect(Collectors.joining("\n")));
            info.put("HOPS", Integer.toString(traceRoute.size()));
        }

        LOG.info("Ping Trace = {}", info);
        return info;
    }

    public static List<String> traceRoute(String address) {
        return traceRoute(new ArrayList<>(), address, 1);
    }

    private static List<String> traceRoute(List<String> n, String address, int i) {
        Map<String, String> responses = new HashMap<>();
        String route = ".*Resposta de ([\\d\\.]+):[^\\d]+";
        responses.put(route, "$1");// RECEIVED
        String ipRegex = "Disparando ([\\d\\.]+) .+";
        responses.put(ipRegex, "$1");// IP
        Map<String, String> executeInConsole =
                ConsoleUtils.executeInConsole("ping " + address + " -i " + i + " -n 1 ", responses);

        String lineFromConsole = executeInConsole.get(route);
        if (lineFromConsole != null && !lineFromConsole.matches("Host de destino inacess.+") && i < 100
                && !n.contains(lineFromConsole)) {
            n.add(lineFromConsole);
            return traceRoute(n, address, i + 1);
        }
        n.add(executeInConsole.get(ipRegex));
        return n;
    }

}
