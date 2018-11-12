package pdfreader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
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
    private static final String PDF_FILE = ResourceFXUtils
            .toFullPath("102 - Analista de Tecnologia da Informacao - Tipo D.pdf");
    private ObservableList<String> lines = FXCollections.observableArrayList();
    private ObservableList<String> skipLines = FXCollections.observableArrayList();
    private ObservableList<String> words = FXCollections.observableArrayList();
    private Timeline timeline;
    private final Text currentWord = new Text();
    private final Text currentLine = new Text();
    private final Text currentPage = new Text();
    private int index;
    private int lineIndex;
    private PdfInfo pdfInfo = new PdfInfo();

    private IntegerProperty pageIndex = new SimpleIntegerProperty(0);
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
        pdfInfo = PdfUtils.readFile(pdfInfo.getFile());
        primaryStage.setTitle("PDF Read Helper");
        final Button startButton = CommonsFX.newButton("_Start/Stop", e -> toggleTimelineStatus());
        final Button nextButton = CommonsFX.newButton("_Next Line", e -> displayNextLine());
        final Button pageButton = CommonsFX.newButton("_Next Page", e -> displayNextPage());
        final Button newPDF = CommonsFX.newButton("New _PDF", e -> PdfUtils.readFile(displayDialog(primaryStage)));
        timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.millis(WORD_DISPLAY_PERIOD), e -> displayNextWord())
                .cycleCount(Animation.INDEFINITE).build();
        currentWord.setFont(Font.font(60));
        currentPage.textProperty().bind(pageIndex.asString().concat("/" + pdfInfo.getNumberOfPages()));
        VBox root = new VBox(currentWord, currentLine, currentPage, startButton, nextButton, newPDF, pageButton);
        currentLine.wrappingWidthProperty().bind(root.widthProperty().subtract(30));
        currentLine.setTextAlignment(TextAlignment.CENTER);
        root.setAlignment(Pos.CENTER);
        TableView<HasImage> imagesTable = createImagesTable();
        VBox root2 = new VBox(root, imagesTable);
        root.prefWidthProperty().bind(root2.widthProperty().divide(2));
        imagesTable.prefWidthProperty().bind(root2.widthProperty().divide(2));
        root2.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root2, 500, 250, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private TableView<HasImage> createImagesTable() {
        return new SimpleTableViewBuilder<HasImage>()
                .scaleShape(false)
                .addColumn("Image", "image",s -> new ImageTableCell())
                .items(currentImages)
                .equalColumns()
                .build();
    }

    private File displayDialog(Stage primaryStage) {
        FileChooser fileChooser2 = new FileChooser();
        fileChooser2.setTitle("Selecione Arquivo PDF");
        fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser2.showOpenDialog(primaryStage);

    }

    private void displayNextLine() {
        if (lineIndex < lines.size()) {
            String value = lines.get(lineIndex++);
            skipLines.add(value);
            currentLine.setText(value);
            words.setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
            index = 0;
            if (lineIndex >= lines.size()) {
                timeline.stop();
            }
        }
    }

    private void displayNextPage() {
        if (pageIndex.get() < pdfInfo.getNumberOfPages() - 1) {
            pageIndex.set(pageIndex.get() + 1);
            lines.setAll(pdfInfo.getPages().get(pageIndex.get()));
        }
    }

    private void displayNextWord() {
        if (index >= words.size()) {
            if (lineIndex >= lines.size()) {
                lines.setAll(pdfInfo.getPages().get(pageIndex.get()));
                if (pdfInfo.getImages().containsKey(pageIndex.get())) {
                    List<PdfImage> col = pdfInfo.getImages().get(pageIndex.get());
                    currentImages.setAll(col);
                } else {
                    currentImages.clear();
                }
                pageIndex.set(pageIndex.get() + 1);
                lineIndex = 0;
                if (pageIndex.get() >= pdfInfo.getPages().size()) {
                    timeline.stop();
                }
            }
            String value = lines.get(lineIndex++);
            if (skipLines.contains(value) && lineIndex < lines.size() - 1) {
                value = lines.get(lineIndex++);
            }
            currentLine.setText(value);
            words.setAll(Arrays.asList(value.split(PdfUtils.SPLIT_WORDS_REGEX)));
            index = 0;
        }
        if (!words.isEmpty()) {
            currentWord.setText(words.get(index++));
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
