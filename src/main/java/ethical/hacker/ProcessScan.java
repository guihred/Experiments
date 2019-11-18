package ethical.hacker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public class ProcessScan {

    private static final Logger LOG = HasLogging.log();

    public static void main(String[] args) {
        LOG.info("{}", scanNetstats());
    }

    public static List<Map<String, String>> scanCurrentTasks() {
        List<String> processes = ConsoleUtils
            .executeInConsoleInfo("wmic process get Name,ProcessId,ParentProcessId,SessionId /FORMAT:csv");
//        Node,Name,ParentProcessId,ProcessId,SessionId
        List<String> title = new ArrayList<>();
        return processes.stream().map(e -> e.trim().split(",")).map(e -> Stream.of(e).collect(Collectors.toList()))
            .map(key -> createMap(title, key)).filter(e -> !e.isEmpty()).collect(Collectors.toList());
    }

    public static List<Map<String, String>> scanNetstats() {
        List<String> executionInfo = ConsoleUtils.executeInConsoleInfo(" netstat -aon");
        List<String> title = new ArrayList<>();
        return executionInfo.stream().filter(StringUtils::isNotBlank)
            .filter(e -> !"Conexões ativas".equals(e))
            .map(String::trim).filter(e -> !e.startsWith("UDP")).map(e -> e.split("\\s+(?=[A-Z0-9\\[\\*])"))
            .map(e -> Stream.of(e).collect(Collectors.toList())).map(key -> createMap(title, key))
            .filter(e -> !e.isEmpty()).collect(Collectors.toList());

    }

    private static Map<String, String> createMap(List<String> title, List<String> key) {
        Map<String, String> hashMap = new LinkedHashMap<>();
        if (title.isEmpty() && key.size() > 1) {
            title.addAll(key);
        } else if (title.size() == key.size()) {
            for (int i = 0; i < title.size(); i++) {
                hashMap.put(title.get(i), key.get(i));
            }
        }
        return hashMap;
    }

}
