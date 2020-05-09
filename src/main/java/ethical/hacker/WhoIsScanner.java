package ethical.hacker;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.RunnableEx;

public class WhoIsScanner {
    private static final Logger LOG = HasLogging.log();

    private final Map<String, String> cookies = new HashMap<>();

    public WhoIsScanner() {
    }

    public ObservableList<Map<String, String>> scanIps(String ip) {
        ObservableList<Map<String, String>> observableArrayList = FXCollections.observableArrayList();
        String[] split = ip.split("[\\s,;]+");
        RunnableEx.runNewThread(() -> {
            for (String string : split) {
                RunnableEx.run(() -> observableArrayList.add(whoIsScan(string)));
            }
        });

        return observableArrayList;
    }
    private Map<String, String> whoIsScan(String ip) throws IOException {
        String scanIP = "http://isc.sans.edu/api/ip/" + ip;
        Map<String, String> map = new LinkedHashMap<>();
        ExtractUtils.getDocument(scanIP, cookies).getElementsByTag("ip")
                .forEach(e -> e.children().forEach(m -> map.put(m.tagName(), m.text())));
        LOG.info("{}", map);
        return map;
    }

    public static void main(String[] args) {
        String ip = "74.125.210.93\n66.249.88.223\n66.249.88.10\n66.249.83.125\n66.249.83.39\n"
                + "66.249.83.38\n66.102.8.228\n66.102.8.204\n66.102.8.22\n66.102.8.7\n"
                + "66.102.8.5\n66.102.8.1\n66.102.6.198\n66.102.6.167\n";
        new WhoIsScanner().scanIps(ip);

    }
}