package javaexercises.graphs;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Cell extends Pane {

	private final String cellId;
	protected Text text;
	protected BooleanProperty selected = new SimpleBooleanProperty(false);
	protected ObjectProperty<Color> color = new SimpleObjectProperty<>();
	private List<Cell> children = new ArrayList<>();
	private List<Cell> parents = new ArrayList<>();

	private Node view;

	public Cell(String cellId) {
		this.cellId = cellId;
		text = new Text(cellId);
	}

	public CellType getType() {
		return null;
	}
	public void setColor(Color color) {
		this.color.set(color);
	}

	public Color getColor() {
		return color.get();
	}
	public void addCellChild(Cell cell) {
		children.add(cell);
	}

	public void addCellParent(Cell cell) {
		parents.add(cell);
	}

	public void addText(String s) {
		text.setText(cellId + "\n" + s);
	}

	public List<Cell> getCellChildren() {
		return children;
	}

	public String getCellId() {
		return cellId;
	}

	public List<Cell> getCellParents() {
		return parents;
	}

	public Node getView() {
		return view;
	}

	public boolean isSelected() {
		return selected.get();
	}

	public void removeCellChild(Cell cell) {
		children.remove(cell);
	}

	public void setSelected(boolean articulation) {
		selected.set(articulation);
	}

	public void setView(Node view) {
		this.view = view;
		getChildren().add(view);
	}

	@Override
	public String toString() {
		return cellId;
	}
}