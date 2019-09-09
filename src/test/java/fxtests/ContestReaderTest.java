package fxtests;

import static contest.db.ContestReader.getContestQuestions;

import contest.db.ContestReader;
import contest.db.IadesCrawler;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ContestReaderTest extends AbstractTestExecution {

    private Logger log = HasLogging.log();

    private List<String> invalidFiles = new ArrayList<>();
    @Test
    public void testAllFiles() {
        List<File> listFiles = Arrays
            .asList(ResourceFXUtils.getOutFile().listFiles(f -> f.isDirectory() && f.getName().matches("\\d+")));
        for (File file : listFiles) {
            File firstPdf = ResourceFXUtils.getFirstPathByExtension(file, "pdf").toFile();
            getContestQuestions(firstPdf, reader -> addToInvalidFiles(invalidFiles, firstPdf, reader));
        }
        displayResults(listFiles, invalidFiles);
    }

    public void testErrorFiles() {
        List<File> listFiles = invalidFiles
            .stream()
            .map(ResourceFXUtils::getOutFile).collect(Collectors.toList());
        List<String> invalidFiles2 = new ArrayList<>();
        for (File file : listFiles) {
            getContestQuestions(file, reader -> addToInvalidFiles(invalidFiles2, file, reader));
        }
        displayResults(listFiles, invalidFiles2);
    }

    @Test
    public void testIades() {
        show(IadesCrawler.class);
        type(KeyCode.SPACE);
        type(KeyCode.RIGHT);
        type(KeyCode.DOWN, 9);
        type(KeyCode.RIGHT);
        type(KeyCode.DOWN, 9);
        type(KeyCode.TAB);
        type(KeyCode.DOWN, 9);
        type(KeyCode.TAB);
        type(KeyCode.SPACE);
        type(KeyCode.DOWN, 1);

    }

    private void addToInvalidFiles(List<String> invalidFiles1, File firstPdf, ContestReader i) {
        String absolutePath = ResourceFXUtils.getOutFile().getAbsolutePath() + "\\";
        if (!i.validate()) {
            log.error("ERROR IN FILE {}", firstPdf);
            invalidFiles1.add(firstPdf.getAbsolutePath().replace(absolutePath, ""));
        } else {
            log.info("VALID {}", firstPdf);
        }
    }

    private void displayResults(List<File> listFiles, List<String> invalidFiles1) {
        String invalidStr = invalidFiles1.stream().map(e -> e.replace("\\", "\\\\"))
            .collect(Collectors.joining("\",\"", "\"", "\""));
        log.info("INVALID {}/{}  {}", invalidFiles1.size(), listFiles.size(), invalidStr);
    }

}