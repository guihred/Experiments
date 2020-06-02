package paintexp;

import static java.lang.Math.nextDown;

import java.io.File;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.CommonsFX;
import utils.ResourceFXUtils;

public class PrintConfig extends Application {

    @FXML
    private ComboBox<Integer> linesPerPage;
    @FXML
    private ComboBox<Integer> columnsPerPage;
    @FXML
    private GridPane panel;
    private Path firstPathByExtension =
            ResourceFXUtils.getFirstPathByExtension(new File("src").getAbsoluteFile(), ".png");
    @FXML
    private ToggleGroup printType;
    @FXML
    private Slider hgap;
    @FXML
    private Slider vgap;

    public void changeConfig() {
        panel.getChildren().clear();
        Integer lines = linesPerPage.getSelectionModel().getSelectedItem();
        Integer columns = columnsPerPage.getSelectionModel().getSelectedItem();
        String url = firstPathByExtension.toUri().toString();
        String text = getImpressionType();
        panel.setHgap(Math.min(hgap.getValue(), nextDown(1. / columns)) * panel.getPrefWidth());
        panel.setVgap(Math.min(vgap.getValue(), nextDown(1. / lines)) * panel.getPrefHeight());
        panel.setGridLinesVisible(true);
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                ImageView image = createImage(lines, columns, url, text);
                panel.add(image, j, i);
            }
        }
    }

    public void initialize() {
        changeConfig();
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CommonsFX.loadFXML("Print Config", "PrintConfig.fxml", this, primaryStage);
        primaryStage.getScene().getStylesheets().add(ResourceFXUtils.toExternalForm("starterApp.css"));
    }

    @SuppressWarnings("unused")
    public void toggleChanged(ObservableValue<? extends Toggle> ob, Toggle old, Toggle val) {
        if (panel != null) {
            changeConfig();
        }
    }

    @SuppressWarnings("unused")
    public void valueChanged(ObservableValue<? extends Double> ob, Double old, Double val) {
        if (panel != null) {
            changeConfig();
        }
    }

    private ImageView createImage(Integer lines, Integer columns, String imageURL, String printTypeName) {
        double fitWidth = panel.getPrefWidth() / columns - panel.getHgap() * (columns - 1);
        double fitHeight = panel.getPrefHeight() / lines - panel.getVgap() * (lines - 1);
        ImageView child = new ImageView(imageURL);
        if ("Whole Image".equals(printTypeName)) {
            child.setPreserveRatio(true);
            return setBestFit(child, fitWidth, fitHeight);
        }
        if ("Expand".equals(printTypeName)) {
            child.setFitWidth(fitWidth);
            child.setFitHeight(fitHeight);
            return child;
        }
        final double realWidth = child.getImage().getWidth();
        final double realHeight = child.getImage().getHeight();
        double d = fitWidth / realWidth;
        double e = fitHeight / realHeight;
        if (Math.abs(d - 1) > Math.abs(e - 1)) {
            d /= e;
            e = 1;
            child.setFitHeight(fitHeight);
        } else {
            e /= d;
            d = 1;
            child.setFitWidth(fitWidth);
        }
        final double width = realWidth * d;
        final double height = realHeight * e;
        double a = Math.max(0, (realWidth - width) / 2.);
        double b = Math.max(0, (realHeight - height) / 2.);
        child.setPreserveRatio(true);
        child.setViewport(new Rectangle2D(a, b, Math.min(realWidth, width), Math.min(realHeight, height)));
        return child;
    }

    private String getImpressionType() {
        return ((RadioButton) printType.getSelectedToggle()).getText();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static ImageView setBestFit(ImageView child, double width, double height) {
        if (width > height) {
            child.setFitHeight(height);
        } else {
            child.setFitWidth(width);
        }
        return child;
    }

}
