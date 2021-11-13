package graphs.app;

import ethical.hacker.ProcessScan;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.Map;
import javafx.beans.NamedArg;
import javafx.scene.paint.Color;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.DataframeUtils;
import ml.graph.ColorPattern;
import utils.StringSigaUtils;
import utils.ex.HasLogging;

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
        DataframeML dataframeML = new DataframeML();
        hosts.forEach(m -> {
            dataframeML.add(m);
        });
        dataframeML.<String, Long>map("ReadOperationCount", Long.class, e -> StringSigaUtils.strToFileSize(e));
        DataframeStatisticAccumulator acc = DataframeUtils.makeStats(dataframeML).get("ReadOperationCount");

        HasLogging.log().info(DataframeUtils.toString(dataframeML, 100));
        for (Map<String, String> packageName : hosts) {
            String string = packageName.get("ReadOperationCount");
            Color colorForValue = ColorPattern.HUE.getColorForValue(StringSigaUtils.strToFileSize(string),
                    (Double) acc.getMin(), (Double) acc.getMax());
            graph.getModel().addCell(packageName.get("ProcessId"), CellType.RECTANGLE).addText(packageName.get("Name"))
                    .addText(packageName.get("Status")).setColor(colorForValue);

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