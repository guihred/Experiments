package fxtests;

import static contest.db.ContestReader.getContestQuestions;

import java.io.File;
import java.nio.file.Path;
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

    public void testAllFiles() {
        File[] listFiles = ResourceFXUtils.getOutFile().listFiles(f -> f.isDirectory() && f.getName().matches("\\d+"));
        List<String> invalidFiles = new ArrayList<>();
        String absolutePath = ResourceFXUtils.getOutFile().getAbsolutePath();
        for (File file : listFiles) {
            Path firstPdf = ResourceFXUtils.getFirstPathByExtension(file, "pdf");
            getContestQuestions(firstPdf.toFile(), (i) -> {
                if (!i.validate()) {
                    log.error("ERROR IN FILE {}", firstPdf);
                    invalidFiles.add(firstPdf.toFile().getAbsolutePath().replace(absolutePath + "\\", ""));
                } else {
                    log.info("VALID {}", firstPdf);
                }
            });
        }
        log.info("INVALID {}", invalidFiles);

        String invalidStr = invalidFiles.stream().map(e -> e.replace("\\", "\\\\"))
            .collect(Collectors.joining("\",\"", "\"", "\""));
        log.info("INVALID {}/{}  {}", invalidFiles.size(), listFiles.length, invalidStr);
    }

    @Test
    public void testErrorFiles() {

        List<String> asList = Arrays.asList(
            "20110504114659355\\PO_101-NS-AdministraÆo.pdf", "2012121910575597\\EBSERH_101.pdf",
            "2013010312319122\\EBSERH_201.pdf");
        List<File> listFiles = asList.stream().map(ResourceFXUtils::getOutFile).collect(Collectors.toList());
        List<String> invalidFiles = new ArrayList<>();
        String absolutePath = ResourceFXUtils.getOutFile().getAbsolutePath();
        for (File file : listFiles) {
            getContestQuestions(file, (i) -> {
                if (!i.validate()) {
                    log.error("ERROR IN FILE {}", file);
                    invalidFiles.add(file.getAbsolutePath().replace(absolutePath + "\\", ""));
                } else {
                    log.info("VALID {}", file);
                }
            });
        }
        String invalidStr = invalidFiles.stream().map(e -> e.replace("\\", "\\\\"))
            .collect(Collectors.joining("\",\"", "\"", "\""));
        log.info("INVALID {}/{}  {}", invalidFiles.size(), asList.size(), invalidStr);
    }

}