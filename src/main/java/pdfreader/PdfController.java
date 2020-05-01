package pdfreader;

import extract.PdfInfo;
import extract.PdfUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import simplebuilder.StageHelper;
import utils.*;

public class PdfController extends Application {

    private static final int WORD_DISPLAY_PERIOD = 250;
    private static final String PDF_FILE = ResourceFXUtils.toFullPath("sngpc2808.pdf");

    @FXML
    private ProgressIndicator progress;
    @FXML
    private Text currentWord;
    @FXML
    private Text currentLine;
    @FXML
    private Slider slider;
    @FXML
    private Text currentPage;
    @FXML
    private Pane imageBox;
    private PdfInfo pdfInfo = new PdfInfo(PDF_FILE);
    private final ObservableList<HasImage> currentImages =
            FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    private Timeline timeline =
            new SimpleTimelineBuilder().addKeyFrame(Duration.millis(WORD_DISPLAY_PERIOD), time -> displayNextWord())
                    .cycleCount(Animation.INDEFINITE).build();

    public void displayNextLine() {
        if (pdfInfo.getLineIndex() < pdfInfo.getLines().size()) {
            String value = pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
            pdfInfo.getSkipLines().add(value);
            currentLine.setText(value);
            pdfInfo.getWords().setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
            pdfInfo.setIndex(0);
            if (pdfInfo.getLineIndex() >= pdfInfo.getLines().size()) {
                timeline.stop();
            }
            updateAll();
        }
    }

    public void displayNextPage() {
        if (pdfInfo.getPageIndex() < pdfInfo.getNumberOfPages() - 1) {
            pdfInfo.setPageIndex(pdfInfo.getPageIndex() + 1);
            pdfInfo.setIndex(0);
            pdfInfo.setLineIndex(0);
            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));
            displayNextWord();
            updateAll();
        }
    }

    public void displayNextWord() {
        if (pdfInfo.getIndex() >= pdfInfo.getWords().size()) {
            if (pdfInfo.getLineIndex() >= pdfInfo.getLines().size()) {

                List<String> linesNextPage = pdfInfo.getPages().get(pdfInfo.getPageIndex());
                if (!pdfInfo.getLines().isEmpty()) {
                    List<String> repeated = linesNextPage.stream().filter(l -> pdfInfo.getLines().contains(l))
                            .collect(Collectors.toList());
                    pdfInfo.getSkipLines().addAll(repeated);

                }
                pdfInfo.getLines().setAll(linesNextPage);
                updateImages();
                pdfInfo.setPageIndex(pdfInfo.getPageIndex() + 1);
                pdfInfo.setLineIndex(0);
                if (pdfInfo.getPageIndex() >= pdfInfo.getPages().size()) {
                    timeline.stop();
                }
            }
            updateCurrentLine();
            pdfInfo.setIndex(0);
        }
        updateWords();
    }

    public void displayPreviousPage() {
        if (pdfInfo.getPageIndex() > 0) {
            pdfInfo.setPageIndex(pdfInfo.getPageIndex() - 1);
            pdfInfo.setIndex(0);
            pdfInfo.setLineIndex(0);
            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));
            updateAll();
        }
    }

    public void initialize() {
        slider.valueProperty().bindBidirectional(timeline.rateProperty());
        progress.progressProperty().bind(pdfInfo.getProgress());
        currentPage.textProperty()
                .bind(pdfInfo.pageIndexProperty().asString().concat("/").concat(pdfInfo.numberOfPagesProperty()));
        currentImages.addListener((Change<? extends HasImage> e) -> onImageChange());
        imageBox.managedProperty().bind(imageBox.visibleProperty());
        imageBox.setVisible(false);
        PdfUtils.readFile(pdfInfo);
    }

    public void openNewPDF(ActionEvent event) {
        StageHelper
                .fileAction("Selecione Arquivo PDF",
                        file -> RunnableEx.runNewThread(() -> PdfUtils.readFile(pdfInfo, file)), "File", "*.pdf")
                .handle(event);
        updateAll();
    }

    @Override
    public void start(Stage primaryStage) {
        final int width = 500;
        CommonsFX.loadFXML("PDF Read Helper", "PdfReader.fxml", primaryStage, width, width);
    }

    public void toggleTimelineStatus() {
        Status status = timeline.getStatus();
        if (status == Status.RUNNING) {
            timeline.stop();
        } else {
            timeline.play();
        }
    }

    private String getCurrentLine() {
        String value = pdfInfo.getLines().isEmpty() ? ""
                : pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd() % pdfInfo.getLines().size());
        if (!pdfInfo.getSkipLines().contains(value) || pdfInfo.getLineIndex() >= pdfInfo.getLines().size() - 1) {
            return value;
        }
        return pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
    }

    private void onImageChange() {
        ReadOnlyDoubleProperty widthProperty = imageBox.widthProperty();
        List<ImageView> createImages = currentImages.stream()
                .flatMap(im -> ImageTableCell.createImagesMaxWidth(im.getImage(), widthProperty).stream())
            .collect(Collectors.toList());
        imageBox.getChildren().setAll(createImages);
        imageBox.setVisible(!createImages.isEmpty());
    }

    private void updateAll() {
        updateImages();
        updateCurrentLine();
        updateWords();
    }

    private void updateCurrentLine() {
        String value = getCurrentLine();
        currentLine.setText(value);
        pdfInfo.getWords().setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
    }

    private void updateImages() {
        currentImages.clear();
        if (pdfInfo.getImages().containsKey(pdfInfo.getPageIndex() - 1)) {
            currentImages.setAll(pdfInfo.getImages().get(pdfInfo.getPageIndex() - 1));
        }
    }

    private void updateWords() {
        if (!pdfInfo.getWords().isEmpty()) {
            currentWord.setText(pdfInfo.getWords().get(pdfInfo.getIndexAndAdd()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
