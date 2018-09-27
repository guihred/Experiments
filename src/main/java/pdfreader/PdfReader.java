package pdfreader;

import contest.db.HasImage;
import contest.db.PrintImageLocations;
import contest.db.PrintImageLocations.PDFImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ImageTableCell;

public class PdfReader extends Application implements HasLogging {
    private static final int WORD_DISPLAY_PERIOD = 200;
    private static final String SPLIT_WORDS_REGEX = "[\\s]+";
    private static final String PDF_FILE = "C:\\Users\\guilherme.hmedeiros\\Documents\\BaseConhecimento\\CEH-V8\\Certified Ethical Hacker (CEH) v.8 Courseware Searchable PROPER\\CEH v8 Labs Module 02 Footprinting and Reconnaissance.pdf";
    private ObservableList<String> lines = FXCollections.observableArrayList();
    private ObservableList<String> skipLines = FXCollections.observableArrayList();
	private ObservableList<String> words = FXCollections.observableArrayList();
    private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Map<Integer, List<PDFImage>> images = new ConcurrentHashMap<>();
    private Timeline timeline;
    private final Text currentWord = new Text();
	private final Text currentLine = new Text();
    private final Text currentPage = new Text();
    private int index;
	private int lineIndex;
    private IntegerProperty pageIndex = new SimpleIntegerProperty(0);
    private ObservableList<HasImage> currentImages = FXCollections
            .synchronizedObservableList(FXCollections.observableArrayList());
	private int numberOfPages;
    @Override
    public void start(Stage primaryStage) throws Exception {
        readFile(new File(PDF_FILE));
        primaryStage.setTitle("PDF Read Helper");
        final Button startButton = CommonsFX.newButton("_Start/Stop", e -> toggleTimelineStatus());
        final Button nextButton = CommonsFX.newButton("_Next", e -> displayNextLine());
        timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.millis(WORD_DISPLAY_PERIOD), e -> displayNextWord())
                .cycleCount(Animation.INDEFINITE).build();
        currentWord.setFont(Font.font(60));
		currentPage.textProperty().bind(pageIndex.asString().concat("/" + numberOfPages));
		VBox root = new VBox(currentWord, currentLine, currentPage, startButton, nextButton);
        currentLine.wrappingWidthProperty().bind(root.widthProperty().subtract(30));
        currentLine.setTextAlignment(TextAlignment.CENTER);
        root.setAlignment(Pos.CENTER);
        TableView<HasImage> imagesTable = createImagesTable();
        HBox root2 = new HBox(root, imagesTable);
        root.prefWidthProperty().bind(root2.widthProperty().divide(2));
        imagesTable.prefWidthProperty().bind(root2.widthProperty().divide(2));

        root2.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root2, 500, 250, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private TableView<HasImage> createImagesTable() {

        final TableView<HasImage> table = new TableView<>();
        table.setPrefWidth(300);
        table.setScaleShape(false);
        table.setItems(currentImages);
        TableColumn<HasImage, String> imageQuestion = new TableColumn<>("Image");
        imageQuestion.setSortable(true);
        imageQuestion.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageQuestion.setCellFactory(s -> new ImageTableCell());
        imageQuestion.prefWidthProperty().bind(table.prefWidthProperty());
        table.getColumns().add(imageQuestion);
        return table;
    }

    private void displayNextLine() {
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
    }

    private void displayNextWord() {
        if (index >= words.size()) {
        	if (lineIndex >= lines.size()) {
        		lines.setAll(pages.get(pageIndex.get()));
                if (images.containsKey(pageIndex.get())) {
                    List<PDFImage> col = images.get(pageIndex.get());
                    currentImages.setAll(col);
                } else {
                    currentImages.clear();
                }
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
            new Thread(() -> {
                extracted(file, start);
            }).start();
        } catch (Exception e) {
            getLogger().error("", e);
        }
    }

    private void extracted(File file, int start) {
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            for (int i = start; i < numberOfPages; i++) {
                PrintImageLocations printImageLocations = new PrintImageLocations();
                PDPage page = pdDoc.getPage(i);
                getPageImages(printImageLocations, i, page);
            }
        } catch (Exception e) {
            getLogger().info("", e);
        }
    }

    private void getPageImages(PrintImageLocations printImageLocations, int i, PDPage page) {
        try {
            List<PDFImage> images1 = printImageLocations.processPage(page, i);
            getLogger().info("images extracted {}", images1);
            images.put(i, images1);
        } catch (Exception e) {
            getLogger().info("", e);
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

    private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

}
