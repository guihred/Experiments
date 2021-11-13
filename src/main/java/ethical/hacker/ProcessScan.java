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
import utils.StringSigaUtils;
import utils.ex.HasLogging;

public class ProcessScan {

    private static final Logger LOG = HasLogging.log();

    public static void main(String[] args) {
        LOG.info("{}", scanNetstats());
    }

    public static List<Map<String, String>> scanCurrentTasks() {
        List<String> processes = ConsoleUtils.executeInConsoleInfo(
                "wmic process get Name,ProcessId,ParentProcessId,SessionId,CommandLine,CreationDate"
                        + ",ExecutablePath,Priority,ThreadCount,VirtualSize,WorkingSetSize,ReadOperationCount /FORMAT:csv");
        // Node,Name,ParentProcessId,ProcessId,SessionId
        List<String> title = new ArrayList<>();
        return processes.stream().map(e -> Stream.of(e.trim().split(",")).collect(Collectors.toList()))
                .map(key -> createMap(title, key)).filter(e -> !e.isEmpty())
                .peek(s -> s.entrySet().stream().filter(e -> e.getKey().contains("Size")).collect(Collectors.toList())
                        .stream().forEach(e -> s.compute(e.getKey(), (k, v) -> StringSigaUtils.getFileSize(v))))
                .peek(s -> s.entrySet().stream().filter(e -> e.getKey().contains("Read")).collect(Collectors.toList())
                        .stream()
                        .forEach(e -> s.compute(e.getKey(),
                                (k, v) -> StringSigaUtils.getFileSize(Double.valueOf(v).longValue() * 1024))))
                .peek(s -> s.entrySet().stream().filter(e -> e.getKey().contains("Date")).collect(Collectors.toList())
                        .stream()
                        .forEach(e -> s.compute(e.getKey(),
                                (k, v) -> v.replaceAll("^(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2}).+",
                                        "$3/$2/$1 $4:$5:$6"))))
                .collect(Collectors.toList());
    }

    public static List<Map<String, String>> scanNetstats() {
        List<String> executionInfo = ConsoleUtils.executeInConsoleInfo(" netstat -aon");
        List<String> title = new ArrayList<>();
        return executionInfo.stream().filter(StringUtils::isNotBlank).filter(e -> !"Conexões ativas".equals(e))
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
