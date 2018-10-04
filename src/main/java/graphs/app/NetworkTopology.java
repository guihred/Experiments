package graphs.app;

import ethical.hacker.TracerouteScanner;
import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkTopology extends BaseTopology {

	private Map<String, List<String>> scanNetworkRoutes;

    public NetworkTopology(Graph graph) {
        super(graph, "Network");
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();

        if (scanNetworkRoutes == null) {
            scanNetworkRoutes = TracerouteScanner.scanNetworkRoutes("10.69.64.31/26");
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