package pdfreader;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pdfreader.PrintImageLocations.PDFImage;

class PdfInfo{
    private File file;
    private int numberOfPages;
    private ObservableList<List<String>> pages = FXCollections.observableArrayList();
    private Map<Integer, List<PDFImage>> images;
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public int getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    public ObservableList<List<String>> getPages() {
        return pages;
    }
    public void setPages(ObservableList<List<String>> pages) {
        this.pages = pages;
    }
    public Map<Integer, List<PDFImage>> getImages() {
        return images;
    }
    public void setImages(Map<Integer, List<PDFImage>> images) {
        this.images = images;
    }
}