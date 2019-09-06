package fxtests;

import static contest.db.ContestReader.getContestQuestions;

import contest.db.ContestReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ContestReaderTest {

    Logger log = HasLogging.log();

//    @Test
    public void testAllFiles() {
        List<File> listFiles = Arrays
            .asList(ResourceFXUtils.getOutFile().listFiles(f -> f.isDirectory() && f.getName().matches("\\d+")));
        List<String> invalidFiles = new ArrayList<>();
        for (File file : listFiles) {
            File firstPdf = ResourceFXUtils.getFirstPathByExtension(file, "pdf").toFile();
            getContestQuestions(firstPdf, reader -> addToInvalidFiles(invalidFiles, firstPdf, reader));
        }
        displayResults(listFiles, invalidFiles);
    }

    @Test
    public void testErrorFiles() {
        List<File> listFiles = Arrays
            .asList("20130122195848234\\Provasaplicadasem2012013-EmpregosdeN¡velSuperiorde110a112\\EBSERH_110.pdf",
                "20130122204521337\\Provasaplicadasem2012013-EmpregosdeN¡velSuperiorde127a129\\EBSERH_127.pdf")
            .stream()
            .map(ResourceFXUtils::getOutFile).collect(Collectors.toList());
        List<String> invalidFiles = new ArrayList<>();
        for (File file : listFiles) {
            getContestQuestions(file, reader -> addToInvalidFiles(invalidFiles, file, reader));
        }
        displayResults(listFiles, invalidFiles);
    }

    private void addToInvalidFiles(List<String> invalidFiles, File firstPdf, ContestReader i) {
        String absolutePath = ResourceFXUtils.getOutFile().getAbsolutePath() + "\\";
        if (!i.validate()) {
            log.error("ERROR IN FILE {}", firstPdf);
            invalidFiles.add(firstPdf.getAbsolutePath().replace(absolutePath, ""));
        } else {
            log.info("VALID {}", firstPdf);
        }
    }

    private void displayResults(List<File> listFiles, List<String> invalidFiles) {
        String invalidStr = invalidFiles.stream().map(e -> e.replace("\\", "\\\\"))
            .collect(Collectors.joining("\",\"", "\"", "\""));
        log.info("INVALID {}/{}  {}", invalidFiles.size(), listFiles.size(), invalidStr);
    }

}