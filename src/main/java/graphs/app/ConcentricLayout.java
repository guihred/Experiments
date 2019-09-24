package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModel;
import java.util.List;
import java.util.stream.IntStream;
import javafx.beans.NamedArg;

public class ConcentricLayout extends Layout {


    public ConcentricLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
        List<Edge> allEdges = model.getAllEdges();
        double w = graph.getScrollPane().getWidth() / 2;
        layoutConcentric(cells, allEdges, w);
    }


    public static void layoutConcentric(List<Cell> cells, List<Edge> allEdges, double center) {
        List<List<Cell>> cellsGroups = LayerSplitter.getLayers(cells, allEdges);
        if (cellsGroups.isEmpty()) {
            return;
        }
        int count = IntStream.range(0, cellsGroups.size() / 2)
            .map(i -> cellsGroups.get(i).size() - cellsGroups.get(cellsGroups.size() - i - 1).size()).sum();
        boolean invert = count > 0;

        double orElse = Math.max(center / cellsGroups.size(),
            cellsGroups.stream().flatMap(List<Cell>::stream).mapToDouble(Cell::getHeight).max().orElse(20));
        for (int i = 0; i < cellsGroups.size(); i++) {
            List<Cell> list = cellsGroups.get(i);
            final double d = 180 - 180. / cells.size();
            int mul = invert ? cellsGroups.size() - i : i;
            double bound = mul == 1 && list.size() == 1 ? 0
                : mul * orElse / (list.size() <= 2 ? 1 : Math.tan(Math.PI * 2 / list.size()));
            CircleLayout.generateCircle(list, allEdges, center, center, d / list.size() * i, bound);
        }
    }

}
