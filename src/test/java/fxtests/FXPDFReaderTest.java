package fxtests;

import extract.PdfUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import pdfreader.PdfController;
import utils.ResourceFXUtils;

public class FXPDFReaderTest extends AbstractTestExecution {

    @Test
    public void testPdfUtils() {
        File parentFile = new File(".").getAbsoluteFile().getParentFile().getParentFile();
        List<Path> pathByExtension = ResourceFXUtils.getPathByExtension(parentFile, ".pdf");
        File file2 = randomItem(pathByExtension).toFile();
        measureTime("PdfUtils.readFile", () -> PdfUtils.readText(file2));
    }

    @Test
    public void testPdfUtils2() {
        File file2 = new File("C:\\Users\\guigu\\Documents\\Estudo\\jsoup.pdf");
        measureTime("PdfUtils.readFile", () -> PdfUtils.readText(file2));
    }

    @Test
    public void verifyButtons() {
        show(PdfController.class);
        lookup(".button").queryAll().forEach(t -> {
            sleep(1000);
            tryClickOn(t);
            type(KeyCode.ESCAPE);
        });
    }
}
