package others;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.RarException.RarExceptionType;
import com.github.junrar.rarfile.FileHeader;

import simplebuilder.HasLogging;

public final class UnRar {
    private static final String SRC_DIRECTORY = "C:\\Users\\Guilherme\\Videos\\FantasticBeasts\\Fantastic.Beasts.and.Where.to.Find.Them";

    private static final Logger LOGGER = HasLogging.log();

    private static List<String> successfulFiles = new ArrayList<>();
    private static List<String> errorFiles = new ArrayList<>();
    private static List<String> unsupportedFiles = new ArrayList<>();

    private UnRar() {
    }

    public static void main(String[] args) {
        File file = new File(SRC_DIRECTORY);
        File output = new File(file, "arquivos3");
        if (!output.exists()) {
            output.mkdir();
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                recurseDirectory(file, output);
            } else {
                testFile(file, output);
            }
        }
        printSummary();
    }

    private static void printSummary() {
        LOGGER.info("\n\n\nSuccessfully tested archives:\n");
        for (String sf : successfulFiles) {
            LOGGER.info(sf);
        }
        LOGGER.info("");
        LOGGER.info("Unsupported archives:\n");
        for (String uf : unsupportedFiles) {
            LOGGER.info(uf);
        }
        LOGGER.info("");
        LOGGER.info("Failed archives:");
        for (String ff : errorFiles) {
            LOGGER.info(ff);
        }
        LOGGER.info("");
        LOGGER.info("\n\n\nSummary\n");
        LOGGER.info("tested:\t\t{}", successfulFiles.size() + unsupportedFiles.size() + errorFiles.size());
        LOGGER.info("successful:\t{}", successfulFiles.size());
        LOGGER.info("unsupported:\t{}", unsupportedFiles.size());
        LOGGER.info("failed:\t\t{}", errorFiles.size());
    }

    private static void testFile(File file, File output) {
        if (file == null || !file.exists()) {
            LOGGER.info("error file {} does not exist", file);
            return;
        }
        LOGGER.info(">>>>>> testing archive: {}", file);
        String s = file.toString();

        s = s.substring(s.length() - 3);
        if ("rar".equalsIgnoreCase(s)) {
            LOGGER.info("{}", file);
            try (Archive arc = new Archive(file);) {
                if (arc.isEncrypted()) {
                    LOGGER.info("archive is encrypted cannot extreact");
                    unsupportedFiles.add(file.toString());
                    return;
                }
                List<FileHeader> files = arc.getFileHeaders();
                for (FileHeader fh : files) {
                    if (fh.isEncrypted()) {
                        LOGGER.info("file is encrypted cannot extract: {}", fh.getFileNameString());
                        unsupportedFiles.add(file.toString());
                        return;
                    }
                    LOGGER.info("extracting file: {}", fh.getFileNameString());
                    if (fh.isFileHeader() && fh.isUnicode()) {
                        LOGGER.info("unicode name: {}", fh.getFileNameW());
                    }
                    LOGGER.info("start: {}", new Date());
                    File file2 = new File(output, fh.getFileNameString());
                    if (!file2.exists()) {
                        file2.createNewFile();
                    }
                    if (!tryExtractFile(file2, fh, arc, file)) {
                        return;
                    }
                    LOGGER.info("end: {}", new Date());
                }
                LOGGER.info("successfully tested archive: {}", file);
                successfulFiles.add(file.toString());
            } catch (Exception e) {
                LOGGER.info("file: {} extraction error - does the file exist?{}", file, e);
                errorFiles.add(file.toString());
            }

        }
    }

    private static boolean tryExtractFile(File file2, FileHeader fh, Archive arc, File file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file2);) {
            arc.extractFile(fh, os);
        } catch (RarException e) {
            if (e.getType().equals(RarExceptionType.notImplementedYet)) {
                LOGGER.info("error extracting unsupported file: {}", fh.getFileNameString() + e);
                unsupportedFiles.add(file.toString());
                return false;
            }
            LOGGER.info("error extracting file: {}", fh.getFileNameString() + e);
            errorFiles.add(file.toString());
            return false;
        }
        return true;

    }

    private static void recurseDirectory(File file, File output) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                recurseDirectory(f, output);
            }
        } else {
            testFile(file, output);
        }
    }
}
