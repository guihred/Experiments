package ethical.hacker;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.RunnableEx;

public class WhoIsScanner {
    private static final Logger LOG = HasLogging.log();

    private final Map<String, String> cookies;

    public WhoIsScanner() {
        cookies = new HashMap<>();
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

    public Map<String, String> whoIsScan(String ip) throws IOException {
        String scanIP = "http://isc.sans.edu/api/ip/" + ip;
        Map<String, String> map = new LinkedHashMap<>();
        ExtractUtils.getDocument(scanIP, cookies).getElementsByTag("ip")
                .forEach(e -> e.children()
                        .forEach(m -> map.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        LOG.info("{}", map);

        return map;
    }

    public static void main(String[] args) {
        String ip =
                "187.22.201.244";
        new WhoIsScanner().scanIps(ip);

    }
}