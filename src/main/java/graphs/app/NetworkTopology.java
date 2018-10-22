package graphs.app;

import ethical.hacker.PortScanner;
import ethical.hacker.TracerouteScanner;
import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;

public class NetworkTopology extends BaseTopology {

	private ObservableMap<String, List<String>> scanNetworkRoutes;
	private ObservableMap<String, List<String>> scanPossibleOSes = FXCollections.observableHashMap();
    public NetworkTopology(Graph graph) {
        super(graph, "Network");
		scanPossibleOSes.addListener(this::onNewOS);

	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();

        if (scanNetworkRoutes == null) {
			scanNetworkRoutes = TracerouteScanner.scanNetworkRoutes(TracerouteScanner.NETWORK_ADDRESS);
			scanNetworkRoutes.addListener(
					this::scanOSes);
        }

        List<String> collect = scanNetworkRoutes.values().stream().flatMap(List<String>::stream).distinct()
                .collect(Collectors.toList());

        graph.getModel().addCell("localhost", CellType.RECTANGLE);

        for (String packageName : collect) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
		}
		for (List<String> hops : scanNetworkRoutes.values()) {
            String currentCell = "localhost";
			for (String hop : hops) {
                graph.getModel().addEdge(currentCell, hop, 1);
                currentCell = hop;
            }
        }
        List<Cell> cells = graph.getModel().getAddedCells();
		scanPossibleOSes.forEach(this::addDescription);
		RandomLayout.layoutRandom(cells, graph.getModel().getAddedEdges());

        graph.endUpdate();


    }

	private void onNewOS(Change<? extends String, ? extends List<String>> change) {
		addDescription(change.getKey(), change.getValueAdded());
	}

	private void addDescription(String ip, List<String> valueAdded) {
		if (!valueAdded.isEmpty()) {
			String collect = valueAdded.stream().collect(Collectors.joining("\n"));
			Cell cell = graph.getModel().getCell(ip);
			if (cell != null && !cell.getText().contains(collect)) {
				cell.addText(collect);
			}
		}
	}

	private void scanOSes(Change<? extends String, ? extends List<String>> change) {
		String ip = change.getKey();
		Platform.runLater(() -> {
			execute();
			new Thread(() -> {
				Map<String, List<String>> scanOSes = PortScanner.scanPossibleOSes(ip);
				scanPossibleOSes.putAll(scanOSes);
			}).start();
		});
	}




}