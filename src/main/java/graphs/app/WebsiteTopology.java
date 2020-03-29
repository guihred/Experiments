package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FunctionEx;
import utils.HasLogging;
import utils.RunnableEx;

public class WebsiteTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();

    private final ObservableMap<String, List<String>> websiteRoutes =
            FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
    private final StringProperty websiteAddress = new SimpleStringProperty("https://pt.wikipedia.org");

    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, Integer> depth = new HashMap<>();

    public WebsiteTopology(@NamedArg("graph") Graph graph) {
        super(graph);
        websiteAddress.addListener(o -> {
            websiteRoutes.clear();
            depth.clear();
            cookies.clear();
        });
    }

    @Override
    public synchronized void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        getLinkNetwork();
        for (String packageName : allHosts()) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }

        List<Entry<String, List<String>>> values = entrySet();
        for (int l = 0; l < values.size(); l++) {
            Entry<String, List<String>> hops = values.get(l);
            for (String hop : hops.getValue()) {
                graph.getModel().addEdge(hops.getKey(), hop, 1);
            }
        }
        graph.endUpdate();
        double min =
                Math.min(graph.getScrollPane().getViewportBounds().getWidth(), graph.getScrollPane().getWidth() / 2);
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), min);

    }

    public StringProperty websiteProperty() {
        return websiteAddress;
    }

    private synchronized void addToMap(String ip2) {
        if (allHosts().size() < getSize()) {

            websiteRoutes.putIfAbsent(ip2, new ArrayList<>());
        }
    }

    private synchronized List<String> allHosts() {
        return websiteRoutes.entrySet().stream()
                .flatMap(e -> Stream.concat(Stream.of(e.getKey()), e.getValue().stream())).distinct()
                .collect(Collectors.toList());
    }

    private List<Entry<String, List<String>>> entrySet() {
        return websiteRoutes.entrySet().stream().collect(Collectors.toList());
    }

    private void getLinkNetwork() {
        String url = websiteAddress.get();
        if (websiteRoutes.isEmpty()) {
            websiteRoutes
                    .addListener((Change<? extends String, ? extends List<String>> change) -> scanWebSites(change,
                            this::execute));
            websiteRoutes.put(url, new ArrayList<String>());
        }
    }

    private List<String> getLinks(String url) throws IOException {
        SimpleStringProperty currentDomain = new SimpleStringProperty("");
        URL url2 = new URL(url);
        currentDomain.set(url2.getProtocol() + "://" + url2.getHost());

        return ExtractUtils.getDocument(url, cookies).select("a").stream().map(e -> e.attr("href"))
                .filter(StringUtils::isNotBlank).filter(s -> !s.contains("#"))
                .map(e -> ExtractUtils.addDomain(currentDomain, e)).filter(s -> !Objects.equals(s, url))
                .filter(s -> s.contains(url2.getHost())).distinct().limit(5).collect(Collectors.toList());
    }

    private void scanWebSites(final Change<? extends String, ? extends List<String>> change, RunnableEx run) {
        if (change.wasAdded() && depth.getOrDefault(change.getKey(), 0) < 3 && change.getValueAdded().isEmpty()) {
            RunnableEx.runNewThread(() -> RunnableEx.make(() -> {
                String ip = change.getKey();
                if (!websiteRoutes.get(ip).isEmpty()) {
                    return;
                }
                List<String> links = getLinks(ip);
                LOG.info("{} Found {} ", ip, links);
                websiteRoutes.get(ip).addAll(links);
                RunnableEx.runInPlatform(run);
                if (allHosts().size() < getSize()) {
                    for (int i = 0; i < links.size(); i++) {
                        String ip2 = links.get(i);
                        depth.put(ip2, depth.getOrDefault(ip, 0) + 1);
                        addToMap(ip2);
                    }
                }
            }, e -> LOG.info("ERRO {} {}", change.getKey(),
                    FunctionEx.mapIf(e.getCause(), Throwable::getMessage, e.getMessage())))
                    .run());
        }
    }

}