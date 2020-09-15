package ethical.hacker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class WebsiteScanner {
    private static final Logger LOG = HasLogging.log();

    private final ObservableMap<String, List<String>> websiteRoutes =
            FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());

    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, Integer> depth = new HashMap<>();

    private int maxSize = 50;

    private long linkMax = 5;

    public WebsiteScanner() {
    }

    public WebsiteScanner(int size, long maxLink) {
        maxSize = size;
        linkMax = maxLink;
    }

    public synchronized List<String> allHosts() {
        return websiteRoutes.entrySet().stream()
                .flatMap(e -> Stream.concat(Stream.of(e.getKey()), e.getValue().stream())).distinct()
                .collect(Collectors.toList());
    }

    public List<Entry<String, List<String>>> entrySet() {
        return websiteRoutes.entrySet().stream().collect(Collectors.toList());
    }

    public ObservableMap<String, List<String>> getLinkNetwork(String url, BiConsumer<String, List<String>> run) {
        if (websiteRoutes.isEmpty()) {
            websiteRoutes.addListener(
                    (Change<? extends String, ? extends List<String>> change) -> scanWebSites(change, run));
        }
        websiteRoutes.clear();
        depth.clear();
        cookies.clear();
        websiteRoutes.put(url, new ArrayList<String>());
        return websiteRoutes;
    }

    public ObservableMap<String, List<String>> getLinkNetwork(String url, ConsumerEx<String> run) {
        return getLinkNetwork(url, (page, links) -> ConsumerEx.makeConsumer(run).accept(page));
    }

    public ObservableMap<String, List<String>> getLinkNetwork(String url, RunnableEx run) {
        return getLinkNetwork(url, (a, b) -> RunnableEx.run(run));
    }

    public void setSize(int size) {
        maxSize = size;
    }

    private synchronized void addToMap(String ip2) {
        if (allHosts().size() < getSize()) {
            websiteRoutes.putIfAbsent(ip2, new ArrayList<>());
        }
    }

    private List<String> getLinks(String url) throws IOException {
        Document document = ExtractUtils.getDocument(url, cookies);
        return getLinks(url, document).stream().limit(linkMax).collect(Collectors.toList());
    }

    private int getSize() {
        return maxSize;
    }

    private void scanWebSites(final Change<? extends String, ? extends List<String>> change,
            BiConsumer<String, List<String>> run) {
        if (change.wasAdded() && depth.getOrDefault(change.getKey(), 0) < 3 && change.getValueAdded().isEmpty()) {
            RunnableEx.runNewThread(() -> RunnableEx.make(() -> {
                String ip = change.getKey();
                if (!websiteRoutes.get(ip).isEmpty()) {
                    return;
                }
                List<String> links = getLinks(ip);
                LOG.info("{} Found {} ", ip, links);
                websiteRoutes.get(ip).addAll(links);
                CommonsFX.runInPlatform(() -> run.accept(ip, links));
                if (allHosts().size() < getSize()) {
                    for (int i = 0; i < links.size(); i++) {
                        String ip2 = links.get(i);
                        depth.put(ip2, depth.getOrDefault(ip, 0) + 1);
                        addToMap(ip2);
                    }
                }
            }, e -> LOG.info("ERRO {} {}", change.getKey(),
                    FunctionEx.mapIf(e.getCause(), Throwable::getMessage, e.getMessage()))).run());
        }
    }

    public static List<String> getLinks(String url, Document document) throws MalformedURLException {
        URL url2 = new URL(url);
        SimpleStringProperty currentDomain = new SimpleStringProperty(url2.getProtocol() + "://" + url2.getHost());
        return document.select("a").stream().map(e -> e.attr("abs:href"))
                .filter(StringUtils::isNotBlank).filter(s -> !s.contains("#"))
                .map(e -> ExtractUtils.addDomain(currentDomain, e)).filter(s -> !Objects.equals(s, url)).distinct()
                .collect(Collectors.toList());
    }

}
