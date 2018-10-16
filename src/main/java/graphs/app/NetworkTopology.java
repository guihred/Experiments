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
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;

public class NetworkTopology extends BaseTopology {

	private ObservableMap<String, List<String>> scanNetworkRoutes;

    public NetworkTopology(Graph graph) {
        super(graph, "Network");
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
        ConcentricLayout.layoutConcentric(cells, graph.getModel().getAddedEdges());
        graph.endUpdate();

    }

	private void scanOSes(Change<? extends String, ? extends List<String>> change) {
		String ip = change.getKey();
		Platform.runLater(() -> {
			execute();
			new Thread(() -> {
				Map<String, List<String>> scanPossibleOSes = PortScanner.scanPossibleOSes(ip);
				Platform.runLater(() -> {
					if (!scanPossibleOSes.isEmpty() && scanPossibleOSes.containsKey(ip)
							&& !scanPossibleOSes.get(ip).isEmpty()) {
						graph.getModel().getCell(ip)
								.addText(scanPossibleOSes.get(ip).stream().collect(Collectors.joining("\n")));
					}
				});
			}).start();
		});
	}




}