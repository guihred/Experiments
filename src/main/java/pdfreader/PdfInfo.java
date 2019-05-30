package pdfreader;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

class PdfInfo {
    private File file;
    private final IntegerProperty numberOfPages = new SimpleIntegerProperty(0);
    private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Map<Integer, List<PdfImage>> images;
    private int index;
    private int lineIndex;
    private final ObservableList<String> lines = FXCollections.observableArrayList();
    private final ObservableSet<String> skipLines = FXCollections.observableSet();
    private final ObservableList<String> words = FXCollections.observableArrayList();

    private final IntegerProperty pageIndex = new SimpleIntegerProperty(0);
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

    public IntegerProperty getPageIndex() {
        return pageIndex;
    }

    public ObservableList<List<String>> getPages() {
        return pages;
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

    public void setLines(ObservableList<String> lines) {
        this.lines.clear();
        this.lines.setAll(lines);
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages.set(numberOfPages);
    }

    public void setPageIndex(int v) {
        pageIndex.set(v);
    }

    public void setPages(ObservableList<List<String>> pages) {
        this.pages = pages;
    }


    public void setWords(ObservableList<String> words) {
        this.words.setAll(words);
    }
}