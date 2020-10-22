package extract;

import static utils.ex.FunctionEx.mapIf;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class PdfInfo {
    private File file;
    private final IntegerProperty numberOfPages = new SimpleIntegerProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Map<Integer, List<PdfImage>> images;
    private int index;
    private IntegerProperty lineIndex = new SimpleIntegerProperty(0);
    private final ObservableList<String> lines = FXCollections.observableArrayList();
    private final ObservableSet<String> skipLines = FXCollections.observableSet();
    private final ObservableList<String> words = FXCollections.observableArrayList();
    private final IntegerProperty pageIndex = new SimpleIntegerProperty(0);
    private final StringProperty titleName = new SimpleStringProperty("");

    public PdfInfo() {
    }

    public PdfInfo(File pdfFile) {
        setFile(pdfFile);
    }

    public PdfInfo(String pdfFile) {
        setFile(new File(pdfFile));
    }

    public File getFile() {
        return file;
    }

    public Map<Integer, List<PdfImage>> getImages() {
        return images;
    }

    public int getIndex() {
        return index;
    }

    public int getIndexAndAdd() {
        index = index + 1;

        return index - 1;
    }

    public int getLineIndex() {
        return lineIndex.get();
    }

    public int getLineIndexAndAdd() {
        int i = lineIndex.get();
        lineIndex.set(i + 1);
        return i;
    }

    public ObservableList<String> getLines() {
        return lines;
    }

    public int getNumberOfPages() {
        return numberOfPages.get();
    }

    public int getPageIndex() {
        return pageIndex.get();
    }

    public ObservableList<List<String>> getPages() {
        return pages;
    }

    public DoubleProperty getProgress() {
        return progress;
    }

    public ObservableSet<String> getSkipLines() {
        return skipLines;
    }

    public ObservableList<String> getWords() {
        return words;
    }

    public IntegerProperty lineIndexProperty() {
        return lineIndex;
    }

    public IntegerProperty numberOfPagesProperty() {
        return numberOfPages;
    }

    public IntegerProperty pageIndexProperty() {
        return pageIndex;
    }

    public final void setFile(File file) {
        this.file = file;
        titleName.set(file.getName());
    }

    public void setImages(Map<Integer, List<PdfImage>> images) {
        this.images = images;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex.set(lineIndex);
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages.set(numberOfPages);
    }

    public void setPageIndex(int v) {
        pageIndex.set(v);
    }

    public void setProgress(double value) {
        progress.set(value);
    }

    public StringProperty titleNameProperty() {
        return titleName;
    }

    @Override
    public String toString() {
        return String.format(
                "PdfInfo {%n file=%s,%n numberOfPages=%s,%n progress=%s,%n index=%s,%n lineIndex=%s,%n pageIndex=%s%n}",
                mapIf(file, File::getName), numberOfPages.get(), progress.get(), index, lineIndex, pageIndex.get());
    }
}