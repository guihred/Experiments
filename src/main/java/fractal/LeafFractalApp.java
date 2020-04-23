package fractal;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class LeafFractalApp extends Application {
    private double[][][] matrix = new double[][][] { FernFractal.MATRIX_X, FernFractal.MATRIX_Y };

    private boolean skip;

    @Override
	public void start(Stage primaryStage) {
        FernFractal treeFractal = new FernFractal();
        SimpleToggleGroupBuilder coefs = toggleBuilder(treeFractal);
        VBox limitSlider = SimpleSliderBuilder.newSlider("Limit", 100, 100_000, treeFractal.limitProperty());
        VBox xScaleSlider = SimpleSliderBuilder.newSlider("xScale", 100, 500, treeFractal.scaleProperty());
        VBox coefSlider = SimpleSliderBuilder.newSlider("Coef", -1., 1., 200, treeFractal.coefProperty());
        primaryStage.setTitle("Tree Fractal");

        List<Node> togglesAs = coefs.getTogglesAs(Node.class);
        BorderPane root = new BorderPane();
        HBox value = new HBox(limitSlider, xScaleSlider, coefSlider);
        root.setTop(value);
        root.setLeft(new VBox(togglesAs.toArray(new Node[0])));
        StackPane pane = new StackPane(treeFractal);
        root.setCenter(pane);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        treeFractal.draw();
        treeFractal.coefProperty().addListener((ob, old, newV) -> onChangeCoefficient(coefs, old, newV));
    }

    private void onChangeCoefficient(SimpleToggleGroupBuilder toggleBuilder, Number old, Number newV) {
        Toggle selectedItem = toggleBuilder.selectedItem();
        if (selectedItem == null || !selectedItem.isSelected()) {
            return;
        }

        int[] userData = (int[]) selectedItem.getUserData();
        if (!skip) {
            matrix[userData[0]][userData[1]][userData[2]] -= old.doubleValue();
        }
        matrix[userData[0]][userData[1]][userData[2]] += newV.doubleValue();
        ToggleButton item = (ToggleButton) selectedItem;
        item.setText(toString(userData));
        skip = false;
    }

    private SimpleToggleGroupBuilder toggleBuilder(FernFractal treeFractal) {
        SimpleToggleGroupBuilder builder = new SimpleToggleGroupBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                for (int k = 0; k < matrix[i][j].length; k++) {
                    builder.addToggle(toString(matrix[i][j][k]), new int[] { i, j, k });
                }
            }
        }
        builder.onChange(ob -> {
            skip = true;
            treeFractal.coefProperty().set(0);
        });
        return builder;
    }

    private String toString(int[] userData) {
        return toString(matrix[userData[0]][userData[1]][userData[2]]);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String toString(double d) {
        return String.format("%.2f", d);
    }

}
