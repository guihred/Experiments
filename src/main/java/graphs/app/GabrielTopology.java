package graphs.app;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.EdgeDistancePack;
import graphs.entities.Graph;
import java.util.List;
import javafx.beans.NamedArg;

public class GabrielTopology extends BaseTopology {

    public GabrielTopology(@NamedArg("size") int size, @NamedArg("graph") Graph graph) {
        super(graph, "Gabriel", size);
    }

    @Override
    public void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        int bound = 100;
        double angle = newAngle();
        double y = bound * Math.sin(Math.toRadians(angle));
        double x = bound * Math.cos(Math.toRadians(angle));
        for (int i = 0; i < getSize(); i++) {
            Cell cell = graph.getModel().addCell(BaseTopology.identifier(i), CellType.CIRCLE);
            angle = newAngle();
            x += bound * Math.cos(Math.toRadians(angle));
            y += bound * Math.sin(Math.toRadians(angle));
            cell.relocate(x, y);
        }
        List<Cell> cells = graph.getModel().getAddedCells();
        for (Cell cell : cells) {
            double y1 = cell.getLayoutY();
            double x1 = cell.getLayoutX();
            for (Cell cell2 : cells) {
                double m = (x1 + cell2.getLayoutX()) / 2;
                double n = (y1 + cell2.getLayoutY()) / 2;
                double r = EdgeDistancePack.distance(x1, cell2.getLayoutX(), y1, cell2.getLayoutY());
                if (cells.stream().filter(c -> c != cell && c != cell2)
                    .noneMatch(c -> EdgeDistancePack.distance(m, c.getLayoutX(), n, c.getLayoutY()) <= r / 2)) {
                    graph.getModel().addBiEdge(cell.getCellId(), cell2.getCellId(), 1);
                }

            }
        }
        graph.endUpdate();

    }




}