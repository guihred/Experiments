package extract;

import java.io.File;
import utils.HasImage;

public class PdfImage implements HasImage {
    private File file;
    private float x;
    private float y;
    private int pageN;

    @Override
    public void appendImage(String image) {
        // DOES NOTHING
    }

    @Override
    public String getImage() {
        return getFile().getName();
    }

    @Override
    public boolean matches(String s0) {
        return false;
    }

    @Override
    public void setImage(String image) {
        setFile(new File(image));
    }

    @Override
    public String toString() {
        return getFile() != null ? getFile().getName() : "";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getPageN() {
        return pageN;
    }

    public void setPageN(int pageN) {
        this.pageN = pageN;
    }
}