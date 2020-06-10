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
        String ip =
                "49.89.254.226,49.89.255.113,114.239.52.44,114.239.53.58,114.239.53.124,114.239.54.144,114.239.104.152";
        new WhoIsScanner().scanIps(ip);

    }
}