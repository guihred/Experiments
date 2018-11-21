package paintexp.tool;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.event.EventType;
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
	boolean pressed;

	private SVGPath area;
	private int initialX;
	private int initialY;
	private PictureOption pic = PictureOption.HEART;
    private FillOption option = FillOption.STROKE;

	public SVGPath getArea() {
		if (area == null) {
			area = new SVGPath();
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);

		}
		area.setContent(pic.path);
		return area;
	}

	@Override
	public SVGPath getIcon() {
		if (icon == null) {
			icon = PictureOption.HEART.toSVG();
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
	public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			onMouseDragged(e, model);
		}
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
			onMouseReleased(model);
		}

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
                .onChange((o, old, newV) -> option = (FillOption) newV.getUserData())
                .getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
		SimpleToggleGroupBuilder picOptions = new SimpleToggleGroupBuilder();
		List<PictureOption> validOptions = Stream.of(PictureOption.values()).filter(e -> e.path != null)
				.collect(Collectors.toList());
		validOptions.forEach(e -> picOptions.addToggle(e.toSVG(), e));

		VBox value = new VBox();
		value.getChildren().addAll(picOptions.onChange((o, old, newV) -> pic = (PictureOption) newV.getUserData())
				.select(validOptions.size() - 1)
				.getTogglesAs(Node.class));

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(value);
		scrollPane.setMaxSize(50, 200);
		model.getToolOptions().getChildren().addAll(scrollPane);
    }

	private void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = setWithinRange(e.getX(), 0, model.getImage().getWidth());
		double y = setWithinRange(e.getY(), 0, model.getImage().getHeight());
		area.setScaleX(Math.abs(initialX - x) / pic.width);
		area.setScaleY(Math.abs(initialY - y) / pic.height);
		area.setLayoutX(Math.min(x, initialX));
		area.setLayoutY(Math.min(y, initialY));
	}

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
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

    private void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
		if (children.contains(getArea())) {
			takeSnapshotFill(model, area);
        }
        children.remove(getArea());
    }


	enum PictureOption {
		TRIANGLE,
		SCALENE,
		DIAMOND,
		PENTAGON,
		HEXAGON,
		ARROW_RIGHT,
		ARROW_LEFT,
		ARROW_UP,
		ARROW_DOWN,
		STAR_4,
		STAR_5("m25,1 6,17h18l-14,11 5,17-15-10-15,10 5-17-14-11h18z"),
		STAR_6,
		HEART("M14.75 1A5.24 5.24 0 0 0 10 4 5.24 5.24 0 0 0 0 6.25C0 11.75 10 19 10 19s10-7.25 10-12.75A5.25 5.25 0 0 0 14.75 1z");
		private String path;
		private double width;
		private double height;

		PictureOption() {

		}

		PictureOption(final String path) {
			this.path = path;
		}

		public SVGPath toSVG() {
			SVGPath svgPath = new SVGPath();
			svgPath.setContent(path);
			svgPath.maxWidth(20);
			width = svgPath.getBoundsInLocal().getWidth();
			height = svgPath.getBoundsInLocal().getWidth();
			svgPath.setLayoutX(0);
			svgPath.setLayoutY(0);
			svgPath.setScaleX(20 / width);
			svgPath.setScaleY(20 / height);
			return svgPath;
		}
	}



}