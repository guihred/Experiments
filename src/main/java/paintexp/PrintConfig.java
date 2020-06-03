package paintexp;

import static java.lang.Math.nextDown;

import extract.PdfUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class PrintConfig extends Application {

    // private static final Logger LOG = HasLogging.log();
    @FXML
    private ComboBox<Integer> linesPerPage;
    @FXML
    private ComboBox<Integer> columnsPerPage;
    @FXML
    private GridPane panel;
    private final Image image;
    @FXML
    private ToggleGroup printType;
    @FXML
    private Slider hgap;

    @FXML
    private Slider vgap;

    public PrintConfig() {
        image = new Image(
                ResourceFXUtils.getFirstPathByExtension(new File("src").getAbsoluteFile(), ".png").toUri().toString());
    }

    public PrintConfig(Image image) {
        this.image = image;
    }

    public void changeConfig() {
        panel.getChildren().clear();
        Integer lines = linesPerPage.getSelectionModel().getSelectedItem();
        Integer columns = columnsPerPage.getSelectionModel().getSelectedItem();
        String text = getImpressionType();
        panel.setHgap(Math.min(hgap.getValue(), nextDown(1. / columns)) * panel.getPrefWidth());
        panel.setVgap(Math.min(vgap.getValue(), nextDown(1. / lines)) * panel.getPrefHeight());
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                ImageView imageView = createImage(lines, columns, text);
                panel.add(imageView, j, i);
            }
        }
    }

    public void initialize() {
        changeConfig();
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void print() {
        // ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        // String string = "Microsoft Print to PDF";
        // Printer curPrinter = allPrinters.stream().filter(e ->
        // string.equals(e.getName())).findFirst()
        // .orElse(Printer.getDefaultPrinter());
        // PrinterJob job = PrinterJob.createPrinterJob(curPrinter);
        // if (job == null) {
        // return;
        // }
        // File outFile = ResourceFXUtils.getOutFile("oi.pdf");
        // job.jobStatusProperty().addListener((ob, old, newV) -> {
        // LOG.info("Status {}", newV);
        // if (newV == JobStatus.DONE) {
        // RunnableEx.run(() -> {
        // while (!outFile.exists()) {
        // Thread.sleep(100);
        // }
        // ImageFXUtils.openInDesktop(outFile);
        // });
        // }
        // });
        // RunnableEx.run(() -> {
        // Files.deleteIfExists(outFile.toPath());
        // Object jobImpl = ClassReflectionUtils.getFieldValue(job, "jobImpl");
        // PrintRequestAttributeSet printReqAttrSet =
        // (PrintRequestAttributeSet) ClassReflectionUtils.getFieldValue(jobImpl,
        // "printReqAttrSet");
        // printReqAttrSet.add(new Destination(outFile.toURI()));
        // });
        // String description =
        // ClassReflectionUtils.getDescription(curPrinter.getPrinterAttributes());
        // LOG.info("PRINTER ATTRIBUTES {}", description);
        // PageLayout createPageLayout = curPrinter.createPageLayout(Paper.A4,
        // PageOrientation.PORTRAIT, 5, 5, 5, 5);
        // LOG.info("DEFAULT LAYOUT{}", curPrinter.getDefaultPageLayout());
        // job.printPage(createPageLayout, panel);
        // job.endJob();
        printToPDF();
        StageHelper.closeStage(panel);
    }

    public void printToPDF() {
        RunnableEx.run(() -> {
            BufferedImage bimg = ImageFXUtils.toBufferedImage(panel, panel.getWidth(), panel.getHeight(), 1);
            File outputFile = ResourceFXUtils.getOutFile("oi2.pdf");
            PdfUtils.createPDFFromImage(bimg, outputFile);
            ImageFXUtils.openInDesktop(outputFile);
        });
    }

    public void show() {
        RunnableEx.run(() -> start(new Stage()));
    }

    @Override
    public void start(Stage primaryStage) {
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

    private ImageView createImage(Integer lines, Integer columns, String printTypeName) {
        double fitWidth = panel.getPrefWidth() / columns - panel.getHgap() * (columns - 1);
        double fitHeight = panel.getPrefHeight() / lines - panel.getVgap() * (lines - 1);
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
