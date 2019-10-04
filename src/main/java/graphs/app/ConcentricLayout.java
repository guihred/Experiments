package graphs.app;

import graphs.entities.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
        double w = graph.getScrollPane().getViewportBounds().getWidth() / 2;
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

		int size = cellsGroups.stream().mapToInt(List<Cell>::size).max().orElse(cellsGroups.size());
		int index = IntStream.range(0, cellsGroups.size()).filter(i -> cellsGroups.get(i).size() == size).findFirst()
            .orElse(0) + 1;
        double minLength = cellsGroups.stream().flatMap(List<Cell>::stream).map(Cell::getBoundsInParent)
            .mapToDouble(value -> Math.max(value.getWidth(), value.getHeight()) / 2).max().orElse(20);
        int mul1 = Math.max(getMul(cellsGroups, invert, index) - 1, 1);
        double maxHeight = Math.max(getRadius(minLength, size, mul1) / mul1, minLength);
        for (int i = 0; i < cellsGroups.size(); i++) {
            List<Cell> list = cellsGroups.get(i);
            final double d = 180 * (1 - 1. / cells.size());
            int mul = getMul(cellsGroups, invert, i);
            List<Cell> collect = cellsGroups.stream().skip(i).flatMap(List<Cell>::stream).collect(Collectors.toList());
            list.sort(Comparator.comparing(e -> GraphModelAlgorithms.edgesNumber(e, allEdges, collect)));
            CircleLayout.generateCircle(list, center, center, d / list.size() * i, maxHeight * mul);
        }
    }

    private static int getMul(List<List<Cell>> cellsGroups, boolean invert, int i) {
        if (cellsGroups.get(invert ? cellsGroups.size() - 1 : 0).size() == 1) {
            return invert ? cellsGroups.size() - i - 1 : i;
        }
        return invert ? cellsGroups.size() - i : i + 1;
    }

	private static double getRadius(double orElse, int size, int mul) {
        return mul * orElse / (size <= 2 ? 1 : Math.abs(Math.tan(Math.PI * 2 / size)));
    }

}
