package ethical.hacker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ProcessScan {

    private static final Logger LOG = HasLogging.log(ProcessScan.class);

    public static void scanProcesses() {

        List<String> executionInfo = ResourceFXUtils.executeInConsoleInfo(
                " netstat -aon");
        List<String[]> ports = new ArrayList<>();
        for (String string : executionInfo) {
            if (string.matches("  [^P].+")) {
                String[] fields = string.split("\\s+");
                String field = Arrays.toString(fields);
                LOG.info("{}", field);
                ports.add(fields);
            }
        }
        List<String> processes= ResourceFXUtils.executeInConsoleInfo(
                " tasklist /NH /FO CSV");
//        "Nome da imagem","Identificação pessoal","Nome da sessão","Sessão#","Uso de memória"
        List<String[]> collect = processes.stream().map(e -> e.split("\",*\"*")).collect(Collectors.toList());
        LOG.info("{}", collect);
        

    }

    public static void main(String[] args) {
        scanProcesses();
    }
}
