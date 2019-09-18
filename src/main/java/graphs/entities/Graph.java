package graphs.entities;

import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import utils.ZoomableScrollPane;
public class Graph {

	private Group canvas;

	private Pane cellLayer;

	private GraphModel model;

	private MouseGestures mouseGestures;

	private ZoomableScrollPane scrollPane;

	public Graph() {

		model = new GraphModel();

		canvas = new Group();
		cellLayer = new CellLayer();

		canvas.getChildren().add(cellLayer);


		scrollPane = new ZoomableScrollPane(canvas);
        mouseGestures = new MouseGestures(scrollPane.scaleValueProperty());

		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

	}

	public void clean() {
		getCellLayer().getChildren().removeIf(VoronoiRegion.class::isInstance);
	}

	public void endUpdate() {

		// remove components from graph pane
		getCellLayer().getChildren().removeAll(model.getRemovedCells());
		getCellLayer().getChildren().removeAll(model.getRemovedEdges());
		// add components to graph pane
		getCellLayer().getChildren().addAll(model.getAddedEdges());
		getCellLayer().getChildren().addAll(model.getAddedCells());

		// enable dragging of cells
		for (Cell cell : model.getAddedCells()) {
			mouseGestures.makeDraggable(cell);
		}

		// every cell must have a parent, if it doesn't, then the graphParent is
		// the parent
		getModel().attachOrphansToGraphParent(model.getAddedCells());

		// remove reference to graphParent
		getModel().disconnectFromGraphParent(model.getRemovedCells());

		// merge added & removed cells with all cells
		getModel().merge();
	}

	public Pane getCellLayer() {
		return cellLayer;
	}

	public GraphModel getModel() {
		return model;
	}

    public ScrollPane getScrollPane() {
		return scrollPane;
	}


	public void sortChildren() {
		ObservableList<Node> children = getCellLayer().getChildren();
		List<Node> cells = children.stream().filter(Cell.class::isInstance).collect(Collectors.toList());
		for (Node node : cells) {
			node.toFront();
		}
	}

    public List<Triangle> triangulate() {
        List<Triangle> triangulate = getModel().triangulate(getModel().getAllCells());
        endUpdate();
        sortChildren();
        return triangulate;
    }

	public void voronoi() {
		clean();
        List<Triangle> triangulate = triangulate();
        List<Ponto> allPoints = triangulate.stream().flatMap(Triangle::allPoints).distinct()
                .collect(Collectors.toList());
        for (Ponto ponto : allPoints) {
            List<Triangle> tr = triangulate.stream().filter(t -> t.allPoints().anyMatch(ponto::equals))
                .collect(Collectors.toList());
			VoronoiRegion voronoiRegion = new VoronoiRegion(ponto, tr);
			getCellLayer().getChildren().add(0, voronoiRegion);
		}
		sortChildren();
		getModel().coloring();

	}
}