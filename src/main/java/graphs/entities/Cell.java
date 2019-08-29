package graphs.entities;

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
import javafx.scene.text.TextAlignment;
import org.junit.Ignore;

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
		text.setTextAlignment(TextAlignment.CENTER);
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

	public ObjectProperty<Color> colorProperty() {
        return color;
    }

    @Ignore
    public List<Cell> getCellChildren() {
		return children;
	}

    public String getCellId() {
		return cellId;
	}

    @Ignore
	public List<Cell> getCellParents() {
		return parents;
	}

	public Color getColor() {
		return color.get();
	}

	public String getText() {
		return text.getText();
	}

    @SuppressWarnings("static-method")
    public CellType getType() {
		return null;
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

	public void setColor(Color color) {
		this.color.set(color);
	}

	public void setSelected(boolean articulation) {
		selected.set(articulation);
	}

	public void setText(String s) {
	    addText(s);
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