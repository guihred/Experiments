package fxtests;

import static fxtests.FXTesting.measureTime;

import extract.PdfUtils;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import pdfreader.PdfController;
import utils.ResourceFXUtils;

public class FXPDFReaderTest extends AbstractTestExecution {

    @Test
    public void testPdfUtils() {
        List<Path> pathByExtension = ResourceFXUtils.getPathByExtension(new File(""), ".pdf");
        File file2 = randomItem(pathByExtension).toFile();
        measureTime("PdfUtils.readFile", () -> PdfUtils.readFile(file2,
            new PrintStream(ResourceFXUtils.getOutFile(file2.getName().replaceAll("\\.pdf", "") + ".txt"))));
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
