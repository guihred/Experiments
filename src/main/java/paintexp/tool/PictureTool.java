package paintexp.tool;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import utils.ConsumerEx;
import utils.HasLogging;

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
		area.setContent(correctPath(pic.path));
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
		scrollPane.setMaxSize(150, 200);
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
		TRIANGLE("M 12,4 L 20,20 4,20 12,4z"),
		SCALENE("M 0,0 l 0,20 20,0 Z "),
		DIAMOND("M 20,0 l 10,10 -10,10 -10,-10 Z "),
		PENTAGON("M 10,0 L 0,7 L 4,19 L 16,19 L 20,7 Z"),
		HEXAGON("M 20,9 L 15,17.310350 5,17.310350 0,9 5,0.061711 15,0.061711 20,9 z"),
		ARROW_RIGHT("m 20,0 l10,0 0,-7 10,10 -10,10 0,-7 -10,0 z"),
		ARROW_LEFT("m 0,20 l10,10 0,-7 10,0 0,-7 -10,0 0,-7 z"),
		ARROW_UP("m 20,0 l10,10 -7,0 0,10 -7,0 0,-10 -7,0 z"),
		ARROW_DOWN("m 0,20 l0,10 -7,0 10,10 10,-10 -7,0 0,-10 z"),
		STAR_4("M 10 4 L 11.9385 9.8393 L 18 12 L 11.9385 14.1607 L 10 20 L 7.6171 14.1607 L 2 12 L 7.6171 9.8393 Z"),
		STAR_5("m20,0 2,6h6l-5,4 2,6-5-4-5,4 2-6-5-4h6z "),
		STAR_6("M10,0 L12.875,5.020 18.660,5 15.750,10 18.660,15 12.875,14.980 10,20 7.125,14.980 1.340,15 4.250,10 1.340,5 7.125,5.020 z"),
		HEART("M15.53 1A5.52 5.52 0 0 0 11 4 5.52 5.52 0 0 0 0 6.58C0 12.37 11 20 11 20s11-7.63 11-13.42A5.53 5.53 0 0 0 15.53 1z");
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
			svgPath.setContent(correctPath(path));
			svgPath.maxWidth(20);
			width = svgPath.getBoundsInLocal().getWidth();
			height = svgPath.getBoundsInLocal().getWidth();
			svgPath.setFill(Color.TRANSPARENT);
			svgPath.setStroke(Color.BLACK);
			svgPath.setLayoutX(0);
			svgPath.setLayoutY(0);
			svgPath.setScaleX(20 / width);
			svgPath.setScaleY(20 / height);
			return svgPath;
		}

	}

	public static String correctPath(final String path) {
		Pattern compile = Pattern.compile("([\\d\\.]+)");
		Matcher a = compile.matcher(path);
		double max = 0;
		while (a.find()) {
			String group = a.group(1);
			max = Double.max(Double.parseDouble(group), max);
		}
		a.reset();
		StringBuffer sb = new StringBuffer();
		while (a.find()) {
			String group = a.group(1);
			double parseDouble = Double.parseDouble(group);
			int indexOf = group.indexOf(".");
			if (indexOf == -1) {
				indexOf = 0;
			} else {
				indexOf = group.length() - indexOf - 1;

			}
			a.appendReplacement(sb, String.format(Locale.ENGLISH, "%." + indexOf + "f", parseDouble * 20 / max));
		}
		a.appendTail(sb);
		return sb.toString();
	}

	public static void main(final String[] args) {
		Stream.of(PictureOption.values()).filter(e -> e.path != null).forEach(ConsumerEx.makeConsumer(e -> {
			HasLogging.log().info("{}", e);
			HasLogging.log().info("{}", e.path);
			HasLogging.log().info("{}", correctPath(e.path));
		}));
	}

}