package fxtests;

import static fxtests.FXTesting.measureTime;

import extract.PdfUtils;
import java.io.File;
import java.io.PrintStream;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import pdfreader.PdfReader;
import utils.ResourceFXUtils;

public class FXPDFReaderTest extends AbstractTestExecution {

    @Test
    public void testPdfUtils() {
        File file2 = randomItem(ResourceFXUtils.getPathByExtension(new File(""), ".pdf")).toFile();
        measureTime("PdfUtils.readFile", () -> PdfUtils.readFile(file2,
            new PrintStream(ResourceFXUtils.getOutFile(file2.getName().replaceAll("\\.pdf", "") + ".txt"))));
    }

    @Test
    public void verifyButtons() {
        show(PdfReader.class);
        lookup(".button").queryAll().forEach(t -> {
            sleep(1000);
            tryClickOn(t);
            type(KeyCode.ESCAPE);
        });
    }
}
