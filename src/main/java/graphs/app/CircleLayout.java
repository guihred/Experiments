package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;

public class CircleLayout extends Layout {

    public CircleLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
        double width = graph.getScrollPane().getViewportBounds().getWidth();
        double height = graph.getScrollPane().getViewportBounds().getHeight();
        generateCircle(cells, model.getAllEdges(), width / 2, height / 2, 2);
    }

    public static void generateCircle(Collection<Cell> cells, double centerX, double centerY, double startAngle,
        double bound) {
        int size = cells.size();
        double step = 360.0 / size;
        double angle = startAngle;
        for (Cell cell : cells) {
            double x = Math.cos(Math.toRadians(angle)) * bound;
            double y = Math.sin(Math.toRadians(angle)) * bound;
            cell.relocate(x + centerX, y + centerY);
            angle += step;
        }
    }

    public static void generateCircle(List<Cell> cells, List<Edge> allEdges, double centerX, double centerY, int mul) {
        int bound = radius(cells.size(), mul,
            cells.stream().mapToDouble(value -> value.getBoundsInLocal().getWidth()).max().orElse(20));

        List<Cell> collect =
                LayerSplitter.getLayers(cells, allEdges).stream().flatMap(List<Cell>::stream)
                        .collect(Collectors.toList());
        allEdges.forEach(e -> e.setSelected(false));
        generateCircle(collect, centerX, centerY, 0, bound);
    }

    public static int radius(int size2, int mul, double cellBound) {
        int i = size2 / 30 + 1;
        return (int) (cellBound * i * mul);
    }

}