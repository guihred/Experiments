package ethical.hacker;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class NameServerLookup {
    private static final Logger LOG = HasLogging.log(NameServerLookup.class);

    public static void getNSInformation(String address) {
        Map<String, String> results = new HashMap<>();
        String nonAuthoritativeAnswer = "\\s+([\\d\\.]+)\\s*";
        results.put(nonAuthoritativeAnswer, "$1");
        String dnsServerName = ".*Address:\\s+([\\d\\.]+)\\s*";
        results.put(dnsServerName, "$1");
        
        
        Map<String, String> executeInConsole = ResourceFXUtils.executeInConsole("nslookup " + address,
                results);
        String alias = "\\s*([\\w\\.]+)\\s*";
        results.put(alias, "$1");
        String mailExchanger = "\\s*responsible mail addr =\\s*(.+)\\s*";
        results.put(mailExchanger, "$1");
        String server = "\\s*Servidor:\\s*([\\w\\.]+)";
        results.put(server, "$1");

        Map<String, String> cname = ResourceFXUtils.executeInConsole("nslookup -type=cname " + address, results);
        LOG.info("DNS Server Name = {}", executeInConsole.get(dnsServerName));
        LOG.info("Non-Authoritative Answer = {}", executeInConsole.get(nonAuthoritativeAnswer));
        LOG.info("CNAME Alias = {}", cname.get(alias));
        LOG.info("Canonical name = {}", cname.get(server));
        LOG.info("MX (Mail Exchanger) = {}", cname.get(mailExchanger));

    }

    public static void main(String[] args) {
        getNSInformation("www.google.com");
    }
}
