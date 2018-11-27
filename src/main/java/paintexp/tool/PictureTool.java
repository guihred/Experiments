package paintexp.tool;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import paintexp.PaintModel;
import simplebuilder.SimpleToggleGroupBuilder;

public class PictureTool extends PaintTool {

	private SVGPath icon;

	private SVGPath area;
	private int initialX;
	private int initialY;
    private PictureOption pic = PictureOption.TRIANGLE;
    private FillOption option = FillOption.STROKE;

	public SVGPath getArea() {
		if (area == null) {
			area = new SVGPath();
			area.setStrokeWidth(.1);
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
		}
		area.setContent(pic.getCorrectedPath(10));
		return area;
	}

	@Override
	public SVGPath getIcon() {
		if (icon == null) {
			icon = PictureOption.TRIANGLE.toSVG();
			icon.setContent(PictureOption.TRIANGLE.getCorrectedPath(10));
			icon.setFill(Color.TRANSPARENT);
			icon.setStroke(Color.BLACK);
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.DISAPPEAR;
	}

	@Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        icon = null;
		SVGPath icon2 = getIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        icon = null;
		SVGPath icon3 = getIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        icon = null;
		SVGPath icon4 = getIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        icon = null;
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
                .addToggle(icon2, FillOption.STROKE)
                .addToggle(icon3, FillOption.FILL)
                .addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option = newV == null ? FillOption.STROKE : (FillOption) newV.getUserData())
                .select(0)
                .getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
		SimpleToggleGroupBuilder picOptions = new SimpleToggleGroupBuilder();
		List<PictureOption> validOptions = Stream.of(PictureOption.values()).filter(e -> e.getPath() != null)
				.collect(Collectors.toList());
		validOptions.forEach(e -> picOptions.addToggle(e.toSVG(), e));

		VBox value = new VBox();
		value.getChildren().addAll(picOptions.onChange((o, old, newV) -> pic = (PictureOption) newV.getUserData())
                .select(0)
				.getTogglesAs(Node.class));

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(value);
		scrollPane.setPrefWidth(65);
		scrollPane.setMaxSize(200, 200);
		model.getToolOptions().getChildren().addAll(scrollPane);
    }

	@Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = setWithinRange(e.getX(), 0, model.getImage().getWidth());
		double y = setWithinRange(e.getY(), 0, model.getImage().getHeight());
		area.setScaleX(Math.abs(initialX - x) / pic.getWidth());
		area.setScaleY(Math.abs(initialY - y) / pic.getHeight());
		area.setLayoutX(Math.min(x, initialX));
		area.setLayoutY(Math.min(y, initialY));
	}

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
			area.setStroke(Color.BLACK);
			getArea().setManaged(false);
			getArea().setFill(Color.TRANSPARENT);
        }
        initialX = (int) e.getX();
        getArea().setLayoutX(initialX);
        initialY = (int) e.getY();
        getArea().setLayoutY(initialY);
        getArea().setStroke(Color.TRANSPARENT);
        getArea().setFill(Color.TRANSPARENT);
        if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
            getArea().setStroke(model.getFrontColor());
        }
        if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
            getArea().setFill(model.getBackColor());
        }
    }

    @Override
    protected  void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
		if (children.contains(getArea())) {
			takeSnapshotFill(model, area);
        }
        children.remove(getArea());
    }

}