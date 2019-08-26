package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModelAlgorithms;
import java.util.List;
import javafx.beans.NamedArg;

public class RandomLayout implements Layout {

    private final Graph graph;

    public RandomLayout(@NamedArg("graph") Graph graph) {
        this.graph = graph;
    }

    @Override
    public void execute() {
        List<Cell> cells = graph.getModel().getAllCells();
        List<Edge> allEdges = graph.getModel().getAllEdges();
        graph.clean();
        layoutRandom(cells, allEdges, graph.getScrollPane().getWidth());
    }

    public Graph getGraph() {
        return graph;
    }

    public static void layoutRandom(List<Cell> cells, List<Edge> allEdges, double width) {
        double bound = width;

        for (Cell cell : cells) {
            int i = 0;
            int minIntersection = Integer.MAX_VALUE;
            double miny = 0;
            double minx = 0;
            List<Edge> edges = GraphModelAlgorithms.edges(cell, allEdges);
            do {
                double x = BaseTopology.rndPositive(bound);
                double y = BaseTopology.rndPositive(bound);
                cell.relocate(x, y);
                int j = 0;
                for (Edge edge : edges) {
                    j += GraphModelAlgorithms.intersection(allEdges, edge);
                }
                if (minIntersection > j && !GraphModelAlgorithms.anyIntersection(cells, cell)) {
                    minx = x;
                    miny = y;
                    minIntersection = j;
                }
            } while (minIntersection == Integer.MAX_VALUE
                || i++ < 10 && GraphModelAlgorithms.anyIntersection(edges, allEdges));
            cell.relocate(minx, miny);
        }
    }

}