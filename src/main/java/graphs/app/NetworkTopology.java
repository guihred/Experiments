package graphs.app;

import ethical.hacker.TracerouteScanner;
import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
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
					(Change<? extends String, ? extends List<String>> change) -> Platform.runLater(this::execute));
        }

        List<String> collect = scanNetworkRoutes.values().stream().flatMap(List<String>::stream).distinct()
                .collect(Collectors.toList());

        graph.getModel().addCell("localhost", CellType.RECTANGLE);

        for (String packageName : collect) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
		}
        for (String cellId : scanNetworkRoutes.keySet()) {
            String currentCell = "localhost";
            List<String> map = scanNetworkRoutes.get(cellId);
            for (String hop : map) {
                graph.getModel().addEdge(currentCell, hop, 1);
                currentCell = hop;
            }

        }
        List<Cell> cells = graph.getModel().getAddedCells();
        ConcentricLayout.layoutConcentric(cells, graph.getModel().getAddedEdges());
        graph.endUpdate();

    }




}