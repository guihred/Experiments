package graphs.app;

import ethical.hacker.PortScanner;
import ethical.hacker.TracerouteScanner;
import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;

public class NetworkTopology extends BaseTopology {

    private ObservableMap<String, List<String>> scanNetworkRoutes;
    private ObservableMap<String, List<String>> scanPossibleOSes = FXCollections.observableHashMap();


    public NetworkTopology(Graph graph) {
        super(graph, "Network");

    }

    @Override
    public synchronized void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();

        if (scanNetworkRoutes == null) {
            scanNetworkRoutes = TracerouteScanner.scanNetworkRoutes(TracerouteScanner.NETWORK_ADDRESS);
            scanNetworkRoutes.addListener(this::scanOSes);
        }

        List<String> collect = scanNetworkRoutes.values().stream().flatMap(List<String>::stream).distinct()
                .collect(Collectors.toList());

        graph.getModel().addCell("localhost", CellType.RECTANGLE);

        for (String packageName : collect) {
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
        scanPossibleOSes.forEach(this::addDescription);
        graph.endUpdate();
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
                graph.getScrollPane().getWidth() / 3);


    }

    private void addDescription(String ip, List<String> valueAdded) {
        if (!valueAdded.isEmpty()) {
            String collect = valueAdded.stream().limit(3).collect(Collectors.joining("\n"));
            Cell cell = graph.getModel().getCell(ip);
            if (cell != null && !cell.getText().contains(collect)) {
                cell.addText(collect);
            }
        }
    }

    private InvalidationListener onChange(ObservableMap<String, List<String>> scanOSes) {
        return observable -> {
            scanPossibleOSes.putAll(scanOSes);
            Platform.runLater(this::execute);
        };
    }

    private void scanOSes(Change<? extends String, ? extends List<String>> change) {
        Platform.runLater(this::execute);
        String ip = change.getKey();
        if (!change.wasRemoved()) {
            new Thread(() -> {
                ObservableMap<String, List<String>> scanOSes = PortScanner.scanPossibleOSes(ip);
                scanOSes.addListener(onChange(scanOSes));
            }).start();
        }
    }

}