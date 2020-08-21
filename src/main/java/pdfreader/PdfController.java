package pdfreader;

import extract.PdfInfo;
import extract.PdfUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleTimelineBuilder;
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
    @FXML
    private WebView currentLines;
    @FXML
    private SplitPane splitPane;

    public void displayNextLine() {

        if (pdfInfo.getLineIndex() < pdfInfo.getLines().size()) {
            String value = pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
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
            updateAll();
            displayNextWord();
            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));
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
        splitPane.setDividerPosition(1, 1);
        StringBinding createStringBinding =
                Bindings.createStringBinding(this::getPageLines, pdfInfo.getLines(), pdfInfo.getSkipLines(),
                        pdfInfo.lineIndexProperty());
        InvalidationListener listener =
                (Observable o) -> RunnableEx
                        .runInPlatform(() -> {
                            currentLines.getEngine().loadContent(createStringBinding.get());
                            RunnableEx.ignore(() -> currentLines.getEngine()
                                    .executeScript("document.getElementsByTagName(\"b\")[0].scrollIntoView(true);"));

                        });
        pdfInfo.getLines().addListener(listener);
        pdfInfo.lineIndexProperty().addListener(listener);
        pdfInfo.getSkipLines().addListener(listener);
        PdfUtils.readFile(pdfInfo);
    }

    public void openNewPDF(ActionEvent event) {

        new FileChooserBuilder().title("Selecione Arquivo PDF").extensions("File", "*.pdf").onSelect(file -> {
            timeline.stop();
            RunnableEx.runNewThread(() -> PdfUtils.readFile(pdfInfo, file));
        }).openFileAction().handle(event);
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
        if (!pdfInfo.getSkipLines().contains(value)) {
            return value;
        } else if (pdfInfo.getLineIndex() >= pdfInfo.getLines().size() - 1) {
            return "";
        }
        return pdfInfo.getLines().get(pdfInfo.getLineIndexAndAdd());
    }

    private String getPageLines() {
        ObservableList<String> lines = pdfInfo.getLines();
        String string = lines.isEmpty() ? "" : lines.get((pdfInfo.getLineIndex() - 1 + lines.size()) % lines.size());

        return lines.stream().filter(StringUtils::isNotBlank)
                .filter(t -> !pdfInfo.getSkipLines().contains(t))
                .map(e -> "<div>" + (Objects.equals(e, string) ? "<b>" + e + "</b>" : e) + "</div>")
                .collect(Collectors.joining("\n"));
    }

    private void onImageChange() {
        ReadOnlyDoubleProperty widthProperty = imageBox.widthProperty();
        List<ImageView> createImages = currentImages.stream()
                .flatMap(im -> ImageTableCell.createImagesMaxWidth(im.getImage(), widthProperty).stream())
                .collect(Collectors.toList());
        imageBox.getChildren().setAll(createImages);
        imageBox.setVisible(!createImages.isEmpty());

        splitPane.setDividerPosition(1, createImages.isEmpty() ? 1 : 0.66);

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
        if (!pdfInfo.getWords().isEmpty() && pdfInfo.getIndex() < pdfInfo.getWords().size()) {

            String value = pdfInfo.getWords().get(pdfInfo.getIndexAndAdd());
            while (currentWord.getText().equals(value) && pdfInfo.getIndex() < pdfInfo.getWords().size()) {
                value = pdfInfo.getWords().get(pdfInfo.getIndexAndAdd());
            }
            currentWord.setText(value);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
