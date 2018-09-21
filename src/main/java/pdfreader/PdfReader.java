package pdfreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	private static final String SPLIT_WORDS_REGEX = "[\\s]+";
	private ObservableList<String> lines = FXCollections.observableArrayList();
    private ObservableList<String> skipLines = FXCollections.observableArrayList();
    private ObservableList<String> words = FXCollections.observableArrayList();
	private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Timeline timeline;
    private final Text currentWord = new Text();
    private final Text currentLine = new Text();
	private final Text currentPage = new Text();
    private int index;
    private int lineIndex;
	private IntegerProperty pageIndex = new SimpleIntegerProperty(0);
    private static final File PDF_FILE = new File(
			"C:\\Users\\guigu\\Documents\\Estudo\\processoLegislativo.pdf");
	private int numberOfPages;
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
				words.setAll(Arrays.asList(value.split(SPLIT_WORDS_REGEX)));
                index = 0;
                if (lineIndex >= lines.size()) {
					timeline.stop();
                }
            }
        });
        timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.millis(200), e -> {
            if (index >= words.size()) {
				if (lineIndex >= lines.size()) {
					lines.setAll(pages.get(pageIndex.get()));
					pageIndex.set(pageIndex.get() + 1);
					lineIndex = 0;
					if (pageIndex.get() >= pages.size()) {
						timeline.stop();
					}
				}
                String value = lines.get(lineIndex++);
                if (skipLines.contains(value)) {
                    value = lines.get(lineIndex++);
                }
                currentLine.setText(value);
				words.setAll(Arrays.asList(value.split(SPLIT_WORDS_REGEX)));
                index = 0;
            }
            if (!words.isEmpty()) {
                currentWord.setText(words.get(index++));
            }
		}).cycleCount(Animation.INDEFINITE).build();
        currentWord.setFont(Font.font(60));
		currentPage.textProperty().bind(pageIndex.asString().concat("/" + numberOfPages));
		VBox root = new VBox(currentWord, currentLine, currentPage, startButton, nextButton);
        currentLine.wrappingWidthProperty().bind(root.widthProperty().subtract(30));
        currentLine.setTextAlignment(TextAlignment.CENTER);
        root.setAlignment(Pos.CENTER);
		Scene scene = new Scene(root, 500, 250, Color.WHITE);
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
            numberOfPages = pdDoc.getNumberOfPages();

			int start = 0;
			for (int i = start; i < numberOfPages; i++) {
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                String parsedText = pdfStripper.getText(pdDoc);
                String[] pageLines = parsedText.split("\r\n");
				List<String> lines1 = new ArrayList<>();
                for (String string : pageLines) {
					if (string.split(SPLIT_WORDS_REGEX).length >= 4 * string.length() / 10) {
						string = string.replaceAll("(?<=[^\\s]) (?=[^\\s])", "");
                    }

					lines1.add(string.replaceAll("\t", " "));
                }
				pages.add(lines1);

			}
			numberOfPages -= start;
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
