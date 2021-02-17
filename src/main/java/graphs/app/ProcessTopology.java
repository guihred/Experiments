package graphs.app;

import ethical.hacker.ProcessScan;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.Map;
import javafx.beans.NamedArg;

public class ProcessTopology extends BaseTopology {

    public ProcessTopology(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public synchronized void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();

        List<Map<String, String>> hosts = ProcessScan.scanCurrentTasks();
        for (Map<String, String> packageName : hosts) {
            graph.getModel().addCell(packageName.get("ProcessId"), CellType.RECTANGLE).addText(packageName.get("Name"))
                    .addText(packageName.get("Status"));
        }
        for (Map<String, String> packageName : hosts) {
            graph.getModel().addEdge(packageName.get("ParentProcessId"), packageName.get("ProcessId"), 1);
        }
        graph.endUpdate();
        LayerLayout.layoutInLayers(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
            graph.getScrollPane().getViewportBounds().getWidth() / 2,
            graph.getScrollPane().getViewportBounds().getHeight());

    }

}