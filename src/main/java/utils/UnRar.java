package utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.RarException.RarExceptionType;
import com.github.junrar.rarfile.FileHeader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class UnRar {
    private static final String UNRAR_FILE = "\"C:\\Program Files (x86)\\WinRAR\\UnRAR.exe\"";
    private static final Logger LOGGER = HasLogging.log();
    private static final String SRC_DIRECTORY = new File("").getAbsolutePath();

    private List<String> successfulFiles = new ArrayList<>();
    private List<String> unsupportedFiles = new ArrayList<>();
    private List<String> errorFiles = new ArrayList<>();

    private UnRar() {
    }

    private void extractRar(File file) {
        File output = new File(file.getParentFile(), file.getName().replaceAll("\\.rar", ""));
        if (file.exists()) {
            if (!output.exists()) {
                output.mkdir();
            }
            if (file.isDirectory()) {
                recurseDirectory(file, output);
            } else {
                testFile(file, output);
            }
        }
        printSummary();
    }

    private void printSummary() {
        LOGGER.info("Successfully tested archives:");
        successfulFiles.forEach(LOGGER::info);
        LOGGER.info("Unsupported archives:");
        unsupportedFiles.forEach(LOGGER::info);
        LOGGER.info("Failed archives:");
        errorFiles.forEach(LOGGER::info);
        LOGGER.info("Summary");
        LOGGER.info("tested:\t{}", successfulFiles.size() + unsupportedFiles.size() + errorFiles.size());
        LOGGER.info("successful:\t{}", successfulFiles.size());
        LOGGER.info("unsupported:\t{}", unsupportedFiles.size());
        LOGGER.info("failed:\t{}", errorFiles.size());
    }

    private void recurseDirectory(File file, File output) {
        if (doesNotExist(file)) {
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

    private void testFile(File file, File output) {
        if (doesNotExist(file)) {
            LOGGER.info("error file {} does not exist", file);
            return;
        }
        String s = file.toString().replaceAll(".+\\.(\\w+)$", "$1");
        if (!"rar".equalsIgnoreCase(s)) {
            return;
        }
        LOGGER.trace("{}", file);
        try (FileInputStream fileInputStream = new FileInputStream(file); Archive arc = new Archive(fileInputStream)) {
            if (arc.isEncrypted()) {
                LOGGER.trace("archive is encrypted cannot extreact");
                unsupportedFiles.add(file.toString());
                return;
            }
            List<FileHeader> files = arc.getFileHeaders();
            for (FileHeader fh : files) {
                if (fh.isEncrypted()) {
                    LOGGER.trace("file is encrypted cannot extract: {}", fh.getFileNameString());
                    unsupportedFiles.add(file.toString());
                    return;
                }
                LOGGER.trace("extracting file: {}", fh.getFileNameString());
                logIfUnicode(fh);
                LOGGER.trace("start: {}", LocalTime.now());
                File file2 = new File(output, fh.getFileNameString());
                createIfDoesNotExists(file2);
                if (!tryExtractFile(file2, fh, arc, file)) {
                    return;
                }
                LOGGER.trace("end: {}", LocalTime.now());
            }
            LOGGER.trace("successfully tested archive: {}", file);
            successfulFiles.add(file.toString());
        } catch (Exception e) {
            LOGGER.trace("file: {} extraction error - does the file exist? {}", file, e);
            errorFiles.add(file.toString());
            ConsoleUtils.startProcessAndWait(
                String.format("%s e \"%s\" \"%s\" -y", UNRAR_FILE, file, output), "Tudo OK");
        }
    }

    private boolean tryExtractFile(File file2, FileHeader fh, Archive arc, File file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file2)) {
            arc.extractFile(fh, os);
        } catch (RarException e) {
            if (e.getType() == RarExceptionType.notImplementedYet) {
                LOGGER.trace("error extracting unsupported file: {}", fh.getFileNameString() + e);
                unsupportedFiles.add(file.toString());
                return false;
            }
            LOGGER.trace("error extracting file: {}", fh.getFileNameString() + e);
            errorFiles.add(file.toString());
            return false;
        }
        return true;

    }

    public static void extractRarFiles(File file) {
        new UnRar().extractRar(file);
    }



    public static void main(String[] args) {
        File file = new File(SRC_DIRECTORY);
        extractRarFiles(file);
    }

    private static void createIfDoesNotExists(File file2) throws IOException {
        if (!file2.exists()) {
            boolean createNewFile = file2.createNewFile();
            LOGGER.info("file {} created {}", file2, createNewFile);
        }
    }

    private static boolean doesNotExist(File file) {
        return file == null || !file.exists();
    }

    private static boolean isUnicode(FileHeader fh) {
        return fh.isFileHeader() && fh.isUnicode();
    }

    private static void logIfUnicode(FileHeader fh) {
        if (isUnicode(fh)) {
            LOGGER.info("unicode name: {}", fh.getFileNameW());
        }
    }
}
