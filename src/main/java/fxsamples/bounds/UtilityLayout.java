package fxsamples.bounds;

import static utils.CommonsFX.onCloseWindow;

import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.CommonsFX;

public class UtilityLayout {
    @FXML
    private CheckBox showBounds;
    @FXML
    private CheckBox strokeNodes;
    @FXML
    private CheckBox translateNodes;
    @FXML
    private CheckBox effectNodes;
    @FXML
    private ListView<ShapePair> intersectionView;
    @FXML
    private RadioButton useLayoutBounds;
    @FXML
    private RadioButton useBoundsInParent;
    @FXML
    private RadioButton useBoundsInLocal;
    @FXML
    private WebView boundsExplanation;
    private ObservableList<Shape> shapes;
    private ObservableList<ShapePair> intersections;
    private ObjectProperty<BoundsType> selectedBoundsType;
    private Group boundsOverlay;
    private Shape[] transformableShapes;

    public void initialize() {
        // define content for the intersections utility panel.
        intersectionView.setItems(intersections);
        // add the ability to set a translate value for the circles.
        translateNodes.selectedProperty().addListener((ob, oldValue, translate) -> Stream.of(transformableShapes)
            .peek(s -> s.setTranslateY(translate ? 100 : 0)).forEach(s -> testIntersections()));
        translateNodes.selectedProperty().set(false);
        // add the ability to add an effect to the circles.
        effectNodes.selectedProperty()
            .addListener((observableValue, oldValue, doTranslate) -> Stream.of(transformableShapes)
                .peek(s -> s.setEffect(doTranslate ? new DropShadow() : null)).forEach(s -> testIntersections()));
        effectNodes.selectedProperty().set(true);
        // add the ability to add a stroke to the circles.
        strokeNodes.selectedProperty()
            .addListener((observableValue, oldValue, doTranslate) -> Stream.of(transformableShapes).peek(s -> {
                if (doTranslate) {
                    s.setStroke(Color.LIGHTSEAGREEN);
                }
            }).peek(s -> s.setStrokeWidth(doTranslate ? 10 : 0)).forEach(s -> testIntersections()));
        strokeNodes.selectedProperty().set(true);
        // add the ability to show or hide the layout bounds overlay.
        boundsOverlay.visibleProperty().bind(showBounds.selectedProperty());
        showBounds.selectedProperty().set(true);
        // create a container for the display control checkboxes.
        // create a toggle group for the bounds type to use.
        // change the layout bounds display depending on which bounds type has
        // been selected.
        useLayoutBounds.selectedProperty()
            .addListener((o, a, isSelected) -> changeBoundsType(isSelected, BoundsType.LAYOUT_BOUNDS));
        useBoundsInLocal.selectedProperty()
            .addListener((o, a, isSelected) -> changeBoundsType(isSelected, BoundsType.BOUNDS_IN_LOCAL));
        useBoundsInParent.selectedProperty()
            .addListener((o, a, isSelected) -> changeBoundsType(isSelected, BoundsType.BOUNDS_IN_PARENT));
        useLayoutBounds.selectedProperty().set(true);
        boundsExplanation.getEngine()
            .loadContent("<html><body bgcolor='darkseagreen' fgcolor='lightgrey' style='font-size:12px'><dl>"
                + "<dt><b>Layout Bounds</b></dt><dd>The boundary of the shape.</dd><br/>"
                + "<dt><b>Bounds in Local</b></dt><dd>The boundary of the shape and effect.</dd><br/>"
                + "<dt><b>Bounds in Parent</b></dt>"
                + "<dd>The boundary of the shape, effect and transforms.<br/>The co-ordinates of what you see.</dd>"
                + "</dl></body></html>");
    }

    private void changeBoundsType(Boolean isSelected, BoundsType boundsInParent) {
        if (isSelected) {
            boundsOverlay.getChildren().stream().map(BoundsDisplay.class::cast)
                .forEach(b -> b.monitorBounds(boundsInParent));
            selectedBoundsType.set(boundsInParent);
            testIntersections();
        }
    }

    private void create(Stage stage, final Group boundsOverlay1, final Shape[] transformableShapes1,
        ObservableList<Shape> shapes1, ObservableList<ShapePair> intersections1,
        ObjectProperty<BoundsType> selectedBoundsType1) {
        boundsOverlay = boundsOverlay1;
        transformableShapes = transformableShapes1;
        shapes = shapes1;
        intersections = intersections1;
        selectedBoundsType = selectedBoundsType1;

        final Stage reportingStage = new Stage();
        reportingStage.setTitle("Control Panel");
        reportingStage.initStyle(StageStyle.UTILITY);
        reportingStage.setX(stage.getX() + stage.getWidth());
        reportingStage.setY(stage.getY());
        // ensure the utility window closes when the main app window closes.
        stage.showingProperty().addListener((ob, old, n) -> {
            if (!n) {
                Platform.runLater(reportingStage::close);
            }
        });
        onCloseWindow(stage, () -> Platform.runLater(reportingStage::close));
        CommonsFX.loadFXML("Control Panel", "UtilityLayout.fxml", this, reportingStage);
        reportingStage.show();
        testIntersections();
    }

    private void testIntersections() {
        testIntersections(shapes, intersections, selectedBoundsType);
    }

    public static void createUtilityWindow(Stage stage, final Group boundsOverlay1, final Shape[] transformableShapes1,
        ObservableList<Shape> shapes, ObservableList<ShapePair> intersections,
        ObjectProperty<BoundsType> selectedBoundsType) {
        new UtilityLayout().create(stage, boundsOverlay1, transformableShapes1, shapes, intersections,
            selectedBoundsType);
    }

    public static void testIntersections(ObservableList<Shape> shapes, ObservableList<ShapePair> intersections,
        ObjectProperty<BoundsType> selectedBoundsType) {
        intersections.clear();

        // for each shape test it's intersection with all other shapes.
        for (Shape src : shapes) {
            for (Shape dest : shapes) {
                ShapePair pair = new ShapePair(src, dest);
                if (!(pair.a instanceof Anchor) && !(pair.b instanceof Anchor) && !intersections.contains(pair)
                    && pair.intersects(selectedBoundsType.get())) {
                    intersections.add(pair);
                }
            }
        }
    }
}
