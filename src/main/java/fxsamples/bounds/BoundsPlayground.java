package fxsamples.bounds;

import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Demo for understanding JavaFX Layout Bounds */
public class BoundsPlayground extends Application {
    private final ObservableList<Shape> shapes = FXCollections.observableArrayList();
    private final ObservableList<ShapePair> intersections = FXCollections.observableArrayList();

    private final ObjectProperty<BoundsType> selectedBoundsType = new SimpleObjectProperty<>(BoundsType.LAYOUT_BOUNDS);

    public ObjectProperty<BoundsType> getSelectedBoundsType() {
        return selectedBoundsType;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Bounds Playground");
        // define some objects to manipulate on the scene.
        Circle greenCircle = new Circle(100, 100, 50, Color.FORESTGREEN);
        greenCircle.setId("Green Circle");
        final Circle redCircle = new Circle(300, 200, 50, Color.FIREBRICK);
        redCircle.setId("Red Circle");
        final Line line = new Line(25, 300, 375, 200);
        line.setId("Line");
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStroke(Color.MIDNIGHTBLUE);
        line.setStrokeWidth(5);
        final Anchor anchor1 = new Anchor("Anchor 1", line.startXProperty(), line.startYProperty());
        final Anchor anchor2 = new Anchor("Anchor 2", line.endXProperty(), line.endYProperty());
        final Group group = new Group(greenCircle, redCircle, line, anchor1, anchor2);
        group.getChildrenUnmodifiable().stream().filter(Shape.class::isInstance).map(Shape.class::cast)
            .forEach(shapes::add);
        // monitor intersections of shapes in the scene.
        testIntersections();
        // enable dragging for the scene objects.
        Circle[] circles = { greenCircle, redCircle, anchor1, anchor2 };
        for (Circle circle : circles) {
            enableDrag(circle);
            circle.centerXProperty().addListener((ob, oldValue, newValue) -> testIntersections());
            circle.centerYProperty().addListener((ob, oldValue, newValue) -> testIntersections());
        }
        // define an overlay to show the layout bounds of the scene's shapes.
        Group layoutBoundsOverlay = new Group();
        layoutBoundsOverlay.setMouseTransparent(true);
        shapes.stream().filter(s -> !(s instanceof Anchor)).map(s -> new BoundsDisplay(this, s))
            .forEach(layoutBoundsOverlay.getChildren()::add);
        // layout the scene.
        final StackPane background = new StackPane();
        background.setStyle("-fx-background-color: cornsilk;");
        final Scene scene = new Scene(new Group(background, group, layoutBoundsOverlay), 600, 500);
        background.prefHeightProperty().bind(scene.heightProperty());
        background.prefWidthProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        stage.show();
        createUtilityWindow(stage, layoutBoundsOverlay, new Shape[] { greenCircle, redCircle });
    }

    private void changeBoundsType(final Group boundsOverlay, Boolean isSelected, BoundsType boundsInParent) {
        if (isSelected) {
            boundsOverlay.getChildren().stream().map(BoundsDisplay.class::cast)
                .forEach(b -> b.monitorBounds(boundsInParent));
            getSelectedBoundsType().set(boundsInParent);
            testIntersections();
        }
    }

    // define a utility stage for reporting intersections.
    private void createUtilityWindow(Stage stage, final Group boundsOverlay, final Shape[] transformableShapes) {
        final Stage reportingStage = new Stage();
        reportingStage.setTitle("Control Panel");
        reportingStage.initStyle(StageStyle.UTILITY);
        reportingStage.setX(stage.getX() + stage.getWidth());
        reportingStage.setY(stage.getY());
        // define content for the intersections utility panel.
        final ListView<ShapePair> intersectionView = new ListView<>(intersections);
        final Label instructions = new Label("Click on any circle in the scene to the left to drag it around.");
        double prefSize = Region.USE_PREF_SIZE;
        instructions.setMinSize(prefSize, prefSize);
        instructions.setStyle("-fx-font-weight: bold; -fx-text-fill: darkgreen;");
        final Label intersectionInstructions = new Label(
            "Any intersecting bounds in the scene will be reported below.");
        instructions.setMinSize(prefSize, prefSize);
        // add the ability to set a translate value for the circles.
        final CheckBox translateNodes = new CheckBox("Translate circles");
		translateNodes.selectedProperty().addListener((ob, oldValue, translate) -> Stream.of(transformableShapes)
						.peek(s -> s.setTranslateY(translate ? 100 : 0)).forEach(s -> testIntersections()));
        translateNodes.selectedProperty().set(false);
        // add the ability to add an effect to the circles.
        final Label modifyInstructions = new Label("Modify visual display aspects.");
        modifyInstructions.setStyle("-fx-font-weight: bold;");
        modifyInstructions.setMinSize(prefSize, prefSize);
        final CheckBox effectNodes = new CheckBox("Add an effect to circles");
        effectNodes.selectedProperty()
            .addListener((observableValue, oldValue, doTranslate) -> Stream.of(transformableShapes)
                .peek(s -> s.setEffect(doTranslate ? new DropShadow() : null)).forEach(s -> testIntersections()));
        effectNodes.selectedProperty().set(true);
        // add the ability to add a stroke to the circles.
        final CheckBox strokeNodes = new CheckBox("Add outside strokes to circles");
        strokeNodes.selectedProperty()
            .addListener((observableValue, oldValue, doTranslate) -> Stream.of(transformableShapes).peek(s -> {
                if (doTranslate) {
                    s.setStroke(Color.LIGHTSEAGREEN);
                }
            }).peek(s -> s.setStrokeWidth(doTranslate ? 10 : 0)).forEach(s -> testIntersections()));
        strokeNodes.selectedProperty().set(true);
        // add the ability to show or hide the layout bounds overlay.
        final Label showBoundsInstructions = new Label("The gray squares represent layout bounds.");
        showBoundsInstructions.setStyle("-fx-font-weight: bold;");
        showBoundsInstructions.setMinSize(prefSize, prefSize);
        final CheckBox showBounds = new CheckBox("Show Bounds");
        boundsOverlay.visibleProperty().bind(showBounds.selectedProperty());
        showBounds.selectedProperty().set(true);
        // create a container for the display control checkboxes.
        VBox displayChecks = new VBox(10);
        displayChecks.getChildren().addAll(modifyInstructions, translateNodes, effectNodes, strokeNodes,
            showBoundsInstructions, showBounds);
        // create a toggle group for the bounds type to use.
        ToggleGroup boundsToggleGroup = new ToggleGroup();
        final RadioButton useLayoutBounds = new RadioButton("Use Layout Bounds");
        final RadioButton useBoundsInLocal = new RadioButton("Use Bounds in Local");
        final RadioButton useBoundsInParent = new RadioButton("Use Bounds in Parent");
        useLayoutBounds.setToggleGroup(boundsToggleGroup);
        useBoundsInLocal.setToggleGroup(boundsToggleGroup);
        useBoundsInParent.setToggleGroup(boundsToggleGroup);
        VBox boundsToggles = new VBox(10);
        boundsToggles.getChildren().addAll(useLayoutBounds, useBoundsInLocal, useBoundsInParent);
        // change the layout bounds display depending on which bounds type has
        // been selected.
        useLayoutBounds.selectedProperty()
            .addListener((o, a, isSelected) -> changeBoundsType(boundsOverlay, isSelected, BoundsType.LAYOUT_BOUNDS));
        useBoundsInLocal.selectedProperty()
            .addListener((o, a, isSelected) -> changeBoundsType(boundsOverlay, isSelected, BoundsType.BOUNDS_IN_LOCAL));
        useBoundsInParent.selectedProperty().addListener(
            (o, a, isSelected) -> changeBoundsType(boundsOverlay, isSelected, BoundsType.BOUNDS_IN_PARENT));
        useLayoutBounds.selectedProperty().set(true);
        WebView boundsExplanation = new WebView();
        boundsExplanation.getEngine()
            .loadContent("<html><body bgcolor='darkseagreen' fgcolor='lightgrey' style='font-size:12px'><dl>"
                + "<dt><b>Layout Bounds</b></dt><dd>The boundary of the shape.</dd><br/>"
                + "<dt><b>Bounds in Local</b></dt><dd>The boundary of the shape and effect.</dd><br/>"
                + "<dt><b>Bounds in Parent</b></dt>"
                + "<dd>The boundary of the shape, effect and transforms.<br/>The co-ordinates of what you see.</dd>"
                + "</dl></body></html>");
        boundsExplanation.setPrefWidth(100);
        final int height = 130;
        boundsExplanation.setMinHeight(height);
        boundsExplanation.setMaxHeight(height);
        boundsExplanation.setStyle("-fx-background-color: transparent");
        // layout the utility pane.
        VBox utilityLayout = new VBox(10);
        utilityLayout.setStyle(
            "-fx-padding:10; -fx-background-color: linear-gradient(to bottom, lightblue, derive(lightblue, 20%));");
        utilityLayout.getChildren().addAll(instructions, intersectionInstructions, intersectionView, displayChecks,
            boundsToggles, boundsExplanation);
        final int prefHeight = 530;
        utilityLayout.setPrefHeight(prefHeight);
        reportingStage.setScene(new Scene(utilityLayout));
        reportingStage.show();
        // ensure the utility window closes when the main app window closes.
        stage.setOnCloseRequest(windowEvent -> reportingStage.close());
    }

    // update the list of intersections.
    private void testIntersections() {
        intersections.clear();

        // for each shape test it's intersection with all other shapes.
        for (Shape src : shapes) {
            for (Shape dest : shapes) {
                ShapePair pair = new ShapePair(src, dest);
                if (!(pair.a instanceof Anchor) && !(pair.b instanceof Anchor) && !intersections.contains(pair)
                    && pair.intersects(getSelectedBoundsType().get())) {
                    intersections.add(pair);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // make a node movable by dragging it around with the mouse.
    private static void enableDrag(final Circle circle) {
        final Delta dragDelta = new Delta();
        circle.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = circle.getCenterX() - mouseEvent.getX();
            dragDelta.y = circle.getCenterY() - mouseEvent.getY();
            circle.getScene().setCursor(Cursor.MOVE);
        });
        circle.setOnMouseReleased(mouseEvent -> circle.getScene().setCursor(Cursor.HAND));
        circle.setOnMouseDragged(mouseEvent -> {
            circle.setCenterX(mouseEvent.getX() + dragDelta.x);
            circle.setCenterY(mouseEvent.getY() + dragDelta.y);
        });
        circle.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                circle.getScene().setCursor(Cursor.HAND);
            }
        });
        circle.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                circle.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    // records relative x and y co-ordinates.
    private static class Delta {
        protected double x;
        protected double y;
    }
}