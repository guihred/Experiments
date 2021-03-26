package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModel;
import java.util.List;
import javafx.beans.NamedArg;

public class LayerLayout extends Layout {

    public LayerLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
        double w = graph.getScrollPane().getViewportBounds().getWidth() / 2;
        double h = graph.getScrollPane().getViewportBounds().getHeight();
        layoutInLayers(cells, model.getAllEdges(), w, h);

    }

    public static void layoutInLayers(List<Cell> cells, List<Edge> addedEdges, double center, double h) {

        List<List<Cell>> orderedCellGroups = LayerSplitter.getLayers(cells, addedEdges);
        final double bound = orderedCellGroups.stream()
                .mapToDouble(l -> l.stream().mapToDouble(Cell::getHeight).max().orElse(0)).max().orElse(0);
        double sumWidth = orderedCellGroups.stream().mapToDouble(l -> l.stream().mapToDouble(Cell::getWidth).sum())
                .max().orElse(0);
        double sum2 = Math.max(sumWidth, center);
        double layerHeight = Math.min(h / (orderedCellGroups.size() + 1), bound + 50);
        double y = layerHeight;
        for (int j = 0; j < orderedCellGroups.size(); j++) {
            List<Cell> list = orderedCellGroups.get(j);
            double sum = list.stream().mapToDouble(Cell::getWidth).sum();
            double xStep = 5;
            double x = sum2 / 2 - sum / 2;

            for (int i = 0; i < list.size(); i++) {
                Cell cell = list.get(i);
                if (list.size() == 1) {
                    x += cell.getWidth() / 4 * (j % 2 == 0 ? 1 : -1);
                }
                cell.relocate(x, y);

                x += cell.getWidth() + xStep;
            }
            y += layerHeight;
        }
    }

}