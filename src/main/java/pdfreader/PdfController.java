package pdfreader;

import extract.PdfImage;
import extract.PdfInfo;
import extract.PdfUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import utils.HasImage;
import utils.ImageTableCell;
import utils.ResourceFXUtils;
import utils.StageHelper;

public class PdfController {
    private static final int WORD_DISPLAY_PERIOD = 200;
    private static final String PDF_FILE = ResourceFXUtils.toFullPath("sngpc2808.pdf");
    @FXML
    private TableView<HasImage> imageTable;
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

    private PdfInfo pdfInfo = new PdfInfo(PDF_FILE);

    private final ObservableList<HasImage> currentImages = FXCollections
        .synchronizedObservableList(FXCollections.observableArrayList());

    private Timeline timeline = new SimpleTimelineBuilder()
        .addKeyFrame(Duration.millis(WORD_DISPLAY_PERIOD), time -> displayNextWord())
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
        } 
    }

    public void displayNextPage() {
        if (pdfInfo.getPageIndex() < pdfInfo.getNumberOfPages() - 1) {
            pdfInfo.setPageIndex(pdfInfo.getPageIndex() + 1);
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
                if (pdfInfo.getImages().containsKey(pdfInfo.getPageIndex())) {
                    List<PdfImage> col = pdfInfo.getImages().get(pdfInfo.getPageIndex());
                    currentImages.setAll(col);
                    imageTable.scrollTo(imageTable.getItems().size() - 1);
                    imageTable.scrollTo(0);
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
    @SuppressWarnings("unchecked")
    public void initialize() {
        Property<Number> rate = timeline.rateProperty();
        slider.valueProperty().bindBidirectional(rate);
        progress.progressProperty().bind(pdfInfo.getProgress());
        currentPage.textProperty()
            .bind(pdfInfo.pageIndexProperty().asString().concat("/").concat(pdfInfo.numberOfPagesProperty()));
        imageTable.setItems(currentImages);
        TableColumn<HasImage, String> tableColumn = (TableColumn<HasImage, String>) imageTable.getColumns().get(0);
        tableColumn.setCellFactory(s -> new ImageTableCell<>());
        PdfUtils.readFile(pdfInfo, pdfInfo.getFile());
    }

    public void openNewPDF(ActionEvent event) {
        StageHelper.fileAction("Selecione Arquivo PDF",
            file -> PdfUtils.readFile(pdfInfo, file), "File", "*.pdf").handle(event);
    }

    public void toggleTimelineStatus() {
        Status status = timeline.getStatus();
        if (status == Status.RUNNING) {
            timeline.stop();
        } else {
            timeline.play();
        }
    }

}
