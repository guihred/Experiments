package ethical.hacker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public class ProcessScan {

    private static final Logger LOG = HasLogging.log();

    public static void main(String[] args) {
        scanProcesses();
    }

    public static List<Map<String, String>> scanCurrentTasks() {
        List<String> processes = ConsoleUtils
            .executeInConsoleInfo("wmic process get Name,ProcessId,ParentProcessId,SessionId /FORMAT:csv");
//        Node,Name,ParentProcessId,ProcessId,SessionId
        List<String> title = new ArrayList<>();
        return processes.stream().map(e -> e.trim().split(",")).map(e -> Stream.of(e).collect(Collectors.toList()))
            .map(key -> createMap(title, key)).filter(e -> !e.isEmpty()).collect(Collectors.toList());
    }

    public static List<String> scanProcesses() {
        List<String> executionInfo = ConsoleUtils.executeInConsoleInfo(" netstat -aon");
        List<String[]> ports = new ArrayList<>();
        for (String string : executionInfo) {
            if (string.matches("  [^P].+")) {
                String[] fields = string.trim().split("\\s+");
                String field = Arrays.toString(fields);
                LOG.info("{}", field);
                ports.add(fields);
            }
        }
        return executionInfo;
    }

    private static Map<String, String> createMap(List<String> title, List<String> key) {
        Map<String, String> hashMap = new LinkedHashMap<>();
        if (title.isEmpty() && key.size() > 1) {
            title.addAll(key);
        } else if (key.size() > 1) {
            for (int i = 0; i < title.size(); i++) {
                hashMap.put(title.get(i), key.get(i));
            }
        }
        return hashMap;
    }
}
