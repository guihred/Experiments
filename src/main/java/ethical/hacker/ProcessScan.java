package ethical.hacker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.HasLogging;

public class ProcessScan {

	private static final Logger LOG = HasLogging.log();

    public static List<String> scanProcesses() {

        List<String> executionInfo = ConsoleUtils.executeInConsoleInfo(
                " netstat -aon");
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

    public static List<Map<String, String>> scanCurrentTasks() {
        List<String> processes = ConsoleUtils.executeInConsoleInfo(
                " tasklist /NH /FO CSV");
//        "Nome da imagem","Identificação pessoal","Nome da sessão","Sessão#","Uso de memória"
        return processes.stream().map(e -> e.trim().split("^|\",*\"*"))
                .map(e -> Stream.of(e).map(s -> s.replaceAll("\"", "")).collect(Collectors.toList()))
                .map(key -> {
                    Map<String, String> hashMap = new LinkedHashMap<>();
                    hashMap.put("Nome da imagem", key.get(0));
                    hashMap.put("Identificação pessoal", key.get(1));
                    hashMap.put("Nome da sessão", key.get(2));
                    hashMap.put("Sessão", key.get(3));
                    hashMap.put("Uso de memória", key.get(4));
                    return hashMap;
                }).collect(Collectors.toList());
    }


    public static void main(String[] args) {
        scanProcesses();
    }
}
