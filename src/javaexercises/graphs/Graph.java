package javaexercises.graphs;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
public class Graph {

	public static final BooleanProperty SHOW_WEIGHT = new SimpleBooleanProperty(true);
	private GraphModel model;

	private Group canvas;

	private ZoomableScrollPane scrollPane;

	MouseGestures mouseGestures;

	Pane cellLayer;

	public Graph() {

		model = new GraphModel();

		canvas = new Group();
		cellLayer = new CellLayer();

		canvas.getChildren().add(cellLayer);

		mouseGestures = new MouseGestures(this);

		scrollPane = new ZoomableScrollPane(canvas);

		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public Pane getCellLayer() {
		return cellLayer;
	}

	public GraphModel getModel() {
		return model;
	}

	public void clean() {
		getCellLayer().getChildren().removeIf(VoronoiRegion.class::isInstance);
	}

	public void voronoi() {
		clean();
		List<Cell> allCells = getModel().getAllCells();
		List<Triangle> triangulate = new DelaunayTopology(allCells.size(), this).triangulate();
		List<Ponto> collect = triangulate.stream().flatMap(Triangle::allPoints).distinct().collect(Collectors.toList());
		for (Ponto ponto : collect) {
			List<Triangle> tr = triangulate.stream().filter(t -> t.allPoints().anyMatch(ponto::equals)).collect(Collectors.toList());
			VoronoiRegion voronoiRegion = new VoronoiRegion(ponto, tr);
			getCellLayer().getChildren().add(0, voronoiRegion);
		}
		sortChildren();
		getModel().coloring();

	}

	public void sortChildren() {
		ObservableList<Node> children = getCellLayer().getChildren();
		List<Node> collect2 = children.stream().filter(Cell.class::isInstance).collect(Collectors.toList());
		for (Node node : collect2) {
			node.toFront();
		}
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


	public double getScale() {
		return scrollPane.getScaleValue();
	}
}