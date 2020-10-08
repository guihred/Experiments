package extract;

import java.io.File;
import utils.HasImage;
import utils.ex.FunctionEx;

public class PdfImage implements HasImage {
    private float x;
    private float y;
    private File file;
    private int pageN;
    @Override
    public void appendImage(String image) {
        // NOTHING
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getImage() {
        return getFile().getName();
    }

    public int getPageN() {
        return pageN;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public boolean matches(String s0) {
        return false;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void setImage(String image) {
        setFile(new File(image));
    }

    public void setPageN(int pageN) {
        this.pageN = pageN;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return FunctionEx.mapIf(getFile(), File::getName, "");
    }
}