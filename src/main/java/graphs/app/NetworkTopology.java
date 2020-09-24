package graphs.app;

import ethical.hacker.PortScanner;
import ethical.hacker.TracerouteScanner;
import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class NetworkTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();
    private ObservableMap<String, List<String>> scanNetworkRoutes;

    private ObservableMap<String, List<String>> scanPossibleOSes = FXCollections.observableHashMap();

    private final StringProperty networkAddress = new SimpleStringProperty(TracerouteScanner.NETWORK_ADDRESS);

    public NetworkTopology(@NamedArg("graph") Graph graph) {
        super(graph);
        networkAddress.addListener(o -> scanNetworkRoutes = null);
    }

    @Override
    public synchronized void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();

        if (scanNetworkRoutes == null) {
            scanNetworkRoutes = TracerouteScanner.scanNetworkRoutes(networkAddress.get());
            scanNetworkRoutes.addListener(this::scanOSes);
        }

        List<String> hosts = allHosts();

        graph.getModel().addCell("localhost", CellType.RECTANGLE);

        for (String packageName : hosts) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }
        List<List<String>> values = scanNetworkRoutes.values().stream().collect(Collectors.toList());
        for (int l = 0; l < values.size(); l++) {
            List<String> hops = values.get(l);
            String currentCell = "localhost";
            for (String hop : hops) {
                graph.getModel().addEdge(currentCell, hop, 1);
                currentCell = hop;
            }
        }
        synchronized (scanPossibleOSes) {
            scanPossibleOSes.forEach(this::addDescription);
        }
        graph.endUpdate();
        LayerLayout.layoutInLayers(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
            graph.getScrollPane().getViewportBounds().getWidth() / 2,
            graph.getScrollPane().getViewportBounds().getHeight());

    }


    public StringProperty networkAddressProperty() {
        return networkAddress;
    }

    private void addDescription(final String ip, final List<String> valueAdded) {
        if (!valueAdded.isEmpty()) {
            String firstDescriptions = valueAdded.stream().limit(3).collect(Collectors.joining("\n"));
            Cell cell = graph.getModel().getCell(ip);
            if (cell != null && !cell.getText().contains(firstDescriptions)) {
                cell.addText(firstDescriptions);
            }
        }
    }

    private List<String> allHosts() {

        try {
            return scanNetworkRoutes.values().stream().flatMap(List<String>::stream).distinct()
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.trace(" CONCURRENCY ISSUE", e);
            return scanNetworkRoutes.values().stream().flatMap(List<String>::stream).distinct()
                .collect(Collectors.toList());
        }
    }

    private InvalidationListener onChange(final ObservableMap<String, List<String>> scanOSes) {
        return observable -> {
            synchronized (scanPossibleOSes) {
                scanPossibleOSes.putAll(scanOSes);
            }
            CommonsFX.runInPlatform(this::execute);
        };
    }

    private void scanOSes(final Change<? extends String, ? extends List<String>> change) {
        CommonsFX.runInPlatform(this::execute);
        String ip = change.getKey();
        if (!change.wasRemoved()) {
            RunnableEx.runNewThread(() -> {
                ObservableMap<String, List<String>> scanOSes = PortScanner.scanPossibleOSes(ip);
                scanOSes.addListener(onChange(scanOSes));
            });
        }
    }

}