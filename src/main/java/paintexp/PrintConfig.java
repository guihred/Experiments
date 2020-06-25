package paintexp;

import static java.lang.Math.nextDown;

import extract.PdfUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class PrintConfig extends Application {

    @FXML
    private ComboBox<Integer> linesPerPage;
    @FXML
    private ComboBox<Integer> columnsPerPage;
    @FXML
    private GridPane panel;
    private ObservableList<Image> images = FXCollections.observableArrayList();
    @FXML
    private ToggleGroup printType;
    @FXML
    private CheckBox repeat;
    @FXML
    private CheckBox vertical;
    @FXML
    private Slider hgap;
    @FXML
    private Text page;
    private IntegerProperty currentPage = new SimpleIntegerProperty(0);

    @FXML
    private Slider vgap;

    public PrintConfig() {

        images.add(new Image(
                ResourceFXUtils.getFirstPathByExtension(new File("src").getAbsoluteFile(), ".png").toUri().toString()));
    }
    public PrintConfig(Image image) {
        images.add(image);
    }

    public void addPage() {
        addToCurrentPage(1);
        changeConfig();
    }

    public void changeConfig() {
        addToCurrentPage(0);
        adjustPanel(currentPage.get(), panel);
    }

    public void initialize() {
        changeConfig();
        page.textProperty().bind(currentPage.add(1).asString());
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void loadImages(ActionEvent event) {
        StageHelper.fileActionMultiple("Choose Image", f -> {
            if (!f.isEmpty()) {
                images.clear();
                images.addAll(f.stream().map(ResourceFXUtils::convertToURL).map(e -> new Image(e.toExternalForm()))
                        .collect(Collectors.toList()));
                changeConfig();
            }
        }, "Image", "*.png", "*.jpg", "*.jpeg").handle(event);
    }

    public void print() {
        printToPDF();
        StageHelper.closeStage(panel);
    }

    public void printToPDF() {
        RunnableEx.run(() -> {
            Integer lines = linesPerPage.getSelectionModel().getSelectedItem();
            Integer columns = columnsPerPage.getSelectionModel().getSelectedItem();
            List<BufferedImage> panelImages = new ArrayList<>();
            for (int i = 0; i < images.size(); i += lines * columns) {
                adjustPanel(i, panel);
                BufferedImage bimg =
                        ImageFXUtils.toBufferedImage(panel, panel.getWidth(), panel.getHeight(), 1);
                panelImages.add(bimg);
            }
            File outputFile = ResourceFXUtils.getOutFile("oi2.pdf");
            PdfUtils.createPDFFromImage(outputFile, panelImages.toArray(new BufferedImage[0]));
            ImageFXUtils.openInDesktop(outputFile);
        });
    }

    public void show() {
        RunnableEx.run(() -> start(new Stage()));
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Print Config", "PrintConfig.fxml", this, primaryStage);
        CommonsFX.addCSS(primaryStage.getScene(), "starterApp.css");
    }

    public void subPage() {
        addToCurrentPage(-1);
        changeConfig();
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

    private void addToCurrentPage(int add) {
        double lines = linesPerPage.getSelectionModel().getSelectedItem().doubleValue();
        double columns = columnsPerPage.getSelectionModel().getSelectedItem().doubleValue();
        int ceil = (int) Math.ceil(images.size() / lines / columns);
        currentPage.set((currentPage.get() + add + ceil) % ceil);

    }

    private void adjustPanel(int initial, GridPane panel2) {
        panel2.getChildren().clear();
        Integer lines = linesPerPage.getSelectionModel().getSelectedItem();
        Integer columns = columnsPerPage.getSelectionModel().getSelectedItem();
        String text = getImpressionType();
        double hgapp = Math.min(hgap.getValue(), nextDown(1. / (columns + 2))) * panel2.getPrefWidth();
        panel2.setHgap(hgapp);
        double vgapp = Math.min(vgap.getValue(), nextDown(1. / (lines + 2))) * panel2.getPrefHeight();
        panel2.setVgap(vgapp);
        panel2.setPadding(new Insets(vgapp, hgapp, vgapp, hgapp));
        int k = initial;
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                ImageView imageView = createImage(lines, columns, text, k++);
                panel2.add(imageView, j, i);
            }
        }
    }

    private ImageView createImage(Integer lines, Integer columns, String printTypeName, int k) {
        double fitWidth = panel.getPrefWidth() / columns - panel.getHgap() * (columns + 1);
        double fitHeight = panel.getPrefHeight() / lines - panel.getVgap() * (lines + 1);
        Image image =
                repeat.isSelected() || k < images.size() ? images.get(k % images.size()) : new WritableImage(1, 1);
        if (vertical.isSelected() == image.getWidth() > image.getHeight()) {
            image = ImageFXUtils.flip(image);
        }
        ImageView child = new ImageView(image);
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
        final double width = Math.max(1, realWidth * d);
        final double height = Math.max(1, realHeight * e);
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
