package extract;

import static utils.FunctionEx.mapIf;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    private int lineIndex;
    private final ObservableList<String> lines = FXCollections.observableArrayList();
    private final ObservableSet<String> skipLines = FXCollections.observableSet();
    private final ObservableList<String> words = FXCollections.observableArrayList();
    private final IntegerProperty pageIndex = new SimpleIntegerProperty(0);

    public PdfInfo() {
    }

    public PdfInfo(String pdfFile) {
        file = new File(pdfFile);
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
        return index++;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public int getLineIndexAndAdd() {
        return lineIndex++;
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

    public IntegerProperty numberOfPagesProperty() {
        return numberOfPages;
    }

    public IntegerProperty pageIndexProperty() {
        return pageIndex;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setImages(Map<Integer, List<PdfImage>> images) {
        this.images = images;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
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


    @Override
    public String toString() {
        return String.format(
            "PdfInfo {%n file=%s,%n numberOfPages=%s,%n progress=%s,%n index=%s,%n lineIndex=%s,%n pageIndex=%s%n}",
            mapIf(file, File::getName), numberOfPages.get(), progress.get(), index, lineIndex,
            pageIndex.get());
    }
}