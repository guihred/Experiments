package paintexp.tool;

import fxsamples.DraggingRectangle;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import paintexp.PaintModel;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class TextTool extends PaintTool {

	private static final String TIMES_NEW_ROMAN = "Times New Roman";
    private List<String> families = Font.getFamilies();
    private Text icon;
    private Text text;
    private Rectangle area;
    private double initialX;
    private double initialY;
    private boolean pressed;
    private List<? extends Node> helpers;
    private TextArea textArea;

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder()
            		.fill(Color.TRANSPARENT)
            		.stroke(Color.BLACK)
            		.cursor(Cursor.MOVE)
            		.width(10)
                    .height(10)
                    .strokeDashArray(1, 2, 1, 2)
                    .build();
        }
        return area;
    }

    @Override
    public Text getIcon() {
        if (icon == null) {
            icon = new Text("A");
            icon.setFont(Font.font(TIMES_NEW_ROMAN, FontWeight.BOLD, 18));
        }
        return icon;
    }
    @Override
    public Cursor getMouseCursor() {
        return Cursor.DEFAULT;
    }

    public Text getText() {
        if (text == null) {
            text = new Text();
            text.setManaged(false);
        }
        return text;
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
        displayTextOptions(model);   
    }

    private void addRect(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
            helpers = DraggingRectangle.createDraggableRectangle(area);
            area.setStroke(Color.BLACK);
            getArea().setManaged(false);
            getArea().setFill(Color.TRANSPARENT);
            getArea().setLayoutX(initialX);
            getArea().setLayoutY(initialY);
        }
        if (!children.contains(getText())) {
            children.add(getText());
            getText().fillProperty().bind(model.frontColorProperty());
            getText().layoutXProperty().bind(area.layoutXProperty());
            getText().layoutYProperty()
                    .bind(Bindings.createDoubleBinding(() -> area.layoutYProperty().get() + text.getFont().getSize(),
                            area.layoutYProperty(), text.fontProperty()));
            getText().wrappingWidthProperty().bind(area.widthProperty());
        }
    }
    private SimpleToggleGroupBuilder createAlignments() {
        SimpleToggleGroupBuilder alignments = new SimpleToggleGroupBuilder();
        Group node = new Group();
        int maxWidth = 8;
        for (int i = 0; i < 4; i++) {
            Line line = new Line(0, i * 3, maxWidth - i % 3, i * 3);
            node.getChildren().add(line);
        }
        alignments.addToggle(node, TextAlignment.LEFT);
        Group node2 = new Group();
        for (int i = 0; i < 4; i++) {
            Line line = new Line(0 + i % 3, i * 3, maxWidth, i * 3);
            node2.getChildren().add(line);
        }
        alignments.addToggle(node2, TextAlignment.RIGHT);
        Group node3 = new Group();
        for (int i = 0; i < 4; i++) {
            Line line = new Line(0, i * 3, maxWidth, i * 3);
            node3.getChildren().add(line);
        }
        alignments.addToggle(node3, TextAlignment.JUSTIFY);
        Group node4 = new Group();
        for (int i = 0; i < 4; i++) {
            Line line = new Line(0 + i % 3, i * 3, maxWidth - i % 3, i * 3);
            node4.getChildren().add(line);
        }
        alignments.addToggle(node4, TextAlignment.CENTER);

        return alignments;
    }

    private void displayTextOptions(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        textArea = new TextArea();
        getText().textProperty().bind(textArea.textProperty());
        SimpleToggleGroupBuilder alignments = createAlignments();
        alignments.onChange((ob, old, newV) -> getText().setTextAlignment((TextAlignment) newV.getUserData()));
        Text graphic = new Text("B");
        graphic.setStyle("-fx-font-weight: bold;");
        ToggleButton bold = new ToggleButton(null, graphic);

        Text graphic2 = new Text("I");
        graphic2.setStyle("-fx-font-style: italic;");
        graphic2.setFont(Font.font(TIMES_NEW_ROMAN, FontWeight.NORMAL, FontPosture.ITALIC, 12));
        ToggleButton italic = new ToggleButton(null, graphic2);
        Text graphic3 = new Text("U");
        graphic3.setUnderline(true);
        ToggleButton undeline = new ToggleButton(null, graphic3);
        Text graphic4 = new Text("S");
        graphic4.setStrikethrough(true);
        ToggleButton strikeThrough = new ToggleButton(null, graphic4);

        SimpleComboBoxBuilder<Integer> fontSize = new SimpleComboBoxBuilder<Integer>()
                .items(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72);

		SimpleComboBoxBuilder<String> fontFamily = new SimpleComboBoxBuilder<String>().items(families)
				.styleFunction(t -> "-fx-font-family:\"" + t + "\";")
                .select(TIMES_NEW_ROMAN);

        fontFamily.onChange((old, newV) -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline,
                strikeThrough));

        fontSize.onChange((old, newV) -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline, strikeThrough))
                .select(5);
        bold.setOnAction(
                e -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline, strikeThrough));
        italic.setOnAction(
                e -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline, strikeThrough));
        undeline.setOnAction(
                e -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline, strikeThrough));
        strikeThrough.setOnAction(
                e -> onOptionsChanged(fontFamily, fontSize, bold, italic, undeline, strikeThrough));
        model.getToolOptions().getChildren().addAll(
                field("Font", fontFamily.build()), field("Size", fontSize.build()),
                new HBox(bold, italic, undeline, strikeThrough),
                new HBox(alignments.getTogglesAs(Node.class).toArray(new Node[0])),
                field("Text", textArea));
    }

    private void dragTo(final double x, final double y) {
        getArea().setLayoutX(Double.min(x, initialX));
        getArea().setLayoutY(Double.min(y, initialY));
        getArea().setWidth(Math.abs(x - initialX));
        getArea().setHeight(Math.abs(y - initialY));
    }

    private VBox field(final String text2, final Node fontFamily) {
        return new VBox(new Text(text2), fontFamily);
    }

    private void onMouseDragged(final MouseEvent e, final PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        if (pressed) {
            dragTo(setWithinRange(x, 0, width), setWithinRange(y, 0, height));
        }
    }

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            if (containsPoint(getArea(), e.getX(), e.getY())
                    || helpers.stream().anyMatch(n -> n.contains(e.getX(), e.getY()))) {
                return;
            }
            takeSnapshot(model);
            return;
        }
        initialX = e.getX();
        initialY = e.getY();
        pressed = true;
        addRect(model);

    }

    private void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() < 2 && children.contains(getArea())) {

            children.remove(getArea());
        }
        pressed = false;
        textArea.requestFocus();
        area.setStroke(Color.BLUE);
    }

    private void onOptionsChanged(final SimpleComboBoxBuilder<String> font, final SimpleComboBoxBuilder<Integer> fontSize,
            final ToggleButton bold, final ToggleButton italic, final ToggleButton undeline, final ToggleButton strikeThrough) {
        FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        double size = fontSize.selectedItem();
        text.setFont(Font.font(font.selectedItem(), weight, posture, size));
        getText().setUnderline(undeline.isSelected());
        getText().setStrikethrough(strikeThrough.isSelected());
    }

    private void takeSnapshot(final PaintModel model) {
        int width = (int) getArea().getWidth();
        int height = (int) getArea().getHeight();
        SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
        WritableImage textImage = text.snapshot(params, new WritableImage(width, height));
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
		copyImagePart(textImage, model.getImage(), 0, 0, width, height, x, y, Color.TRANSPARENT);
        model.getImageStack().getChildren().remove(getArea());
        model.getImageStack().getChildren().clear();
        model.getImageStack().getChildren().add(new ImageView(model.getImage()));
        textArea.setText("");
    }

}