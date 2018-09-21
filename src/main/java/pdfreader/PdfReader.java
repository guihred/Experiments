package pdfreader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javafx.animation.Animation.Status;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.HasLogging;

public class PdfReader extends Application implements HasLogging {
    private ObservableList<String> lines = FXCollections.observableArrayList();
    private ObservableList<String> skipLines = FXCollections.observableArrayList();
    private ObservableList<String> words = FXCollections.observableArrayList();
    private Timeline timeline;
    private final Text currentWord = new Text();
    private final Text currentLine = new Text();
    private int index;
    private int lineIndex;
    private static final File PDF_FILE = new File(
            "C:\\Users\\guilherme.hmedeiros\\Documents\\BaseConhecimento\\CEH-V8\\Certified Ethical Hacker (CEH) v.8 Courseware Searchable PROPER\\CEHv8 Module 02 Footprinting and Reconnaissance.pdf");
    @Override
    public void start(Stage primaryStage) throws Exception {
        readFile(PDF_FILE);
        primaryStage.setTitle("PDF Read Helper");
        final Button startButton = CommonsFX.newButton("_Start/Stop", e -> {
            Status status = timeline.getStatus();
            if (status == Status.RUNNING) {
                timeline.stop();
            } else {
                timeline.play();
            }
        });
        final Button nextButton = CommonsFX.newButton("_Next", e -> {

            if (lineIndex < lines.size()) {

                String value = lines.get(lineIndex++);
                skipLines.add(value);
                currentLine.setText(value);
                words.setAll(Arrays.asList(value.split(" ")));
                index = 0;
                if (lineIndex >= lines.size()) {
                    timeline.stop();
                }
            }
        });
        timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.millis(200), e -> {
            if (index >= words.size()) {
                String value = lines.get(lineIndex++);
                if (skipLines.contains(value)) {
                    value = lines.get(lineIndex++);
                }
                currentLine.setText(value);
                words.setAll(Arrays.asList(value.split(" ")));
                index = 0;
                if (lineIndex >= lines.size()) {
                    timeline.stop();
                }
            }
            if (!words.isEmpty()) {
                currentWord.setText(words.get(index++));
            }
        }).cycleCount(Timeline.INDEFINITE).build();
        currentWord.setFont(Font.font(60));
        VBox root = new VBox(currentWord, currentLine, startButton, nextButton);
        currentLine.wrappingWidthProperty().bind(root.widthProperty().subtract(30));
        currentLine.setTextAlignment(TextAlignment.CENTER);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 500, 150, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    private void readFile(File file) {

        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int numberOfPages = pdDoc.getNumberOfPages();

            for (int i = 3; i < numberOfPages; i++) {
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                String parsedText = pdfStripper.getText(pdDoc);
                String[] pageLines = parsedText.split("\r\n");
                for (String string : pageLines) {
                    if (string.split(" ").length >= 4 * string.length() / 10) {
                        string = string.replaceAll("(?<=[^\\\\s]) (?=[^\\s])", "");
                    }

                    lines.add(string);
                }

            }
        } catch (Exception e) {
            getLogger().error("", e);
        }
    }

    private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

}
