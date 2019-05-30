package pdfreader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTimelineBuilder;
import utils.*;

public class PdfReader extends Application implements HasLogging {
    private static final int WORD_DISPLAY_PERIOD = 200;
    private static final String PDF_FILE = ResourceFXUtils.toFullPath("sngpc2808.pdf");

    private final Timeline timeline = new SimpleTimelineBuilder()
        .addKeyFrame(Duration.millis(WORD_DISPLAY_PERIOD), e -> displayNextWord()).cycleCount(Animation.INDEFINITE)
        .build();
    private final Text currentWord = new Text();
    private final Text currentLine = new Text();
    private final Text currentPage = new Text();

    private PdfInfo pdfInfo = new PdfInfo();

    private ObservableList<HasImage> currentImages = FXCollections
        .synchronizedObservableList(FXCollections.observableArrayList());

    public PdfReader() {
        pdfInfo.setFile(new File(PDF_FILE));
    }

    public PdfReader(File file) {
        pdfInfo.setFile(file);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (pdfInfo.getFile() == null) {
            pdfInfo.setFile(displayDialog(primaryStage));
        }
        PdfUtils.readFile(pdfInfo, pdfInfo.getFile());
        primaryStage.setTitle("PDF Read Helper");
        final Button startButton = CommonsFX.newButton("_Start/Stop", e -> toggleTimelineStatus());
        final Button nextButton = CommonsFX.newButton("_Next Line", e -> displayNextLine());
        final Button pageButton = CommonsFX.newButton("_Next Page", e -> displayNextPage());
        final Button newPDF = CommonsFX.newButton("New _PDF",
            e -> PdfUtils.readFile(pdfInfo, displayDialog(primaryStage)));
        currentWord.setFont(Font.font(60));
        Property<Number> rate = timeline.rateProperty();
        VBox rateSlider = CommonsFX.newSlider("Rate", 0.01, 5, rate);

        currentPage.textProperty()
            .bind(pdfInfo.pageIndexProperty().asString().concat("/").concat(pdfInfo.numberOfPagesProperty()));
        VBox root = new VBox(currentWord, currentLine, currentPage, startButton, nextButton, pageButton, newPDF,
            rateSlider);
        currentLine.wrappingWidthProperty().bind(root.widthProperty().subtract(30));
        currentLine.setTextAlignment(TextAlignment.CENTER);
        root.setAlignment(Pos.CENTER);
        TableView<HasImage> imagesTable = createImagesTable();
        HBox root2 = new HBox(root, imagesTable);
        root.prefWidthProperty().bind(root2.widthProperty().divide(2));
        imagesTable.prefWidthProperty().bind(root2.widthProperty().divide(2));
        root2.setAlignment(Pos.CENTER);
        final int width = 500;
        Scene scene = new Scene(root2, width, width / 2, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private TableView<HasImage> createImagesTable() {
        return new SimpleTableViewBuilder<HasImage>().scaleShape(false)
            .addColumn("Image", "image", s -> new ImageTableCell()).items(currentImages).equalColumns().build();
    }

    private File displayDialog(Stage primaryStage) {
        FileChooser fileChooser2 = new FileChooser();
        fileChooser2.setTitle("Selecione Arquivo PDF");
        fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser2.showOpenDialog(primaryStage);

    }

    private void displayNextLine() {
        if (pdfInfo.getLineIndex() < pdfInfo.getLines().size()) {
            String value = pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
            pdfInfo.getSkipLines().add(value);
            currentLine.setText(value);
            pdfInfo.getWords().setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
            pdfInfo.setIndex(0);
            if (pdfInfo.getLineIndex() >= pdfInfo.getLines().size()) {
                timeline.stop();
            }
        }
    }

    private void displayNextPage() {
        if (pdfInfo.getPageIndex() < pdfInfo.getNumberOfPages() - 1) {
            pdfInfo.setPageIndex(pdfInfo.getPageIndex() + 1);
            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));
        }
    }

    private void displayNextWord() {
        if (pdfInfo.getIndex() >= pdfInfo.getWords().size()) {
            if (pdfInfo.getLineIndex() >= pdfInfo.getLines().size()) {

                List<String> linesNextPage = pdfInfo.getPages().get(pdfInfo.getPageIndex());
                if (!pdfInfo.getLines().isEmpty()) {
                    List<String> repeated = linesNextPage.stream().filter(l -> pdfInfo.getLines().contains(l))
                        .collect(Collectors.toList());
                    pdfInfo.getSkipLines().addAll(repeated);

                }
                pdfInfo.getLines().setAll(linesNextPage);
                if (pdfInfo.getImages().containsKey(pdfInfo.getPageIndex())) {
                    List<PdfImage> col = pdfInfo.getImages().get(pdfInfo.getPageIndex());
                    currentImages.setAll(col);
                } else {
                    currentImages.clear();
                }
                pdfInfo.setPageIndex(pdfInfo.getPageIndex() + 1);
                pdfInfo.setLineIndex(0);
                if (pdfInfo.getPageIndex() >= pdfInfo.getPages().size()) {
                    timeline.stop();
                }
            }
            String value = pdfInfo.getLines().isEmpty() ? ""
                : pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd() % pdfInfo.getLines().size());
            if (pdfInfo.getSkipLines().contains(value) && pdfInfo.getLineIndex() < pdfInfo.getLines().size() - 1) {
                value = pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
            }
            currentLine.setText(value);
            pdfInfo.getWords().setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
            pdfInfo.setIndex(0);
        }
        if (!pdfInfo.getWords().isEmpty()) {
            currentWord.setText(pdfInfo.getWords().get(pdfInfo.getIndexAndAdd()));
        }
    }

    private void toggleTimelineStatus() {
        Status status = timeline.getStatus();
        if (status == Status.RUNNING) {
            timeline.stop();
        } else {
            timeline.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
