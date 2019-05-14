package pdfreader;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class PdfInfo {
    private File file;
    private int numberOfPages;
    private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Map<Integer, List<PdfImage>> images;

    public File getFile() {
        return file;
    }

    public Map<Integer, List<PdfImage>> getImages() {
        return images;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public ObservableList<List<String>> getPages() {
        return pages;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setImages(Map<Integer, List<PdfImage>> images) {
        this.images = images;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public void setPages(ObservableList<List<String>> pages) {
        this.pages = pages;
    }
}